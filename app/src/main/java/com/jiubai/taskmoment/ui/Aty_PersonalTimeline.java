package com.jiubai.taskmoment.ui;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.adapter.Adpt_PersonalTimeline;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Member;
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
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人情况
 */
public class Aty_PersonalTimeline extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iv_portrait)
    ImageView iv_portrait;

    @Bind(R.id.tv_loading)
    TextView tv_loading;

    @Bind(R.id.tv_personal_nickname)
    TextView tv_nickname;

    @Bind(R.id.tv_space_comment)
    TextView tv_space_comment;

    @Bind(R.id.tv_space_audit)
    TextView tv_space_audit;

    @Bind(R.id.iv_companyBackground)
    ImageView iv_companyBackground;

    @Bind(R.id.rl_timeline)
    RelativeLayout rl_timeline;

    private static LinearLayout ll_comment, ll_audit;
    private static Space space;
    private static BorderScrollView sv;
    private static ListView lv;
    private static Adpt_PersonalTimeline adapter;
    private static int keyBoardHeight;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;

    private View footerView;
    private String mid, name, mobile, isAudit, isInvolved;
    private boolean isBottomRefreshing = false;
    private Uri imageUri = Uri.parse(Constants.TEMP_FILE_LOCATION); // 用于存放背景图
    private Receiver_UpdateView deleteTaskReceiver, commentReceiver,
            nicknameReceiver, portraitReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_personal_timeline);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        isAudit = intent.getBooleanExtra("isAudit", false) ? "1" : "0";
        isInvolved = intent.getBooleanExtra("isInvolved", false) ? "1" : "0";

        if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
            UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                @Override
                public void successCallback() {
                    getUserInfo();
                    initView();
                }

                @Override
                public void failedCallback() {
                }
            });
        } else {
            getUserInfo();
            initView();
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {

        if (!"null".equals(name) && !"".equals(name)) {
            tv_title.setText(name);
            tv_nickname.setText(name);
        } else {
            tv_title.setText(mobile);
            tv_nickname.setText(mobile);
        }

        initPortrait();

        lv = (ListView) findViewById(R.id.lv_personal);
        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        space = (Space) findViewById(R.id.space);


        footerView = LayoutInflater.from(this).inflate(R.layout.load_more_timeline, null);

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

        sv = (BorderScrollView) findViewById(R.id.sv_personal);
        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
            }

            @Override
            public void onBottom() {
                // 有footerView并且不是正在加载
                if (lv.getFooterViewsCount() > 0 && !isBottomRefreshing) {
                    isBottomRefreshing = true;

                    // 参数应为最后一条任务的时间减1秒
                    refreshTimeline("loadMore", (Adpt_PersonalTimeline.taskList
                            .get(Adpt_PersonalTimeline.taskList.size() - 1)
                            .getCreateTime() / 1000 - 1) + "");
                }
            }
        });

        final TextView tv_addBackground = (TextView) findViewById(R.id.tv_add_background);

        if (Config.COMPANY_BACKGROUND != null) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND+ "?t=" + Config.TIME, iv_companyBackground,
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

        refreshTimeline("refresh", Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
    }

    /**
     * 初始化头像
     */
    private void initPortrait(){
        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        ImageLoader.getInstance().displayImage(
                Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg" + "?t=" + Config.TIME, iv_portrait);
        iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Aty_PersonalTimeline.this, Aty_PersonalInfo.class);

                String nickname;
                if (!"null".equals(name) && !"".equals(name)) {
                    nickname = name;
                } else {
                    nickname = mobile.substring(0, 3) + "***" + mobile.substring(7);
                }
                intent.putExtra("nickname", nickname);
                intent.putExtra("mid", mid);

                startActivity(intent);
                overridePendingTransition(R.anim.in_right_left, R.anim.scale_stay);
            }
        });
    }

    /**
     * 获取时间线
     *
     * @param type         类别 refresh或loadMore
     * @param request_time 需要获取哪个时间之后的数据
     */
    private void refreshTimeline(final String type, String request_time) {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(this, R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] key = {"len", "cid", "create_time", "mid", "shenhe", "canyu"};
        String[] value = {"2", Config.CID, request_time, mid, isAudit, isInvolved};

        VolleyUtil.requestWithCookie(Urls.GET_TASK_LIST, key, value,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            String responseStatus = responseJson.getString("status");

                            if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {

                                // 先实例出适配器
                                if ("refresh".equals(type)) {
                                    adapter = new Adpt_PersonalTimeline(
                                            Aty_PersonalTimeline.this, true, response);
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    adapter = new Adpt_PersonalTimeline(
                                            Aty_PersonalTimeline.this, false, response);
                                }

                                // 设置适配器，并重新设置ListView的高度
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        resetListViewHeight(type);
                                    }
                                });

                            } else if ("900900".equals(responseStatus)) {
                                // 没有更多了，就去掉footerView
                                if ("refresh".equals(type)) {
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    lv.removeFooterView(footerView);

                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(Aty_PersonalTimeline.this,
                                        responseJson.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if ("refresh".equals(type)) {
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(Aty_PersonalTimeline.this,
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
                            tv_loading.setVisibility(View.GONE);
                        } else {
                            isBottomRefreshing = false;
                        }

                        volleyError.printStackTrace();
                        Toast.makeText(Aty_PersonalTimeline.this,
                                "刷新失败，请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );
    }

    /**
     * 设置适配器，并重新设置ListView的高度
     *
     * @param type 刷新类型
     */
    private void resetListViewHeight(final String type){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lv.setAdapter(adapter);
                UtilBox.setListViewHeightBasedOnChildren(lv);

                if ("loadMore".equals(type)) {
                    isBottomRefreshing = false;
                } else {
                    int svHeight = sv.getHeight();

                    int lvHeight = lv.getLayoutParams().height;

                    // 312是除去上部其他组件高度后的剩余空间
                    if (lvHeight >
                            svHeight - UtilBox.dip2px(Aty_PersonalTimeline.this, 312)
                            && lv.getFooterViewsCount() == 0) {

                        lv.addFooterView(footerView);
                        UtilBox.setListViewHeightBasedOnChildren(lv);
                    }
                }
            }
        });
    }

    /**
     * 从memberList中读取用户信息
     */
    private void getUserInfo() {
        for (int i = 1; i < Adpt_Member.memberList.size() - 1; i++) {
            Member member = Adpt_Member.memberList.get(i);
            if (mid.equals(member.getMid())) {
                name = member.getName();
                mobile = member.getMobile();
                break;
            }
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
                while(!flag) {
                    if (keyBoardHeight == 0) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        flag = true;

                        Looper.prepare();

                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 52) + keyBoardHeight);

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int viewHeight = UtilBox.getHeightPixels(context) - y;

                                        int finalScroll = keyBoardHeight - viewHeight
                                                + sv.getScrollY() + UtilBox.dip2px(context, 52);

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
                                                "评论失败，请重试",
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
                                        "评论失败，请重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    /**
     * 弹出审核窗口
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

    @OnClick({R.id.iBtn_back, R.id.iv_companyBackground})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
                break;

            case R.id.iv_companyBackground:
                String[] items = {"更换公司封面"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Config.MID.equals(Config.COMPANY_CREATOR)) {
                            Toast.makeText(Aty_PersonalTimeline.this,
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

                        int standardWidth = UtilBox.getWidthPixels(Aty_PersonalTimeline.this);
                        int standardHeight = UtilBox.dip2px(Aty_PersonalTimeline.this, 270);

                        // 裁剪框比例
                        intent.putExtra("aspectX", standardWidth);
                        intent.putExtra("aspectY", standardHeight);

                        // 输出值
                        intent.putExtra("outputX", standardWidth);
                        intent.putExtra("outputY", standardHeight);

                        startActivityForResult(intent, Constants.CODE_CHOOSE_COMPANY_BACKGROUND);

                        overridePendingTransition(
                                R.anim.in_right_left, R.anim.scale_stay);
                    }
                })
                        .setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_COMPANY_BACKGROUND:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(this,
                                R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO 更换公司背景没有经过服务器
                    if (imageUri != null) {
                        try {
                            final Bitmap bitmap = BitmapFactory.decodeStream(
                                    this.getContentResolver().openInputStream(imageUri));
                            iv_companyBackground.setImageBitmap(bitmap);

                            final String objectName = Config.CID + ".jpg";

                            UploadListener listener = new BaseUploadListener() {

                                @Override
                                public void onUploadFailed(UploadTask uploadTask, com.alibaba.sdk.android.media.utils.FailReason failReason) {
                                    System.out.println(failReason.getMessage());

                                    Toast.makeText(Aty_PersonalTimeline.this,
                                            "图片上传失败，请重试",
                                            Toast.LENGTH_SHORT).show();

                                    ImageLoader.getInstance().displayImage(
                                            Config.COMPANY_BACKGROUND+ "?t=" + Config.TIME,
                                            iv_companyBackground);
                                }

                                @Override
                                public void onUploadComplete(UploadTask uploadTask) {
                                    Config.COMPANY_BACKGROUND = Urls.MEDIA_CENTER_BACKGROUND + objectName;

                                    // 更新时间戳
                                    Config.TIME = Calendar.getInstance().getTimeInMillis();

                                    SharedPreferences sp = getSharedPreferences(
                                            Constants.SP_FILENAME,
                                            Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND,
                                            Config.COMPANY_BACKGROUND);
                                    editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                                    editor.apply();

                                    ImageLoader.getInstance().displayImage(
                                            Config.COMPANY_BACKGROUND+ "?t=" + Config.TIME,
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
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            finish();
            overridePendingTransition(R.anim.scale_stay,
                    R.anim.out_left_right);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        deleteTaskReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < Adpt_PersonalTimeline.taskList.size(); i++) {
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

        commentReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {
                        for (int i = 0; i < Adpt_PersonalTimeline.taskList.size(); i++) {
                            Task task = Adpt_PersonalTimeline.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                Comment comment = (Comment) objects[0];

                                if (task.getComments().get(task.getComments().size() - 1).getTime()
                                        != comment.getTime()) {

                                    Adpt_PersonalTimeline.taskList.get(i).getComments().add(comment);
                                    adapter.notifyDataSetChanged();
                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    break;
                                }
                            }
                        }
                    }
                });
        commentReceiver.registerAction(Constants.ACTION_SEND_COMMENT);

        nicknameReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_title.setText(Config.NICKNAME);
                        tv_nickname.setText(Config.NICKNAME);
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        portraitReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.PORTRAIT+ "?t=" + Config.TIME, iv_portrait);
                    }
                });
        portraitReceiver.registerAction(Constants.ACTION_CHANGE_PORTRAIT);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(deleteTaskReceiver);
        unregisterReceiver(commentReceiver);
        unregisterReceiver(nicknameReceiver);
        unregisterReceiver(portraitReceiver);

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
