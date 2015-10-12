package com.jiubai.taskmoment.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.adapter.Adpt_PublishPicture;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.classes.MyDate;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.view.DateDialog;
import com.jiubai.taskmoment.view.RippleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

    private int grade = 4;
    private List<Button> gradeBtnList = new ArrayList<>();
    private boolean isDeadline = false, isPublishTime = false, hasLoadedMember = false;
    private ArrayList<Member> memberList;
    private Adpt_PublishPicture adpt_publishPicture;
    private int executor = -1, supervisor = -1, auditor = -1;
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
                if (edt_desc.getText().toString().length() == 0) {
                    Toast.makeText(Aty_TaskPublish.this, "请填入任务描述", Toast.LENGTH_SHORT).show();
                } else if (executor == -1) {
                    Toast.makeText(Aty_TaskPublish.this, "请选择执行者", Toast.LENGTH_SHORT).show();
                } else if (supervisor == -1) {
                    Toast.makeText(Aty_TaskPublish.this, "请选择监督者", Toast.LENGTH_SHORT).show();
                } else if (auditor == -1) {
                    Toast.makeText(Aty_TaskPublish.this, "请选择审核者", Toast.LENGTH_SHORT).show();
                } else if (year_deadline == 0) {
                    Toast.makeText(Aty_TaskPublish.this, "请填入完成期限", Toast.LENGTH_SHORT).show();
                } else if (year_publishTime == 0) {
                    Toast.makeText(Aty_TaskPublish.this, "请填入发布时间", Toast.LENGTH_SHORT).show();
                } else {
                    /**
                     *  mid：用户id
                     * 	order_status：分级，开发者后台中配置
                     * 	comments：任务描述
                     * 	works：任务附图
                     * 	ext1：执行者（用户mid）
                     * 	ext2：监督者（用户mid）
                     * 	ext3：审核者（用户mid）
                     * 	time1：完成期限
                     * 	time2：发布时间
                     * 	ifshow：默认on
                     * 	create_time：创建时间，使用当前时间即可
                     * 	update_time：修改时间，使用当前时间即可
                     * 	p1：保存任务评级
                     */
                    String[] key = {"p1", "comments", "ext1", "ext2", "ext3", "time1", "time2"};
                    String[] value = {String.valueOf(grade), edt_desc.getText().toString(),
                            memberList.get(executor).getId(),
                            memberList.get(supervisor).getId(), memberList.get(auditor).getId(),
                            String.valueOf(UtilBox.getStringToDate(tv_deadline.getText().toString())),
                            String.valueOf(UtilBox.getStringToDate(tv_publishTime.getText().toString()))};

                    //VolleyUtil.requestWithCookie(null, key, value, null, null);

                    Aty_TaskPublish.this.setResult(RESULT_OK);
                    Aty_TaskPublish.this.finish();
                    overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
                }
            }
        });
    }

    @OnClick({R.id.iBtn_back, R.id.tv_deadline, R.id.tv_publishTime,
            R.id.tv_executor, R.id.tv_supervisor, R.id.tv_auditor,
            R.id.btn_grade_s, R.id.btn_grade_a, R.id.btn_grade_b, R.id.btn_grade_c, R.id.btn_grade_d})
    public void onClick(View view) {
        MyDate myDate;
        DateDialog dateDialog;

        switch (view.getId()) {
            case R.id.iBtn_back:
                Aty_TaskPublish.this.setResult(RESULT_CANCELED);
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

            case R.id.tv_executor:
                getMember(R.id.tv_executor, "executor");
                break;

            case R.id.tv_supervisor:
                getMember(R.id.tv_supervisor, "supervisor");
                break;

            case R.id.tv_auditor:
                getMember(R.id.tv_auditor, "auditor");
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
     * 获取成员列表
     */
    private void getMember(final int viewID, final String which) {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(this, "啊哦，网络好像抽风了~",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasLoadedMember) {
            showMemberList(viewID, which);
        } else {
            hasLoadedMember = true;

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("加载中...");
            progressDialog.show();

            VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();

                            try {
                                JSONObject responseJson = new JSONObject(response);

                                String responseStatus = responseJson.getString("status");

                                if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {
                                    memberList = new ArrayList<>();

                                    JSONObject memberJson = new JSONObject(response);

                                    if (!"null".equals(memberJson.getString("info"))) {
                                        JSONArray memberArray = memberJson.getJSONArray("info");

                                        for (int i = 0; i < memberArray.length(); i++) {
                                            JSONObject obj = new JSONObject(memberArray.getString(i));
                                            memberList.add(
                                                    new Member(
                                                            obj.getString("real_name"), obj.getString("mobile"),
                                                            obj.getString("id"), obj.getString("mid")));
                                        }

                                        showMemberList(viewID, which);
                                    }
                                } else {
                                    Toast.makeText(Aty_TaskPublish.this, "Oops...好像出错了，再试一次吧？",
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
                            progressDialog.dismiss();
                            Toast.makeText(Aty_TaskPublish.this, "Oops...好像出错了，再试一次吧？",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * 弹出成员选择对话框
     */
    private void showMemberList(final int viewID, final String whichExt) {
        String[] members = new String[memberList.size()];
        for (int i = 0; i < memberList.size(); i++) {

            Member member = memberList.get(i);
            if (!"null".equals(member.getName())
                    && !"".equals(member.getName())) {
                members[i] = member.getName();
            } else {
                members[i] = member.getMobile();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Aty_TaskPublish.this);
        builder.setItems(members, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Member member = memberList.get(which);
                if (!"null".equals(member.getName())
                        && !"".equals(member.getName())) {
                    ((TextView) Aty_TaskPublish.this.findViewById(viewID))
                            .setText(member.getName());
                } else {
                    ((TextView) Aty_TaskPublish.this.findViewById(viewID))
                            .setText(member.getMobile());
                }

                switch (whichExt) {
                    case "executor":
                        executor = which;
                        break;

                    case "supervisor":
                        supervisor = which;
                        break;

                    case "auditor":
                        auditor = which;
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

        switch (requestCode) {
            case Constants.CODE_MULTIPLE_PICTURE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
                break;

            case Constants.CODE_CHECK_PICTURE:
                if (resultCode == RESULT_OK) {
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
