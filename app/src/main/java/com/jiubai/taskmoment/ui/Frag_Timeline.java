package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadTask;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.News;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.BaseUploadListener;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.receiver.Receiver_UpdateView;
import com.jiubai.taskmoment.view.BorderScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * 时间线（任务圈）
 */
public class Frag_Timeline extends Fragment implements View.OnClickListener {

    private static ListView lv;
    private static Adpt_Timeline adapter;
    private static View footerView;
    private static int keyBoardHeight;

    public static BorderScrollView sv;
    public static LinearLayout ll_comment;
    public static LinearLayout ll_audit;
    public static Space space;
    public static ArrayList<String> pictureList; // 供任务附图上传中时使用
    public static String taskID;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;

    private ArrayList<News> newsList;
    private boolean isBottomRefreshing = false;
    private RelativeLayout rl_timeline;
    private SwipeRefreshLayout srl;
    private TextView tv_nickname;
    private ImageView iv_companyBackground;
    private ImageView iv_news_portrait;
    private LinearLayout ll_news;
    private TextView tv_news_num;
    private ImageView iv_portrait;
    private Uri imageUri = Uri.parse(Constants.TEMP_FILE_LOCATION); // 用于存放背景图
    private Receiver_UpdateView newsReceiver, nicknameReceiver,
            portraitReceiver, deleteTaskReceiver, commentReceiver, changeBackgroundReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_timeline, container, false);

        initView(view);
        return view;
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView(View view) {
        iv_portrait = (ImageView) view.findViewById(R.id.iv_portrait);
        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        iv_portrait.setOnClickListener(this);

        tv_nickname = (TextView) view.findViewById(R.id.tv_timeline_nickname);
        if (!"".equals(Config.NICKNAME) && !"null".equals(Config.NICKNAME)) {
            tv_nickname.setText(Config.NICKNAME);
        }

        if (Config.PORTRAIT != null) {
            ImageLoader.getInstance().displayImage(Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
        } else {
            iv_portrait.setImageResource(R.drawable.portrait_default);
        }

        srl = (SwipeRefreshLayout) view.findViewById(R.id.swipe_timeline);
        srl.setColorSchemeResources(R.color.primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新过以后，可以重新进行底部加载
                if (lv.getFooterViewsCount() == 0) {
                    lv.addFooterView(footerView);
                }

                refreshTimeline("refresh",
                        Calendar.getInstance(Locale.CHINA).getTimeInMillis() + "");

            }
        });

        srl.setEnabled(true);

        lv = (ListView) view.findViewById(R.id.lv_timeline);

        footerView = LayoutInflater.from(getActivity()).inflate(R.layout.load_more_timeline, null);

        sv = (BorderScrollView) view.findViewById(R.id.sv_timeline);
        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
            }

            @Override
            public void onBottom() {
                // 有footerView并且不是正在加载
                if (lv.getFooterViewsCount() > 0 && !isBottomRefreshing) {

                    isBottomRefreshing = true;

                    refreshTimeline("loadMore",
                            (Adpt_Timeline.taskList
                                    .get(Adpt_Timeline.taskList.size() - 1)
                                    .getCreateTime() / 1000 - 1) + "");

                }
            }
        });

        space = (Space) view.findViewById(R.id.space);

        TextView tv_space_comment = (TextView) view.findViewById(R.id.tv_space_comment);
        tv_space_comment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (commentWindowIsShow) {
                    UtilBox.setViewParams(space, 0, 0);

                    ll_comment.setVisibility(View.GONE);
                    commentWindowIsShow = false;

                    // 关闭键盘
                    UtilBox.toggleSoftInput(ll_comment, false);
                }
                return false;
            }
        });

        TextView tv_space_audit = (TextView) view.findViewById(R.id.tv_space_audit);
        tv_space_audit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (auditWindowIsShow) {
                    ll_audit.setVisibility(View.GONE);

                    auditWindowIsShow = false;
                }
                return false;
            }
        });

        ll_comment = (LinearLayout) view.findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) view.findViewById(R.id.ll_audit);

        tv_news_num = (TextView) view.findViewById(R.id.tv_news_num);
        ll_news = (LinearLayout) view.findViewById(R.id.ll_news);
        ll_news.setOnClickListener(this);

        GradientDrawable newsBgShape = (GradientDrawable) ll_news.getBackground();
        newsBgShape.setColor(ContextCompat.getColor(getActivity(), R.color.news));

        iv_companyBackground = (ImageView) view.findViewById(R.id.iv_companyBackground);
        iv_companyBackground.setOnClickListener(this);

        final TextView tv_addBackground = (TextView) view.findViewById(R.id.tv_add_background);

        if (Config.COMPANY_BACKGROUND != null) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground,
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            tv_addBackground.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {
                        }
                    });
        }

        iv_news_portrait = (ImageView) view.findViewById(R.id.iv_news_portrait);
        ImageLoader.getInstance().displayImage(Config.PORTRAIT + "?t=" + Config.TIME, iv_news_portrait);

        Aty_Main.toolbar.findViewById(R.id.iBtn_publish).setOnClickListener(this);

        rl_timeline = (RelativeLayout) view.findViewById(R.id.rl_timeline);
        rl_timeline.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        rl_timeline.getWindowVisibleDisplayFrame(r);

                        int screenHeight = rl_timeline.getRootView().getHeight();
                        int difference = screenHeight - (r.bottom - r.top);

                        if (difference > 100) {
                            keyBoardHeight = difference;
                        }
                    }
                });

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                refreshTimeline("refresh",
                        Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
            }
        }, 500);
    }

    /**
     * 注册广播接收器
     */
    @Override
    public void onStart() {
        newsReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_news_num.setText(Config.NEWS_NUM + "");

                        try {
                            if (newsList == null) {
                                newsList = new ArrayList<>();
                            }

                            JSONObject msgJson = new JSONObject(msg);

                            JSONObject contentJson = new JSONObject(msgJson.getString("content"));

                            newsList.add(new News(msgJson.getString("mid"),
                                    decodeTask(msgJson.getString("task")),
                                    contentJson.getString("title"),
                                    contentJson.getString("content"),
                                    UtilBox.getDateToString(
                                            Long.valueOf(contentJson.getString("time")) * 1000,
                                            UtilBox.TIME)));

                            // 显示头像
                            ImageLoader.getInstance().displayImage(
                                    Urls.MEDIA_CENTER_PORTRAIT + msgJson.getString("mid") + ".jpg"
                                            + "?t=" + Config.TIME,
                                    iv_news_portrait);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ll_news.setVisibility(View.VISIBLE);
                    }
                });
        newsReceiver.registerAction(Constants.ACTION_NEWS);

        nicknameReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_nickname.setText(msg);
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        portraitReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
                    }
                });
        portraitReceiver.registerAction(Constants.ACTION_CHANGE_PORTRAIT);

        deleteTaskReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < Adpt_Timeline.taskList.size(); i++) {
                            // TODO 如果任务ID是递增的，就可以用二分查找
                            if (Adpt_Timeline.taskList.get(i).getId().equals(taskID)) {
                                Adpt_Timeline.taskList.remove(i);
                                adapter.notifyDataSetChanged();
                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                break;
                            }
                        }
                    }
                });
        deleteTaskReceiver.registerAction(Constants.ACTION_DELETE_TASK);

        commentReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < Adpt_Timeline.taskList.size(); i++) {
                            Task task = Adpt_Timeline.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                Comment comment = (Comment) objects[0];
                                // 防止多次添加
                                if (task.getComments().isEmpty() ||
                                        task.getComments().get(task.getComments().size() - 1).getTime()
                                                != comment.getTime()) {

                                    Adpt_Timeline.taskList.get(i).getComments().add(comment);

                                    adapter.notifyDataSetChanged();
                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    break;
                                }
                            }
                        }
                    }
                });
        commentReceiver.registerAction(Constants.ACTION_SEND_COMMENT);

        changeBackgroundReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);
                    }
                });
        changeBackgroundReceiver.registerAction(Constants.ACTION_CHANGE_BACKGROUND);

        super.onStart();
    }

    @Override
    public void onDestroy() {
        // 应该在接收完了以后就注销掉，但根据需求，不应注销
        getActivity().unregisterReceiver(newsReceiver);
        getActivity().unregisterReceiver(nicknameReceiver);
        getActivity().unregisterReceiver(portraitReceiver);
        getActivity().unregisterReceiver(deleteTaskReceiver);
        getActivity().unregisterReceiver(commentReceiver);
        getActivity().unregisterReceiver(changeBackgroundReceiver);

        super.onDestroy();
    }

    /**
     * 解析Task的Json字符串
     *
     * @param taskJson Task的Json字符串
     * @return 解析出来的任务
     * @throws JSONException
     */
    private Task decodeTask(String taskJson) throws JSONException {
        JSONObject obj = new JSONObject(taskJson);

        String id = obj.getString("id");

        String mid = obj.getString("mid");
        String portraitUrl = Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg";

        String nickname = obj.getString("show_name");

        char p1 = obj.getString("p1").charAt(0);
        String grade = (p1 - 48) == 1 ? "S" : String.valueOf((char) (p1 + 15));

        String desc = obj.getString("comments");
        String executor = obj.getString("ext1");
        String supervisor = obj.getString("ext2");
        String auditor = obj.getString("ext3");

        ArrayList<String> pictures = decodePictureList(obj.getString("works"));
        ArrayList<Comment> comments
                = decodeCommentList(id, obj.getString("member_comment"));

        long deadline = Long.valueOf(obj.getString("time1")) * 1000;
        long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
        long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

        String audit_result = obj.getString("p2");

        return new Task(id, portraitUrl, nickname, mid, grade, desc,
                executor, supervisor, auditor,
                pictures, comments, deadline, publish_time, create_time, audit_result);
    }

    /**
     * 将json解码成list
     *
     * @param pictures 图片Json
     * @return 图片list
     */
    private ArrayList<String> decodePictureList(String pictures) {
        ArrayList<String> pictureList = new ArrayList<>();

        if (pictures != null && !"null".equals(pictures)) {
            try {
                JSONArray jsonArray = new JSONArray(pictures);

                for (int i = 0; i < jsonArray.length(); i++) {
                    pictureList.add(Urls.MEDIA_CENTER_TASK + jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return pictureList;
    }

    /**
     * 将json解码成list
     *
     * @param comments 评论json
     * @return 图片List
     */
    private ArrayList<Comment> decodeCommentList(String taskID, String comments) {
        ArrayList<Comment> commentList = new ArrayList<>();

        if (!"".equals(comments) && !"null".equals(comments)) {
            try {

                JSONArray jsonArray = new JSONArray(comments);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = new JSONObject(jsonArray.getString(i));

                    String sender = "null".equals(object.getString("send_real_name")) ?
                            object.getString("send_mobile") : object.getString("send_real_name");

                    String receiver = "null".equals(object.getString("receiver_real_name")) ?
                            object.getString("receiver_mobile") : object.getString("receiver_real_name");

                    if ("null".equals(receiver)) {
                        Comment comment = new Comment(taskID,
                                sender, object.getString("send_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    } else {
                        Comment comment = new Comment(taskID,
                                sender, object.getString("send_id"),
                                receiver, object.getString("receiver_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return commentList;
    }

    /**
     * 刷新时间线
     *
     * @param type         refresh或loadMore
     * @param request_time 需要获取哪个时间之后的数据
     */
    private void refreshTimeline(final String type, final String request_time) {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(getActivity(),
                    R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("refresh".equals(type)) {
            srl.setRefreshing(true);
        }

        String[] key = {"len", "cid", "create_time"};
        String[] value = {"2", Config.CID, request_time};

        VolleyUtil.requestWithCookie(Urls.GET_TASK_LIST, key, value,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            String responseStatus = responseJson.getString("status");

                            if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {

                                if ("refresh".equals(type)) {
                                    adapter = new Adpt_Timeline(getActivity(), true, response);
                                } else {
                                    adapter = new Adpt_Timeline(getActivity(), false, response);
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                lv.setAdapter(adapter);
                                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                                if ("refresh".equals(type)) {
                                                    srl.setRefreshing(false);

                                                    int svHeight = sv.getHeight();

                                                    int lvHeight = lv.getLayoutParams().height;

                                                    // 312是除去上部其他组件高度后的剩余空间，
                                                    int newsBar = ll_news.getVisibility() == View.GONE ? 312 : 357;
                                                    if (lvHeight > svHeight - UtilBox.dip2px(getActivity(), newsBar)
                                                            && lv.getFooterViewsCount() == 0) {
                                                        lv.addFooterView(footerView);
                                                        UtilBox.setListViewHeightBasedOnChildren(lv);
                                                    }
                                                } else {
                                                    isBottomRefreshing = false;
                                                }
                                            }
                                        });
                                    }
                                }, 1000);

                            } else if ("900900".equals(responseStatus)) {
                                // 没有更多了，就去掉footerView
                                if ("refresh".equals(type)) {
                                    srl.setRefreshing(false);
                                } else {
                                    lv.removeFooterView(footerView);

                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(getActivity(),
                                        responseJson.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if ("refresh".equals(type)) {
                                    srl.setRefreshing(false);
                                } else {
                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(getActivity(),
                                        responseJson.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        if ("refresh".equals(type)) {
                            srl.setRefreshing(false);
                        } else {
                            isBottomRefreshing = false;
                        }
                        volleyError.printStackTrace();
                        Toast.makeText(getActivity(),
                                "刷新失败，请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );
    }

    /**
     * 弹出评论窗口
     *
     * @param context    上下文
     * @param taskID     任务ID
     * @param receiver   接收者
     * @param receiverID 接收者ID
     * @param y          所点击的组件的y坐标
     */
    public static void showCommentWindow(final Context context, final String taskID,
                                         final String receiver, final String receiverID,
                                         final int y) {
        commentWindowIsShow = true;

        if (auditWindowIsShow) {
            ll_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        ll_comment.setVisibility(View.VISIBLE);
        final EditText edt_content = (EditText) ll_comment.findViewById(R.id.edt_comment_content);
        edt_content.requestFocus();

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = false;
                while (!flag) {
                    if (keyBoardHeight == 0) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        flag = true;

                        Looper.prepare();

                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 50) + keyBoardHeight);
                            }
                        });

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                int viewHeight = UtilBox.getHeightPixels(context) - y;

                                int finalScroll = keyBoardHeight - viewHeight
                                        + sv.getScrollY() + UtilBox.dip2px(context, 50);

                                sv.smoothScrollTo(0, finalScroll);
                            }
                        });
                        Looper.loop();
                    }
                }
            }
        }).start();

        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);

        Button btn_send = (Button) ll_comment.findViewById(R.id.btn_comment_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_content.getText().toString().isEmpty()) {
                    Toast.makeText(context,
                            "请填入评论内容",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else if (!Config.IS_CONNECTED) {
                    Toast.makeText(context,
                            R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ll_comment.setVisibility(View.GONE);

                UtilBox.setViewParams(space, 0, 0);

                UtilBox.toggleSoftInput(ll_comment, false);

                String[] key = {"oid", "pmid", "content"};
                String[] value = {taskID, receiverID, edt_content.getText().toString()};

                VolleyUtil.requestWithCookie(Urls.SEND_COMMENT, key, value,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    if (!"900001".equals(jsonObject.getString("status"))) {
                                        System.out.println(response);

                                        Toast.makeText(context,
                                                "发送失败，请重试",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 发送更新评论广播
                                        Intent intent = new Intent(Constants.ACTION_SEND_COMMENT);
                                        intent.putExtra("taskID", taskID);

                                        String nickname;
                                        if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                                            nickname = "你";
                                        } else {
                                            nickname = Config.NICKNAME;
                                        }

                                        if (!"".equals(receiver)) {
                                            intent.putExtra("comment", new Comment(taskID,
                                                    nickname, Config.MID,
                                                    receiver, receiverID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis()));
                                        } else {
                                            intent.putExtra("comment", new Comment(taskID,
                                                    nickname, Config.MID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis()));
                                        }

                                        context.sendBroadcast(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                volleyError.printStackTrace();
                                Toast.makeText(context,
                                        "评论发送失败，请重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    /**
     * 弹出审核窗口
     *
     * @param context 上下文
     * @param taskID  任务ID
     */
    public static void showAuditWindow(final Context context, final String taskID) {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            UtilBox.setViewParams(space, 1, 0);
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;
        }

        ll_audit.setVisibility(View.VISIBLE);

        final int[] audit_result = {3};
        RadioGroup radioGroup = (RadioGroup) ll_audit.findViewById(R.id.rg_audit);
        radioGroup.check(R.id.rb_complete);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_failed:
                        audit_result[0] = 4;
                        break;

                    case R.id.rb_complete:
                        audit_result[0] = 3;
                        break;

                    case R.id.rb_solved:
                        audit_result[0] = 2;
                        break;
                }
            }
        });

        Button btn_send = (Button) ll_audit.findViewById(R.id.btn_audit_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(context,
                            R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] key = {"id", "level"};
                String[] value = {taskID, audit_result[0] + ""};

                VolleyUtil.requestWithCookie(Urls.SEND_AUDIT, key, value,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject responseObject = new JSONObject(response);
                                    String status = responseObject.getString("status");
                                    if ("1".equals(status) || "900001".equals(status)) {
                                        ll_audit.setVisibility(View.GONE);

                                        Toast.makeText(context,
                                                responseObject.getString("info"),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context,
                                                responseObject.getString("info"),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                volleyError.printStackTrace();

                                Toast.makeText(context, "审核失败，请重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_companyBackground:
                String[] items = {"更换公司封面"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Config.MID.equals(Config.COMPANY_CREATOR)) {
                            Toast.makeText(getActivity(),
                                    "你不是管理员，不能更换公司封面",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

                        intent.setType("image/*");
                        intent.putExtra("crop", "true");
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", false);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent.putExtra("output", imageUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        intent.putExtra("noFaceDetection", true);

                        int standardWidth = UtilBox.getWidthPixels(getActivity());
                        int standardHeight = UtilBox.dip2px(getActivity(), 270);

                        // 裁剪框比例
                        intent.putExtra("aspectX", standardWidth);
                        intent.putExtra("aspectY", standardHeight);

                        // 输出值
                        intent.putExtra("outputX", standardWidth);
                        intent.putExtra("outputY", standardHeight);

                        startActivityForResult(intent, Constants.CODE_CHOOSE_COMPANY_BACKGROUND);

                        getActivity().overridePendingTransition(
                                R.anim.in_right_left, R.anim.out_right_left);
                    }
                })
                        .setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;

            case R.id.iv_portrait:
                Intent intent = new Intent(getActivity(), Aty_PersonalTimeline.class);
                intent.putExtra("mid", Config.MID);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
                break;

            case R.id.iBtn_publish:
                startActivityForResult(
                        new Intent(getActivity(), Aty_TaskPublish.class),
                        Constants.CODE_PUBLISH_TASK);
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.out_right_left);
                break;

            case R.id.ll_news:
                Intent newsIntent = new Intent(getActivity(), Aty_News.class);
                newsIntent.putExtra("newsList", newsList);
                startActivity(newsIntent);
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.out_right_left);

                Config.NEWS_NUM = 0;
                if (newsList != null) {
                    newsList.clear();
                }
                ll_news.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_COMPANY_BACKGROUND:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(getActivity(),
                                R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO 更换公司背景没有经过服务器
                    if (imageUri != null) {
                        try {
                            final Bitmap bitmap = BitmapFactory.decodeStream(
                                    getActivity().getContentResolver().openInputStream(imageUri));
                            iv_companyBackground.setImageBitmap(bitmap);

                            final String objectName = Config.CID + ".jpg";

                            UploadListener listener = new BaseUploadListener() {

                                @Override
                                public void onUploadFailed(UploadTask uploadTask, com.alibaba.sdk.android.media.utils.FailReason failReason) {
                                    System.out.println(failReason.getMessage());

                                    Toast.makeText(getActivity(),
                                            "图片上传失败，请重试",
                                            Toast.LENGTH_SHORT).show();

                                    ImageLoader.getInstance().displayImage(
                                            Config.COMPANY_BACKGROUND + "?t=" + Config.TIME,
                                            iv_companyBackground);
                                }

                                @Override
                                public void onUploadComplete(UploadTask uploadTask) {
                                    Config.COMPANY_BACKGROUND = Urls.MEDIA_CENTER_BACKGROUND + objectName;

                                    // 更新时间戳
                                    Config.TIME = Calendar.getInstance().getTimeInMillis();

                                    SharedPreferences sp = getActivity()
                                            .getSharedPreferences(Constants.SP_FILENAME,
                                                    Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND,
                                            Config.COMPANY_BACKGROUND);
                                    editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                                    editor.apply();

                                    ImageLoader.getInstance().displayImage(
                                            Config.COMPANY_BACKGROUND + "?t=" + Config.TIME,
                                            iv_companyBackground);
                                }

                            };

                            MediaServiceUtil.uploadImage(
                                    UtilBox.compressImage(bitmap, Constants.SIZE_COMPANY_BACKGROUND),
                                    Constants.DIR_BACKGROUND, objectName, listener);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case Constants.CODE_PUBLISH_TASK:
                if (resultCode == Activity.RESULT_OK) {
                    String grade = data.getStringExtra("grade");
                    String content = data.getStringExtra("content");
                    String executor = data.getStringExtra("executor");
                    String supervisor = data.getStringExtra("supervisor");
                    String auditor = data.getStringExtra("auditor");

                    taskID = data.getStringExtra("taskID");
                    pictureList = data.getStringArrayListExtra("pictureList");

                    long deadline = data.getLongExtra("deadline", 0);
                    long publish_time = data.getLongExtra("publish_time", 0);
                    long create_time = data.getLongExtra("create_time", 0);

                    String nickname;
                    if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                        nickname = "你";
                    } else {
                        nickname = Config.NICKNAME;
                    }

                    Adpt_Timeline.taskList.add(0, new Task(taskID,
                            Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg",
                            nickname, Config.MID, grade, content,
                            executor, supervisor, auditor,
                            pictureList, null, deadline, publish_time, create_time, "1"));

                    adapter.notifyDataSetChanged();

                    UtilBox.setListViewHeightBasedOnChildren(lv);
                }
                break;
        }
    }
}
