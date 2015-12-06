package com.jiubai.taskmoment.view.activity;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.adapter.PersonalTimelineAdapter;
import com.jiubai.taskmoment.adapter.TimelineAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.widget.BorderScrollView;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITimelinePresenter;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.TimelinePresenterImpl;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.view.iview.IAuditView;
import com.jiubai.taskmoment.view.iview.ICommentView;
import com.jiubai.taskmoment.view.iview.ITimelineView;
import com.jiubai.taskmoment.view.iview.IUploadImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人情况
 */
public class PersonalTimelineActivity extends BaseActivity
        implements ITimelineView, ICommentView, IAuditView, IUploadImageView {
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
    private static PersonalTimelineAdapter adapter;
    private static int keyBoardHeight;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;
    private static ICommentPresenter commentPresenter;
    private static IAuditPresenter auditPresenter;

    private ITimelinePresenter timelinePresenter;
    private IUploadImagePresenter uploadImagePresenter;
    private View footerView;
    private String mid, name, mobile, isAudit, isInvolved;
    private boolean isBottomRefreshing = false;
    private Uri imageUri = Uri.parse(Constants.TEMP_FILE_LOCATION); // 用于存放背景图
    private UpdateViewReceiver deleteTaskReceiver, commentReceiver, auditReceiver,
            nicknameReceiver, portraitReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personal_timeline);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        isAudit = intent.getBooleanExtra("isAudit", false) ? "1" : "0";
        isInvolved = intent.getBooleanExtra("isInvolved", false) ? "1" : "0";

        initView();
    }

    /**
     * 初始化组件
     */
    @SuppressLint("InflateParams")
    private void initView() {

        if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
            UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                @Override
                public void successCallback() {
                    getUserInfo();
                }

                @Override
                public void failedCallback() {
                }
            });
        } else {
            getUserInfo();
        }

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
                    refreshTimeline("loadMore", (PersonalTimelineAdapter.taskList
                            .get(PersonalTimelineAdapter.taskList.size() - 1)
                            .getCreateTime() / 1000 - 1) + "");
                }
            }
        });

        final TextView tv_addBackground = (TextView) findViewById(R.id.tv_add_background);

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

        commentPresenter = new CommentPresenterImpl(this, this);
        auditPresenter = new AuditPresenterImpl(this, this);
        timelinePresenter = new TimelinePresenterImpl(this);
        uploadImagePresenter = new UploadImagePresenterImpl(this, this);

        refreshTimeline("refresh", Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
    }

    /**
     * 初始化头像
     */
    private void initPortrait() {
        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        ImageLoader.getInstance().displayImage(
                Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg" + "?t=" + Config.TIME, iv_portrait);
        iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonalTimelineActivity.this, PersonalInfoActivity.class);

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

        timelinePresenter.doPullTimeline(request_time, type, mid, isAudit, isInvolved);
    }

    /**
     * 设置适配器，并重新设置ListView的高度
     *
     * @param type 刷新类型
     */
    private void resetListViewHeight(final String type) {
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
                            svHeight - UtilBox.dip2px(PersonalTimelineActivity.this, 312)
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
        for (int i = 1; i < MemberAdapter.memberList.size() - 1; i++) {
            Member member = MemberAdapter.memberList.get(i);
            if (mid.equals(member.getMid())) {
                name = member.getName();
                mobile = member.getMobile();
                break;
            }
        }

        if (!"null".equals(name) && !"".equals(name)) {
            tv_title.setText(name);
            tv_nickname.setText(name);
        } else {
            tv_title.setText(mobile);
            tv_nickname.setText(mobile);
        }

        initPortrait();
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
                                UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 54) + keyBoardHeight);

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

                commentPresenter.doSendComment(taskID, receiver, receiverID,
                        edt_content.getText().toString());
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
                            Toast.makeText(PersonalTimelineActivity.this,
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

                        int standardWidth = UtilBox.getWidthPixels(PersonalTimelineActivity.this);
                        int standardHeight = UtilBox.dip2px(PersonalTimelineActivity.this, 270);

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
                if (resultCode == RESULT_OK) {
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
        }
    }

    @Override
    protected void onStart() {
        deleteTaskReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {

                        for (int i = 0; i < PersonalTimelineAdapter.taskList.size(); i++) {
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

        commentReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... objects) {
                        for (int i = 0; i < PersonalTimelineAdapter.taskList.size(); i++) {
                            Task task = PersonalTimelineAdapter.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                Comment comment = (Comment) objects[0];

                                if (task.getComments().get(task.getComments().size() - 1).getTime()
                                        != comment.getTime()) {

                                    PersonalTimelineAdapter.taskList.get(i).getComments().add(comment);
                                    adapter.notifyDataSetChanged();
                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    break;
                                }
                            }
                        }
                    }
                });
        commentReceiver.registerAction(Constants.ACTION_SEND_COMMENT);

        auditReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String taskID, Object... object) {
                        for (int i = 0; i < PersonalTimelineAdapter.taskList.size(); i++) {
                            Task task = PersonalTimelineAdapter.taskList.get(i);
                            if (task.getId().equals(taskID)) {
                                task.setAuditResult((String) object[0]);
                                PersonalTimelineAdapter.taskList.set(i, task);

                                adapter.notifyDataSetChanged();
                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                break;
                            }
                        }
                    }
                });
        auditReceiver.registerAction(Constants.ACTION_AUDIT);

        nicknameReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_title.setText(Config.NICKNAME);
                        tv_nickname.setText(Config.NICKNAME);
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        portraitReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
                    }
                });
        portraitReceiver.registerAction(Constants.ACTION_CHANGE_PORTRAIT);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(deleteTaskReceiver);
        unregisterReceiver(commentReceiver);
        unregisterReceiver(auditReceiver);
        unregisterReceiver(nicknameReceiver);
        unregisterReceiver(portraitReceiver);

        super.onDestroy();
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);
        }

        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendCommentResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            //Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
        } else if (Constants.FAILED.equals(result)) {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPullTimelineResult(String result, final String type, String info) {
        switch (result) {
            case Constants.SUCCESS:
                // 先实例出适配器
                if ("refresh".equals(type)) {
                    adapter = new PersonalTimelineAdapter(this, true, info);
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    adapter = new PersonalTimelineAdapter(this, false, info);
                }

                // 设置适配器，并重新设置ListView的高度
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        resetListViewHeight(type);
                    }
                });
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

                Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
                break;

            case Constants.FAILED:
            default:
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    isBottomRefreshing = false;
                }

                Toast.makeText(PersonalTimelineActivity.this, info, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onGetNewsResult(int result, News news) {

    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        if (visibility == Constants.VISIBLE) {
            tv_loading.setVisibility(View.VISIBLE);
        } else if (visibility == Constants.INVISIBLE) {
            tv_loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);
        } else if (Constants.FAILED.equals(result)) {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();

            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}