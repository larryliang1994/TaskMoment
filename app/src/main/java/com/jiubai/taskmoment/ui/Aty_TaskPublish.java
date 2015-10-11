package com.jiubai.taskmoment.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.adapter.Adpt_PublishPicture;
import com.jiubai.taskmoment.classes.MyDate;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.view.DateDialog;
import com.jiubai.taskmoment.view.RippleView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * 发布任务
 */
public class Aty_TaskPublish extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_deadline)
    TextView tv_deadline;

    @Bind(R.id.tv_publishTime)
    TextView tv_publishTime;

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

    private int grade = 0;
    private List<Button> gradeBtnList = new ArrayList<>();
    private boolean isDeadline = false, isPublishTime = false;
    private Adpt_PublishPicture adpt_publishPicture;
    private int year_deadline = 0, month_deadline = 0, day_deadline = 0,
            year_publishTime = 0, month_publishTime = 0, day_publishTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_task_publish);

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
        gradeBgShape.setColor(getResources().getColor(R.color.C));

        adpt_publishPicture = new Adpt_PublishPicture(this, new ArrayList<String>());
        gv.setAdapter(adpt_publishPicture);

        GradientDrawable publishBackground = (GradientDrawable) btn_publish.getBackground();
        publishBackground.setColor(getResources().getColor(R.color.gray));

        rv_btn_publish.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {

            }
        });
    }

    @OnClick({R.id.iBtn_back, R.id.tv_deadline, R.id.tv_publishTime,
            R.id.btn_grade_s, R.id.btn_grade_a, R.id.btn_grade_b, R.id.btn_grade_c, R.id.btn_grade_d})
    public void onClick(View view) {
        MyDate myDate;
        DateDialog dateDialog;
        switch (view.getId()) {
            case R.id.iBtn_back:
                Aty_TaskPublish.this.finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
                break;

            case R.id.tv_deadline:
                isDeadline = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this, myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.tv_publishTime:
                isPublishTime = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this, myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
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
     * 修改分级按钮颜色
     *
     * @param which S,A,B,C,D
     */
    private void changeBtnColor(String which) {
        // 全部变成灰色
        for (int i = 0; i < gradeBtnList.size(); i++) {
            if (i + 1 != grade) {
                GradientDrawable gradeBgShape = (GradientDrawable) gradeBtnList.get(i).getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.NONE));
            }
        }

        GradientDrawable gradeBgShape;
        switch (which) {
            case "S":
                gradeBgShape = (GradientDrawable) btn_s.getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.S));
                break;

            case "A":
                gradeBgShape = (GradientDrawable) btn_a.getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.A));
                break;

            case "B":
                gradeBgShape = (GradientDrawable) btn_b.getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.B));
                break;

            case "C":
                gradeBgShape = (GradientDrawable) btn_c.getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.C));
                break;

            case "D":
                gradeBgShape = (GradientDrawable) btn_d.getBackground();
                gradeBgShape.setColor(getResources().getColor(R.color.D));
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
                || (isPublishTime && year_deadline != 0 && year_publishTime == 0)) {
            year = year_deadline;
            month = month_deadline;
            day = day_deadline;
        } else if ((isDeadline) || (isPublishTime && year_publishTime == 0)) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = year_publishTime;
            month = month_publishTime;
            day = day_publishTime;
        }

        return new MyDate(year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

        if (isDeadline) {
            year_deadline = year;
            month_deadline = monthOfYear;
            day_deadline = dayOfMonth;

            isDeadline = false;

            tv_deadline.setText(date);
        } else if (isPublishTime) {
            year_publishTime = year;
            month_publishTime = monthOfYear;
            day_publishTime = dayOfMonth;

            isPublishTime = false;

            tv_publishTime.setText(date);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Constants.CODE_MULTIPLE_PICTURE:
                if(resultCode == RESULT_OK) {
                    ArrayList<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
                break;

            case Constants.CODE_CHECK_PICTURE:
                if(resultCode == RESULT_OK){
                    adpt_publishPicture.pictureList = data.getStringArrayListExtra("pictureList");
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            setResult(RESULT_CANCELED);

            finish();
            overridePendingTransition(R.anim.in_left_right,
                    R.anim.out_left_right);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
