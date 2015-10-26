package com.jiubai.taskmoment.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_Comment;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.adapter.Adpt_TimelinePicture;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
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

/**
 * 任务详情页面
 */
public class Aty_TaskInfo extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iv_item_portrait)
    ImageView iv_portrait;

    @Bind(R.id.btn_comment)
    Button btn_comment;

    @Bind(R.id.tv_item_desc)
    TextView tv_desc;

    @Bind(R.id.tv_item_grade)
    TextView tv_grade;

    @Bind(R.id.tv_item_nickname)
    TextView tv_nickname;

    @Bind(R.id.gv_item_picture)
    GridView gv_picture;

    @Bind(R.id.tv_executor)
    TextView tv_executor;

    @Bind(R.id.tv_supervisor)
    TextView tv_supervisor;

    @Bind(R.id.tv_auditor)
    TextView tv_auditor;

    @Bind(R.id.tv_item_date)
    TextView tv_date;

    private static LinearLayout ll_audit;
    private static LinearLayout ll_comment;
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

        setContentView(R.layout.aty_taskinfo);

        ButterKnife.bind(this);

        String taskID = getIntent().getStringExtra("taskID");
        for (int i = 0; i < Adpt_Timeline.taskList.size(); i++) {
            task = Adpt_Timeline.taskList.get(i);
            if (taskID.equals(task.getId())) {
                break;
            }
        }

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

        tv_date.setText(UtilBox.getDateToString(task.getCreate_time(), UtilBox.DATE));

        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        lv_comment = (ListView) findViewById(R.id.lv_comment);
        sv_taskInfo = (ScrollView) findViewById(R.id.sv_taskInfo);
        space = (Space) findViewById(R.id.space);

        sv_taskInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (commentWindowIsShow) {
                    UtilBox.setViewParams(space, 0, 0);

                    ll_comment.setVisibility(View.GONE);
                    commentWindowIsShow = false;

                    // 关闭键盘
                    UtilBox.toggleSoftInput(ll_comment, false);
                }

                if (auditWindowIsShow) {
                    ll_audit.setVisibility(View.GONE);

                    auditWindowIsShow = false;
                }
                return false;
            }
        });

        adapter_comment = new Adpt_Comment(this, task.getComments(), "taskInfo");
        lv_comment.setAdapter(adapter_comment);
        UtilBox.setListViewHeightBasedOnChildren(lv_comment);
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

            if (executorID.equals(member.getId())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    executor = member.getName();
                } else {
                    executor = member.getMobile();
                }
            }

            if (supervisorID.equals(member.getId())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    supervisor = member.getName();
                } else {
                    supervisor = member.getMobile();
                }
            }

            if (auditorID.equals(member.getId())) {
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    auditor = member.getName();
                } else {
                    auditor = member.getMobile();
                }
            }

            if (executor != null && supervisor != null && auditor != null) {
                break;
            }
        }

        tv_executor.append("：");
        tv_executor.append(executor);

        tv_supervisor.append("：");
        tv_supervisor.append(supervisor);

        tv_auditor.append("：");
        tv_auditor.append(auditor);
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
                tv_grade.setTextColor(getResources().getColor(R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(getResources().getColor(R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(getResources().getColor(R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(getResources().getColor(R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(getResources().getColor(R.color.D));
                break;
        }
    }

    @OnClick({R.id.iBtn_back, R.id.btn_audit, R.id.btn_comment})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.in_left_right,
                        R.anim.out_left_right);
                break;

            case R.id.btn_audit:
                showAuditWindow();
                break;

            case R.id.btn_comment:
                int[] location = new int[2];
                btn_comment.getLocationOnScreen(location);
                int y = location[1];

                showCommentWindow(this, task.getId(), "", "",
                        y + UtilBox.dip2px(this, 15));
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
        edt_content.requestFocus();

        UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 360 + 56));

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int keyBoardHeight = UtilBox.dip2px(context, 360);
                int viewHeight = UtilBox.getHeightPixels(context) - y;

                int finalScroll = keyBoardHeight - viewHeight
                        + sv_taskInfo.getScrollY() + UtilBox.dip2px(context, 56);

                sv_taskInfo.smoothScrollTo(0, finalScroll);
            }
        });

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
                if (!Config.IS_CONNECTED) {
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
                                        if (!"".equals(receiver)) {
                                            adapter_comment.commentList.add(new Comment(
                                                    taskID, 0, Config.NICKNAME, Config.MID,
                                                    receiver, receiverID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis()));
                                        } else {
                                            adapter_comment.commentList.add(new Comment(
                                                    taskID, 0, Config.NICKNAME, Config.MID,
                                                    edt_content.getText().toString(),
                                                    Calendar.getInstance(Locale.CHINA)
                                                            .getTimeInMillis()));
                                        }

                                        adapter_comment = new Adpt_Comment(context,
                                                adapter_comment.commentList, "taskInfo");
                                        lv_comment.setAdapter(adapter_comment);
                                        UtilBox.setListViewHeightBasedOnChildren(lv_comment);
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
    public static void showAuditWindow() {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;

            UtilBox.setViewParams(space, 0, 0);
        }

        ll_audit.setVisibility(View.VISIBLE);

        ((RadioButton) ll_audit.findViewById(R.id.rb_complete)).setChecked(true);

        Button btn_send = (Button) ll_audit.findViewById(R.id.btn_audit_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_audit.setVisibility(View.GONE);
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
