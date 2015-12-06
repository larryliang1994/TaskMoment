package com.jiubai.taskmoment.view.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.adapter.PublishPictureAdapter;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.bean.MyDate;
import com.jiubai.taskmoment.bean.MyTime;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.DateDialog;
import com.jiubai.taskmoment.widget.RippleView;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.widget.TimeDialog;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.view.iview.ITaskView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * 发布任务
 */
public class TaskPublishActivity extends BaseActivity implements ITaskView,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    @Bind(R.id.tv_space)
    TextView tv_space;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_deadline)
    TextView tv_deadline;

    @Bind(R.id.tv_publishTime)
    TextView tv_publishTime;

    @Bind(R.id.edt_desc)
    EditText edt_desc;

    @Bind(R.id.rv_btn_publish)
    RippleView rv_btn_publish;

    @Bind(R.id.btn_publish)
    Button btn_publish;

    @Bind(R.id.gv_publish)
    GridView gv;

    @Bind(R.id.btn_grade_s)
    Button btn_s;

    @Bind(R.id.btn_grade_a)
    Button btn_a;

    @Bind(R.id.btn_grade_b)
    Button btn_b;

    @Bind(R.id.btn_grade_c)
    Button btn_c;

    @Bind(R.id.btn_grade_d)
    Button btn_d;

    private ITaskPresenter taskPresenter;
    private int grade = 4;
    private String date;
    private List<Button> gradeBtnList = new ArrayList<>();
    private boolean isDeadline = false, isStartTime = false;
    private PublishPictureAdapter adpt_publishPicture;
    private int executor = -1, supervisor = -1, auditor = -1;
    private int year_deadline = 0, month_deadline = 0, day_deadline = 0,
            hour_deadline = 0, minute_deadline = 0,
            year_startTime = 0, month_startTime = 0, day_startTime = 0,
            hour_startTime = 0, minute_startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_task_publish);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        tv_title.setText(R.string.taskPublish);

        gradeBtnList.add(btn_s);
        gradeBtnList.add(btn_a);
        gradeBtnList.add(btn_b);
        gradeBtnList.add(btn_c);
        gradeBtnList.add(btn_d);

        // 默认为C级
        changeBtnColor("");
        GradientDrawable gradeBgShape = (GradientDrawable) btn_c.getBackground();
        gradeBgShape.setColor(ContextCompat.getColor(this, R.color.C));

        adpt_publishPicture = new PublishPictureAdapter(this, new ArrayList<String>());
        gv.setAdapter(adpt_publishPicture);

        GradientDrawable publishBackground = (GradientDrawable) btn_publish.getBackground();
        publishBackground.setColor(ContextCompat.getColor(this, R.color.primary));

        tv_space.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UtilBox.toggleSoftInput(edt_desc, false);

                return false;
            }
        });

        taskPresenter = new TaskPresenterImpl(this);

        rv_btn_publish.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(TaskPublishActivity.this,
                            R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                } else if (edt_desc.getText().toString().length() == 0) {
                    Toast.makeText(TaskPublishActivity.this, "请填入任务描述", Toast.LENGTH_SHORT).show();
                } else if (executor == -1) {
                    Toast.makeText(TaskPublishActivity.this, "请选择执行者", Toast.LENGTH_SHORT).show();
                } else if (supervisor == -1) {
                    Toast.makeText(TaskPublishActivity.this, "请选择监督者", Toast.LENGTH_SHORT).show();
                } else if (auditor == -1) {
                    Toast.makeText(TaskPublishActivity.this, "请选择审核者", Toast.LENGTH_SHORT).show();
                } else if (year_deadline == 0) {
                    Toast.makeText(TaskPublishActivity.this, "请填入完成期限", Toast.LENGTH_SHORT).show();
                } else if (year_startTime == 0) {
                    Toast.makeText(TaskPublishActivity.this, "请填入开始时间", Toast.LENGTH_SHORT).show();
                } else {
                    // 先把多余的添加图片入口删掉
                    if (adpt_publishPicture.pictureList != null
                            && !adpt_publishPicture.pictureList.isEmpty()
                            && adpt_publishPicture.actualCount < 9) {
                        adpt_publishPicture.pictureList.remove(
                                adpt_publishPicture.pictureList.size() - 1);
                    }

                    taskPresenter.doPublishTask(
                            String.valueOf(grade),
                            edt_desc.getText().toString(),
                            MemberAdapter.memberList.get(executor).getMid(),
                            MemberAdapter.memberList.get(supervisor).getMid(),
                            MemberAdapter.memberList.get(auditor).getMid(),
                            UtilBox.getStringToDate(tv_deadline.getText().toString()) / 1000 + "",
                            UtilBox.getStringToDate(tv_publishTime.getText().toString()) / 1000 + ""
                    );
                }
            }
        });
    }

    /**
     * 设置返回结果并关掉当前aty
     */
    private void setResultAndFinish(String taskID) {
        Intent intent = new Intent();
        intent.putExtra("grade", gradeBtnList.get(grade - 1).getText().toString());
        intent.putExtra("content", edt_desc.getText().toString());
        intent.putExtra("pictureList", adpt_publishPicture.pictureList);
        intent.putExtra("executor", MemberAdapter.memberList.get(executor).getMid());
        intent.putExtra("supervisor", MemberAdapter.memberList.get(supervisor).getMid());
        intent.putExtra("auditor", MemberAdapter.memberList.get(auditor).getMid());
        intent.putExtra("taskID", taskID);
        intent.putExtra("deadline",
                UtilBox.getStringToDate(tv_deadline.getText().toString()));
        intent.putExtra("publish_time",
                UtilBox.getStringToDate(tv_publishTime.getText().toString()));
        intent.putExtra("create_time",
                Calendar.getInstance(Locale.CHINA).getTimeInMillis());

        TaskPublishActivity.this.setResult(RESULT_OK, intent);
        TaskPublishActivity.this.finish();
        overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
    }

    @OnClick({R.id.iBtn_back, R.id.tv_deadline, R.id.tv_publishTime,
            R.id.tv_executor, R.id.tv_supervisor, R.id.tv_auditor,
            R.id.btn_grade_s, R.id.btn_grade_a, R.id.btn_grade_b,
            R.id.btn_grade_c, R.id.btn_grade_d})
    public void onClick(View view) {
        MyDate myDate;
        DateDialog dateDialog;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("加载中...");

        switch (view.getId()) {
            case R.id.iBtn_back:
                TaskPublishActivity.this.setResult(RESULT_CANCELED);
                TaskPublishActivity.this.finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
                break;

            case R.id.tv_deadline:
                isDeadline = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.tv_publishTime:
                isStartTime = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.tv_executor:
                if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
                    progressDialog.show();

                    UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                        @Override
                        public void successCallback() {
                            progressDialog.dismiss();
                            showMemberList(R.id.tv_executor, "executor");
                        }

                        @Override
                        public void failedCallback() {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    showMemberList(R.id.tv_executor, "executor");
                }
                break;

            case R.id.tv_supervisor:
                if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
                    progressDialog.show();

                    UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                        @Override
                        public void successCallback() {
                            progressDialog.dismiss();
                            showMemberList(R.id.tv_supervisor, "supervisor");
                        }

                        @Override
                        public void failedCallback() {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    showMemberList(R.id.tv_supervisor, "supervisor");
                }
                break;

            case R.id.tv_auditor:
                if (MemberAdapter.memberList == null || MemberAdapter.memberList.isEmpty()) {
                    progressDialog.show();

                    UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                        @Override
                        public void successCallback() {
                            progressDialog.dismiss();
                            showMemberList(R.id.tv_auditor, "auditor");
                        }

                        @Override
                        public void failedCallback() {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    showMemberList(R.id.tv_auditor, "auditor");
                }
                break;

            case R.id.btn_grade_s:
                grade = 1;
                changeBtnColor("S");
                break;

            case R.id.btn_grade_a:
                grade = 2;
                changeBtnColor("A");
                break;

            case R.id.btn_grade_b:
                grade = 3;
                changeBtnColor("B");
                break;

            case R.id.btn_grade_c:
                grade = 4;
                changeBtnColor("C");
                break;

            case R.id.btn_grade_d:
                grade = 5;
                changeBtnColor("D");
                break;
        }
    }

    /**
     * 弹出成员选择对话框
     * 出现的+1-1都是因为memberList有空白的头尾
     *
     * @param viewID   显示控件的Id
     * @param whichExt 正在选择哪个
     */
    private void showMemberList(final int viewID, final String whichExt) {
        if (MemberAdapter.isEmpty) {
            Toast.makeText(this, "暂无成员", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] members = new String[MemberAdapter.memberList.size() - 2];
        for (int i = 1; i < MemberAdapter.memberList.size() - 1; i++) {

            Member member = MemberAdapter.memberList.get(i);

            if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                members[i - 1] = member.getName();
            } else {
                members[i - 1] = member.getMobile();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskPublishActivity.this);
        builder.setItems(members, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Member member = MemberAdapter.memberList.get(which + 1);
                if (!"null".equals(member.getName())
                        && !"".equals(member.getName())) {
                    ((TextView) TaskPublishActivity.this.findViewById(viewID))
                            .setText(member.getName());
                } else {
                    ((TextView) TaskPublishActivity.this.findViewById(viewID))
                            .setText(member.getMobile());
                }

                switch (whichExt) {
                    case "executor":
                        executor = which + 1;
                        break;

                    case "supervisor":
                        supervisor = which + 1;
                        break;

                    case "auditor":
                        auditor = which + 1;
                        break;
                }
            }
        });

        builder.setCancelable(true);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 修改分级按钮颜色
     *
     * @param which S,A,B,C,D
     */
    private void changeBtnColor(String which) {
        // 全部变成灰色
        for (int i = 0; i < gradeBtnList.size(); i++) {
            if (i + 1 != grade) {
                GradientDrawable gradeBgShape
                        = (GradientDrawable) gradeBtnList.get(i).getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.NONE));
            }
        }

        GradientDrawable gradeBgShape;
        switch (which) {
            case "S":
                gradeBgShape = (GradientDrawable) btn_s.getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.S));
                break;

            case "A":
                gradeBgShape = (GradientDrawable) btn_a.getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.A));
                break;

            case "B":
                gradeBgShape = (GradientDrawable) btn_b.getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.B));
                break;

            case "C":
                gradeBgShape = (GradientDrawable) btn_c.getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.C));
                break;

            case "D":
                gradeBgShape = (GradientDrawable) btn_d.getBackground();
                gradeBgShape.setColor(ContextCompat.getColor(this, R.color.D));
                break;
        }
    }

    /**
     * 获取日期
     *
     * @return 当前日期或已设定的日期
     */
    private MyDate getMyDate() {
        int year, month, day;

        if ((isDeadline && year_deadline != 0)
                || (isStartTime && year_deadline != 0 && year_startTime == 0)) {
            year = year_deadline;
            month = month_deadline;
            day = day_deadline;
        } else if ((isDeadline) || (isStartTime && year_startTime == 0)) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = year_startTime;
            month = month_startTime;
            day = day_startTime;
        }

        return new MyDate(year, month, day);
    }

    /**
     * 获取时间
     *
     * @return 当前时间或已设定的时间
     */
    private MyTime getMyTime() {
        int hour, minute;

        if ((isDeadline && hour_deadline != 0)
                || (isStartTime && hour_deadline != 0 && hour_startTime == 0)) {
            hour = hour_deadline;
            minute = minute_deadline;
        } else if ((isDeadline) || (isStartTime && hour_startTime == 0)) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        } else {
            hour = hour_startTime;
            minute = minute_startTime;
        }

        return new MyTime(hour, minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        date = year + "/" + (monthOfYear + 1) + "/" + dayOfMonth;

        if (isDeadline) {
            year_deadline = year;
            month_deadline = monthOfYear;
            day_deadline = dayOfMonth;
        } else if (isStartTime) {
            year_startTime = year;
            month_startTime = monthOfYear;
            day_startTime = dayOfMonth;
        }

        MyTime myTime = getMyTime();
        TimeDialog timeDialog = new TimeDialog(this, this, myTime.getHour(), myTime.getMinute(), true);
        timeDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10) {
            date += " " + hourOfDay + ":0" + minute;
        } else {
            date += " " + hourOfDay + ":" + minute;
        }

        if (isDeadline) {
            hour_deadline = hourOfDay;
            minute_deadline = minute;

            isDeadline = false;

            tv_deadline.setText(date);
        } else if (isStartTime) {
            hour_startTime = hourOfDay;
            minute_startTime = minute;

            isStartTime = false;

            tv_publishTime.setText(date);
        }

        date = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_MULTIPLE_PICTURE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> path
                            = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
                break;

            case Constants.CODE_CHECK_PICTURE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> path = data.getStringArrayListExtra("pictureList");
                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
        }
    }

    @Override
    public void onPublishTaskResult(String result, String info) {
        switch (result){
            case Constants.SUCCESS:
                // 为了能马上显示出来
                setResultAndFinish(info);
                break;

            default:
                Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDeleteTaskResult(String result, String info) {

    }

    @Override
    public void onUpdateTaskResult(String result, String info) {
        switch (result){
            case Constants.SUCCESS:
                break;

            default:
                Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}