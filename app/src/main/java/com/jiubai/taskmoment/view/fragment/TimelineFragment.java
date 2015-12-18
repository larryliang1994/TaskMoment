package com.jiubai.taskmoment.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.adapter.TimelineAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.view.activity.MainActivity;
import com.jiubai.taskmoment.view.activity.NewsActivity;
import com.jiubai.taskmoment.view.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.view.activity.TaskPublishActivity;
import com.jiubai.taskmoment.widget.BorderScrollView;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.ITimelinePresenter;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.presenter.TimelinePresenterImpl;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.view.iview.IAuditView;
import com.jiubai.taskmoment.view.iview.ICommentView;
import com.jiubai.taskmoment.view.iview.ITaskView;
import com.jiubai.taskmoment.view.iview.ITimelineView;
import com.jiubai.taskmoment.view.iview.IUploadImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 时间线（任务圈）
 */
public class TimelineFragment extends Fragment implements ITimelineView, ICommentView, ITaskView,
        IUploadImageView, IAuditView, View.OnClickListener {

    private static ListView lv;
    private static TimelineAdapter adapter;
    private static View footerView;
    private static int keyBoardHeight;
    private static ICommentPresenter commentPresenter;
    private static IAuditPresenter auditPresenter;

    public static BorderScrollView sv;
    public static LinearLayout ll_comment;
    public static LinearLayout ll_audit;
    public static Space space;
    public static ArrayList<String> pictureList; // 供任务附图上传中时使用
    public static String taskID;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;

    private ITaskPresenter taskPresenter;
    private ITimelinePresenter timelinePresenter;
    private IUploadImagePresenter uploadImagePresenter;
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
    private UpdateViewReceiver newsReceiver, nicknameReceiver,
            portraitReceiver, deleteTaskReceiver, commentReceiver,
            auditReceiver, changeBackgroundReceiver;

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
    @SuppressLint("InflateParams")
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
                            (TimelineAdapter.taskList
                                    .get(TimelineAdapter.taskList.size() - 1)
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

        MainActivity.toolbar.findViewById(R.id.iBtn_publish).setOnClickListener(this);

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

        timelinePresenter = new TimelinePresenterImpl(this);
        commentPresenter = new CommentPresenterImpl(getActivity(), this);
        uploadImagePresenter = new UploadImagePresenterImpl(getActivity(), this);
        auditPresenter = new AuditPresenterImpl(getActivity(), this);
        taskPresenter = new TaskPresenterImpl(this);

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                timelinePresenter.onSetSwipeRefreshVisibility(Constants.VISIBLE);
                refreshTimeline("refresh",
                        Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
            }
        }, 200);
    }

    /**
     * 注册广播接收器
     */
    @Override
    public void onStart() {
        newsReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        try {
                            timelinePresenter.doGetNews(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        newsReceiver.registerAction(Constants.ACTION_NEWS);

        nicknameReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_nickname.setText(msg);
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        portraitReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
                        adapter.notifyDataSetChanged();
                    }
                });
        portraitReceiver.registerAction(Constants.ACTION_CHANGE_PORTRAIT);

        deleteTaskReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < TimelineAdapter.taskList.size(); i++) {
                            // TODO 如果任务ID是递增的，就可以用二分查找
                            if (TimelineAdapter.taskList.get(i).getId().equals(taskID)) {
                                TimelineAdapter.taskList.remove(i);
                                adapter.notifyDataSetChanged();
                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                break;
                            }
                        }
                    }
                });
        deleteTaskReceiver.registerAction(Constants.ACTION_DELETE_TASK);

        commentReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < TimelineAdapter.taskList.size(); i++) {
                            Task task = TimelineAdapter.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                Comment comment = (Comment) objects[0];
                                // 防止多次添加
                                if (task.getComments().isEmpty() ||
                                        task.getComments().get(task.getComments().size() - 1).getTime()
                                                != comment.getTime()) {

                                    TimelineAdapter.taskList.get(i).getComments().add(comment);

                                    adapter.notifyDataSetChanged();
                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    break;
                                }
                            }
                        }
                    }
                });
        commentReceiver.registerAction(Constants.ACTION_SEND_COMMENT);

        auditReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... object) {
                        for (int i = 0; i < TimelineAdapter.taskList.size(); i++) {

                            Task task = TimelineAdapter.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                task.setAuditResult((String) object[0]);
                                TimelineAdapter.taskList.set(i, task);

                                adapter.notifyDataSetChanged();
                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                break;
                            }
                        }
                    }
                });
        auditReceiver.registerAction(Constants.ACTION_AUDIT);

        changeBackgroundReceiver = new UpdateViewReceiver(getActivity(),
                new UpdateViewReceiver.UpdateCallBack() {
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
        getActivity().unregisterReceiver(auditReceiver);
        getActivity().unregisterReceiver(changeBackgroundReceiver);

        super.onDestroy();
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
            timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
            return;
        }

        if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
            UtilBox.getMember(getActivity(), new UtilBox.GetMemberCallBack() {
                @Override
                public void successCallback() {
                    timelinePresenter.doPullTimeline(request_time, type);
                }

                @Override
                public void failedCallback() {
                }
            });
        } else {
            timelinePresenter.doPullTimeline(request_time, type);
        }
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
        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);
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
                                UtilBox.setViewParams(space, 1,
                                        UtilBox.dip2px(context, 54) + keyBoardHeight);

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int viewHeight = UtilBox.getHeightPixels(context) - y;

                                        int finalScroll = keyBoardHeight - viewHeight
                                                + sv.getScrollY() + UtilBox.dip2px(context, 54);

                                        sv.smoothScrollTo(0, finalScroll);
                                    }
                                });
                            }
                        });
                        Looper.loop();
                    }
                }
            }
        }).start();

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

                commentPresenter.doSendComment(taskID, receiver, receiverID, edt_content.getText().toString());
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

                auditPresenter.doAudit(taskID, audit_result[0] + "");
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
                                R.anim.in_right_left, R.anim.scale_stay);
                    }
                })
                        .setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;

            case R.id.iv_portrait:
                Intent intent = new Intent(getActivity(), PersonalTimelineActivity.class);
                intent.putExtra("mid", Config.MID);
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;

            case R.id.iBtn_publish:
                startActivityForResult(
                        new Intent(getActivity(), TaskPublishActivity.class),
                        Constants.CODE_PUBLISH_TASK);
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.scale_stay);
                break;

            case R.id.ll_news:
                Intent newsIntent = new Intent(getActivity(), NewsActivity.class);
                newsIntent.putExtra("newsList", newsList);
                startActivity(newsIntent);
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.scale_stay);

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

                            uploadImagePresenter.doUploadImage(
                                    UtilBox.compressImage(bitmap, Constants.SIZE_COMPANY_BACKGROUND),
                                    Constants.DIR_BACKGROUND, objectName,
                                    Constants.SP_KEY_COMPANY_BACKGROUND);

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

                    TimelineAdapter.taskList.add(0, new Task(taskID,
                            Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg",
                            nickname, Config.MID, grade, content,
                            executor, supervisor, auditor,
                            pictureList, null, deadline, publish_time, create_time,
                            "1", Task.SENDING));

                    adapter.notifyDataSetChanged();

                    UtilBox.setListViewHeightBasedOnChildren(lv);

                    // 开始上传图片
                    uploadImagePresenter.doUploadImages(pictureList, Constants.DIR_TASK);
                }
                break;
        }
    }

    @Override
    public void onPullTimelineResult(String result, final String type, String info) {
        switch (result) {
            case Constants.SUCCESS:
                if ("refresh".equals(type)) {
                    adapter = new TimelineAdapter(getActivity(), true, info, uploadImagePresenter);
                } else {
                    adapter = new TimelineAdapter(getActivity(), false, info, uploadImagePresenter);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (lv.getAdapter() == null) {
                                    lv.setAdapter(adapter);
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                if ("refresh".equals(type)) {
                                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);

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
                }, 500);

                break;

            case Constants.NOMORE:
                // 没有更多了，就去掉footerView
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    lv.removeFooterView(footerView);

                    UtilBox.setListViewHeightBasedOnChildren(lv);

                    isBottomRefreshing = false;
                }

                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                break;

            case Constants.FAILED:
            default:
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    isBottomRefreshing = false;
                }

                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onGetNewsResult(int result, News news) {
        if (result == 1) {
            tv_news_num.setText(Config.NEWS_NUM + "");

            if (newsList == null) {
                newsList = new ArrayList<>();
            }

            newsList.add(news);

            // 显示头像
            ImageLoader.getInstance().displayImage(
                    Urls.MEDIA_CENTER_PORTRAIT + news.getSenderID() + ".jpg" + "?t=" + Config.TIME,
                    iv_news_portrait);

            ll_news.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        if (Constants.VISIBLE == visibility) {
            srl.setRefreshing(true);
        } else if (Constants.INVISIBLE == visibility) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    srl.setRefreshing(false);
                }
            }, 1000);
        }
    }

    @Override
    public void onSendCommentResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            //Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
        } else if (Constants.FAILED.equals(result)) {
            Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);
        } else if (Constants.FAILED.equals(result)) {
            Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();

            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {
        switch (result) {
            case Constants.SUCCESS:
                taskPresenter.doUpdateTask(taskID, pictureList);
                break;

            case Constants.FAILED:
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (TimelineAdapter.taskList.get(i).getId().equals(taskID)) {
                        TimelineAdapter.taskList.get(i).setSendState(Task.FAILED);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);
        }

        Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPublishTaskResult(String result, String info) {
    }

    @Override
    public void onDeleteTaskResult(String result, String info) {

    }

    @Override
    public void onUpdateTaskResult(String result, String info) {
        switch (result) {
            case Constants.SUCCESS:
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (TimelineAdapter.taskList.get(i).getId().equals(taskID)) {
                        TimelineAdapter.taskList.get(i).setSendState(Task.SUCCESS);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                break;

            default:
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (TimelineAdapter.taskList.get(i).getId().equals(taskID)) {
                        TimelineAdapter.taskList.get(i).setSendState(Task.FAILED);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}