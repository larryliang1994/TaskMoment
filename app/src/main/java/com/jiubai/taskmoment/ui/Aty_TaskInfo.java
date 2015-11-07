package com.jiubai.taskmoment.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_Comment;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.adapter.Adpt_TimelinePicture;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 任务详情页面
 */
public class Aty_TaskInfo extends AppCompatActivity {
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

    @Bind(R.id.tv_date)
    TextView tv_date;

    @Bind(R.id.tv_delete)
    TextView tv_delete;

    @Bind(R.id.tv_audit_result)
    TextView tv_audit_result;

    @Bind(R.id.tv_space_comment)
    TextView tv_space_comment;

    @Bind(R.id.tv_space_audit)
    TextView tv_space_audit;

    private static LinearLayout ll_comment;
    private static LinearLayout ll_audit;
    private static ListView lv_comment;
    private static ScrollView sv_taskInfo;
    private static Space space;
    private Task task;
    private static Adpt_Comment adapter_comment;
    private static boolean commentWindowIsShow = false;
    private static boolean auditWindowIsShow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_taskinfo);

        ButterKnife.bind(this);

        task = (Task) getIntent().getSerializableExtra("task");

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText("任务详情");

        ImageLoader.getInstance().displayImage(task.getPortraitUrl(), iv_portrait);

        tv_desc.setText(task.getDesc());

        tv_grade.setText(task.getGrade());
        setGradeColor(tv_grade, task.getGrade());

        tv_nickname.setText(task.getNickname());

        tv_audit_result.setText(task.getAuditResult());

        gv_picture.setAdapter(new Adpt_TimelinePicture(this, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(gv_picture, true);

        if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
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

            if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
                UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                    @Override
                    public void successCallback() {
                        setAuditResult();
                    }

                    @Override
                    public void failedCallback() {
                    }
                });
            } else {
                setAuditResult();
            }
        }

        tv_date.setText(UtilBox.getDateToString(task.getCreate_time(), UtilBox.DATE));

        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        lv_comment = (ListView) findViewById(R.id.lv_comment);
        sv_taskInfo = (ScrollView) findViewById(R.id.sv_taskInfo);
        space = (Space) findViewById(R.id.space);

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

        if (!Config.MID.equals(task.getMid())) {
            tv_delete.setVisibility(View.GONE);
        } else {
            tv_delete.setVisibility(View.VISIBLE);

            tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_delete.setBackgroundColor(
                            ContextCompat.getColor(Aty_TaskInfo.this, R.color.gray));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tv_delete.setBackgroundColor(
                                    ContextCompat.getColor(Aty_TaskInfo.this, R.color.transparent));
                        }
                    }, 100);

                    final MaterialDialog dialog = new MaterialDialog(Aty_TaskInfo.this);
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
                                        Toast.makeText(Aty_TaskInfo.this,
                                                R.string.cant_access_network,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String[] key = {"taskid"};
                                    String[] value = {task.getId()};
                                    VolleyUtil.requestWithCookie(Urls.TASK_DELETE, key, value,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(response);

                                                        if ("900001".equals(jsonObject.getString("status"))) {
                                                            dialog.dismiss();

                                                            Intent intent = new Intent(Constants.ACTION_DELETE_TASK);
                                                            intent.putExtra("taskID", task.getId());

                                                            Aty_TaskInfo.this.sendBroadcast(intent);

                                                            Aty_TaskInfo.this.finish();
                                                            overridePendingTransition(R.anim.in_left_right,
                                                                    R.anim.out_left_right);
                                                        } else {
                                                            Toast.makeText(Aty_TaskInfo.this,
                                                                    jsonObject.getString("info"),
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

                                                    Toast.makeText(Aty_TaskInfo.this,
                                                            R.string.usual_error,
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
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
            adapter_comment = new Adpt_Comment(this, task.getComments(), "taskInfo");
            lv_comment.setVisibility(View.VISIBLE);
            lv_comment.setAdapter(adapter_comment);
            UtilBox.setListViewHeightBasedOnChildren(lv_comment);
        } else {
            lv_comment.setVisibility(View.GONE);
        }
    }

    /**
     * 设置审核结果
     */
    private void setAuditResult() {
        switch (task.getAuditResult()) {
            case "2":
                tv_audit_result.setText(R.string.solved);
                break;

            case "3":
                tv_audit_result.setText(R.string.complete);
                break;

            case "4":
                tv_audit_result.setText(R.string.task_failed);
                break;
        }
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
        for (int i = 1; i < Adpt_Member.memberList.size() - 1; i++) {
            Member member = Adpt_Member.memberList.get(i);

            if (executorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    executor = member.getName();
                } else {
                    executor = member.getMobile().substring(0, 3)
                            + "***" + member.getMobile().substring(7);
                }
            }

            if (supervisorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    supervisor = member.getName();
                } else {
                    supervisor = member.getMobile().substring(0, 3)
                            + "***" + member.getMobile().substring(7);
                }
            }

            if (auditorID.equals(member.getMid())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    auditor = member.getName();
                } else {
                    auditor = member.getMobile().substring(0, 3)
                            + "***" + member.getMobile().substring(7);
                }
            }

            if (executor != null && supervisor != null && auditor != null) {
                break;
            }
        }

        if (executor != null) {
            tv_executor.append("：");
            tv_executor.append(executor);
        }

        if (supervisor != null) {
            tv_supervisor.append("：");
            tv_supervisor.append(supervisor);
        }

        if (auditor != null) {
            tv_auditor.append("：");
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
                overridePendingTransition(R.anim.in_left_right,
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
                Intent intent = new Intent(this, Aty_PersonalTimeline.class);
                intent.putExtra("mid", task.getMid());
                startActivity(intent);
                overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
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
        edt_content.setText(null);
        edt_content.requestFocus();

        UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 360 + 48));

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int keyBoardHeight = UtilBox.dip2px(context, 360);
                int viewHeight = UtilBox.getHeightPixels(context) - y;

                int finalScroll = keyBoardHeight - viewHeight
                        + sv_taskInfo.getScrollY() + UtilBox.dip2px(context, 48);

                sv_taskInfo.smoothScrollTo(0, finalScroll);
            }
        });

        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }

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
                                                R.string.usual_error,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        String nickname;
                                        if("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)){
                                            nickname = "你";
                                        } else {
                                            nickname = Config.NICKNAME;
                                        }

                                        Comment comment;
                                        if (!"".equals(receiver)) {
                                            comment = new Comment(taskID,
                                                    nickname, Config.MID,
                                                    receiver, receiverID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis());
                                            adapter_comment.commentList.add(comment);
                                        } else {
                                            comment = new Comment(taskID,
                                                    nickname, Config.MID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis());
                                            adapter_comment.commentList.add(comment);
                                        }

                                        adapter_comment = new Adpt_Comment(context,
                                                adapter_comment.commentList, "taskInfo");
                                        lv_comment.setAdapter(adapter_comment);
                                        UtilBox.setListViewHeightBasedOnChildren(lv_comment);

                                        // 发送更新评论广播
                                        Intent intent = new Intent(Constants.ACTION_SEND_COMMENT);
                                        intent.putExtra("taskID", taskID);
                                        intent.putExtra("comment", comment);
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
                                        R.string.usual_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
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

                                        btn_audit.setVisibility(View.GONE);

                                        tv_audit_result.setVisibility(View.VISIBLE);

                                        task.setAuditResult(audit_result[0] + "");

                                        if (Adpt_Member.memberList == null
                                                || Adpt_Member.memberList.isEmpty()) {
                                            UtilBox.getMember(context,
                                                    new UtilBox.GetMemberCallBack() {
                                                        @Override
                                                        public void successCallback() {
                                                            setAuditResult();
                                                        }

                                                        @Override
                                                        public void failedCallback() {
                                                        }
                                                    });
                                        } else {
                                            setAuditResult();
                                        }

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

                                Toast.makeText(context, R.string.usual_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
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
                overridePendingTransition(R.anim.in_left_right,
                        R.anim.out_left_right);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
