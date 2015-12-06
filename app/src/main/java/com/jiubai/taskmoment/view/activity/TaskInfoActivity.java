package com.jiubai.taskmoment.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.CommentAdapter;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.adapter.TimelinePictureAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.BorderScrollView;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.view.iview.IAuditView;
import com.jiubai.taskmoment.view.iview.ICommentView;
import com.jiubai.taskmoment.view.iview.ITaskView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 任务详情页面
 */
public class TaskInfoActivity extends BaseActivity implements ICommentView, IAuditView, ITaskView {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iv_portrait)
    ImageView iv_portrait;

    @Bind(R.id.btn_comment)
    Button btn_comment;

    @Bind(R.id.btn_audit)
    Button btn_audit;

    @Bind(R.id.tv_desc)
    TextView tv_desc;

    @Bind(R.id.tv_grade)
    TextView tv_grade;

    @Bind(R.id.tv_nickname)
    TextView tv_nickname;

    @Bind(R.id.gv_picture)
    GridView gv_picture;

    @Bind(R.id.tv_executor)
    TextView tv_executor;

    @Bind(R.id.tv_supervisor)
    TextView tv_supervisor;

    @Bind(R.id.tv_auditor)
    TextView tv_auditor;

    @Bind(R.id.tv_publishTime)
    TextView tv_publishTime;

    @Bind(R.id.tv_deadline)
    TextView tv_deadline;

    @Bind(R.id.tv_startTime)
    TextView tv_startTime;

    @Bind(R.id.tv_delete)
    TextView tv_delete;

    @Bind(R.id.tv_audit_result)
    TextView tv_audit_result;

    @Bind(R.id.tv_space_comment)
    TextView tv_space_comment;

    @Bind(R.id.tv_space_audit)
    TextView tv_space_audit;

    @Bind(R.id.rl_taskInfo)
    RelativeLayout rl_taskInfo;

    private static LinearLayout ll_comment;
    private static LinearLayout ll_audit;
    private static ListView lv_comment;
    private static BorderScrollView sv_taskInfo;
    private static int keyBoardHeight;
    private static Space space;
    private Task task;
    private static CommentAdapter adapter_comment;
    private static boolean commentWindowIsShow = false;
    private static boolean auditWindowIsShow = false;
    private static ICommentPresenter commentPresenter;
    private ITaskPresenter taskPresenter;
    private IAuditPresenter auditPresenter;
    private UpdateViewReceiver commentReceiver, auditReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_taskinfo);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

        ButterKnife.bind(this);

        task = (Task) getIntent().getSerializableExtra("task");

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText("任务详情");

        ImageLoader.getInstance().displayImage(task.getPortraitUrl() + "?t=" + Config.TIME, iv_portrait);
        iv_portrait.requestFocus();

        tv_desc.setText(task.getDesc());
        tv_nickname.setText(task.getNickname());
        tv_audit_result.setText(task.getAuditResult());
        tv_grade.setText(task.getGrade());
        setGradeColor(tv_grade, task.getGrade());

        tv_deadline.append(UtilBox.getDateToString(task.getDeadline(), UtilBox.DATE_TIME));
        tv_startTime.append(UtilBox.getDateToString(task.getStartTime(), UtilBox.DATE_TIME));
        tv_publishTime.append(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        gv_picture.setAdapter(new TimelinePictureAdapter(this, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(gv_picture, true);

        if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
            UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                @Override
                public void successCallback() {
                    setESA();
                }

                @Override
                public void failedCallback() {
                }
            });
        } else {
            setESA();
        }

        if ("1".equals(task.getAuditResult()) || "null".equals(task.getAuditResult())) {
            tv_audit_result.setVisibility(View.GONE);
        } else {
            tv_audit_result.setVisibility(View.VISIBLE);

            if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
                UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                    @Override
                    public void successCallback() {
                        tv_audit_result.setText(
                                Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);
                    }

                    @Override
                    public void failedCallback() {
                    }
                });
            } else {
                tv_audit_result.setText(
                        Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);
            }
        }

        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        lv_comment = (ListView) findViewById(R.id.lv_comment);
        sv_taskInfo = (BorderScrollView) findViewById(R.id.sv_taskInfo);
        space = (Space) findViewById(R.id.space);

        tv_space_comment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (commentWindowIsShow) {
                    UtilBox.setViewParams(space, 1, 1);

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

        if (!Config.MID.equals(task.getMid())) {
            tv_delete.setVisibility(View.GONE);
        } else {
            tv_delete.setVisibility(View.VISIBLE);

            tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_delete.setBackgroundColor(
                            ContextCompat.getColor(TaskInfoActivity.this, R.color.gray));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tv_delete.setBackgroundColor(
                                    ContextCompat.getColor(TaskInfoActivity.this, R.color.transparent));
                        }
                    }, 100);

                    final MaterialDialog dialog = new MaterialDialog(TaskInfoActivity.this);
                    dialog.setMessage("真的要删除吗")
                            .setCanceledOnTouchOutside(true)
                            .setNegativeButton("假的", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("真的", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!Config.IS_CONNECTED) {
                                        Toast.makeText(TaskInfoActivity.this,
                                                R.string.cant_access_network,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    dialog.dismiss();
                                    taskPresenter.doDeleteTask(TaskInfoActivity.this, task.getId());


                                }
                            })
                            .show();
                }
            });
        }

        if (!Config.MID.equals(task.getAuditor()) || !"1".equals(task.getAuditResult())) {
            btn_audit.setVisibility(View.GONE);
        }

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            adapter_comment = new CommentAdapter(this, task.getComments(), "taskInfo");
            lv_comment.setVisibility(View.VISIBLE);
            lv_comment.setAdapter(adapter_comment);
            UtilBox.setListViewHeightBasedOnChildren(lv_comment);
        } else {
            lv_comment.setVisibility(View.GONE);
        }

        rl_taskInfo.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        rl_taskInfo.getWindowVisibleDisplayFrame(r);

                        int screenHeight = rl_taskInfo.getRootView().getHeight();
                        int difference = screenHeight - (r.bottom - r.top);

                        if (difference > 100) {
                            keyBoardHeight = difference;
                        }
                    }
                });

        commentPresenter = new CommentPresenterImpl(this, this);
        auditPresenter = new AuditPresenterImpl(this, this);
        taskPresenter = new TaskPresenterImpl(this);
    }

    /**
     * 设置执行者，监督者，审核者
     */
    private void setESA() {
        String executorID = task.getExecutor();
        String supervisorID = task.getSupervisor();
        String auditorID = task.getAuditor();

        String executor = null, supervisor = null, auditor = null;

        // TODO 这里应该可以更高效
        for (int i = 1; i < MemberAdapter.memberList.size() - 1; i++) {
            Member member = MemberAdapter.memberList.get(i);

            if (executorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    executor = member.getName();
                } else {
                    executor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
            }

            if (supervisorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    supervisor = member.getName();
                } else {
                    supervisor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
            }

            if (auditorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    auditor = member.getName();
                } else {
                    auditor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
            }

            if (executor != null && supervisor != null && auditor != null) {
                break;
            }
        }

        if (executor != null) {
            tv_executor.append(executor);
        }

        if (supervisor != null) {
            tv_supervisor.append(supervisor);
        }

        if (auditor != null) {
            tv_auditor.append(auditor);
        }
    }

    /**
     * 设置任务等级的颜色
     *
     * @param tv_grade 需要设置的TextView
     * @param grade    级别
     */
    private void setGradeColor(TextView tv_grade, String grade) {
        switch (grade) {
            case "S":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.D));
                break;
        }
    }

    @OnClick({R.id.iBtn_back, R.id.btn_audit, R.id.btn_comment, R.id.tv_nickname, R.id.iv_portrait})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.scale_stay,
                        R.anim.out_left_right);
                break;

            case R.id.btn_audit:
                showAuditWindow(this, task.getId());
                break;

            case R.id.btn_comment:
                int[] location = new int[2];
                btn_comment.getLocationOnScreen(location);
                int y = location[1];

                showCommentWindow(this, task.getId(), "", "",
                        y + UtilBox.dip2px(this, 15));
                break;

            case R.id.tv_nickname:
            case R.id.iv_portrait:
                Intent intent = new Intent(this, PersonalTimelineActivity.class);
                intent.putExtra("mid", task.getMid());
                startActivity(intent);
                overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;
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
                                UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 54) + keyBoardHeight);

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int viewHeight = UtilBox.getHeightPixels(context) - y;

                                        int finalScroll = keyBoardHeight - viewHeight
                                                + sv_taskInfo.getScrollY() + UtilBox.dip2px(context, 54);

                                        sv_taskInfo.smoothScrollTo(0, finalScroll);
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

                UtilBox.setViewParams(space, 1, 1);

                UtilBox.toggleSoftInput(ll_comment, false);

                commentPresenter.doSendComment(taskID, receiver, receiverID,
                        edt_content.getText().toString());
            }
        });
    }

    /**
     * 弹出审核窗口
     */
    @SuppressWarnings("unused")
    public void showAuditWindow(final Context context, final String taskID) {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;

            UtilBox.setViewParams(space, 0, 0);
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
    protected void onStart() {
        super.onStart();

        commentReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... object) {
                        List<Comment> list = adapter_comment.commentList;
                        list.add((Comment) object[0]);

                        lv_comment.setAdapter(new CommentAdapter(TaskInfoActivity.this, list, "taskInfo"));

                        UtilBox.setListViewHeightBasedOnChildren(lv_comment);
                    }
                });
        commentReceiver.registerAction(Constants.ACTION_SEND_COMMENT);

        auditReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... object) {
                        tv_audit_result.setVisibility(View.VISIBLE);

                        task.setAuditResult((String) object[0]);

                        tv_audit_result.setText(
                                Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);

                    }
                });
        auditReceiver.registerAction(Constants.ACTION_AUDIT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(commentReceiver);
        unregisterReceiver(auditReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (commentWindowIsShow) {
                UtilBox.setViewParams(space, 1, 0);
                ll_comment.setVisibility(View.GONE);
                commentWindowIsShow = false;
                UtilBox.toggleSoftInput(ll_comment, false);
            } else if (auditWindowIsShow) {
                ll_audit.setVisibility(View.GONE);
                auditWindowIsShow = false;
            } else {
                finish();
                overridePendingTransition(R.anim.scale_stay,
                        R.anim.out_left_right);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);

            btn_audit.setVisibility(View.GONE);
        }

        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendCommentResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){

        } else if (Constants.FAILED.equals(result)) {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPublishTaskResult(String result, String info) {

    }

    @Override
    public void onDeleteTaskResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            finish();
            overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
        } else {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateTaskResult(String result, String info) {

    }
}