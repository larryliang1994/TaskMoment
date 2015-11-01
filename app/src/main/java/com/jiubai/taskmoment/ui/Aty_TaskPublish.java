package com.jiubai.taskmoment.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.adapter.Adpt_PublishPicture;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.classes.MyDate;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.OssUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.view.DateDialog;
import com.jiubai.taskmoment.view.RippleView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean isDeadline = false, isPublishTime = false;
    private ArrayList<String> pictureList = new ArrayList<>();
    private Adpt_PublishPicture adpt_publishPicture;
    private int uploadedNum = 0;
    private int executor = -1, supervisor = -1, auditor = -1;
    private int year_deadline = 0, month_deadline = 0, day_deadline = 0,
            year_publishTime = 0, month_publishTime = 0, day_publishTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

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
        publishBackground.setColor(getResources().getColor(R.color.primary));

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
                    // 先把多余的添加图片入口删掉
                    if (adpt_publishPicture.pictureList != null
                            && !adpt_publishPicture.pictureList.isEmpty()
                            && adpt_publishPicture.actualCount < 9) {
                        adpt_publishPicture.pictureList.remove(
                                adpt_publishPicture.pictureList.size() - 1);
                    }

                    /**
                     *  'p1'         => _post('p1'),//任务级别
                     *  'comments'   => _post('comments'),//备注
                     *  'works'      => _post('works'),//图片
                     *  'ext1'       => _post('ext1'),//执行者id
                     *  'ext2'       => _post('ext2'),//监督者id
                     *  'ext3'       => _post('ext3'),//审核者id
                     *  'ext4'       => _post('cid'),//所属公司
                     *  'time1'      => _post('time1'),//完成期限
                     *  'time2'      => _post('time2'),//发布时间
                     */
                    String[] key = {"p1", "comments", "ext1", "ext2", "ext3",
                            "cid", "time1", "time2"};
                    String[] value = {
                            String.valueOf(grade),
                            edt_desc.getText().toString(),
                            Adpt_Member.memberList.get(executor).getMid(),
                            Adpt_Member.memberList.get(supervisor).getMid(),
                            Adpt_Member.memberList.get(auditor).getMid(),
                            Config.CID,
                            UtilBox.getStringToDate(tv_deadline.getText().toString()) / 1000 + "",
                            UtilBox.getStringToDate(tv_publishTime.getText().toString()) / 1000 + ""};

                    VolleyUtil.requestWithCookie(Urls.PUBLISH_TASK, key, value,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject responseJson = new JSONObject(response);

                                        if (!"1".equals(responseJson.getString("status"))
                                                && !"900001".equals(responseJson.getString("status"))) {
                                            System.out.println(response);

                                            Toast.makeText(Aty_TaskPublish.this,
                                                    responseJson.getString("info"),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            // 为了能马上显示出来
                                            setResultAndFinish(responseJson.getString("taskid"));

                                            // 上传图片
                                            if (adpt_publishPicture.pictureList != null
                                                    && !adpt_publishPicture.pictureList.isEmpty()) {
                                                uploadImage(responseJson.getString("taskid"));
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                        Toast.makeText(Aty_TaskPublish.this,
                                                R.string.usual_error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    volleyError.printStackTrace();
                                }
                            });


                }
            }
        });
    }

    /**
     * 设置返回结果并关掉当前aty
     */
    private void setResultAndFinish(String taskID){
        Intent intent = new Intent();
        intent.putExtra("grade", gradeBtnList.get(grade - 1).getText().toString());
        intent.putExtra("content", edt_desc.getText().toString());
        intent.putExtra("pictureList", adpt_publishPicture.pictureList);
        intent.putExtra("create_time",
                Calendar.getInstance(Locale.CHINA).getTimeInMillis());
        intent.putExtra("executor", Adpt_Member.memberList.get(executor).getMid());
        intent.putExtra("supervisor", Adpt_Member.memberList.get(supervisor).getMid());
        intent.putExtra("auditor", Adpt_Member.memberList.get(auditor).getMid());
        intent.putExtra("taskID", taskID);

        Aty_TaskPublish.this.setResult(RESULT_OK, intent);
        Aty_TaskPublish.this.finish();
        overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
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
                Aty_TaskPublish.this.setResult(RESULT_CANCELED);
                Aty_TaskPublish.this.finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
                break;

            case R.id.tv_deadline:
                isDeadline = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.tv_publishTime:
                isPublishTime = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.tv_executor:
                if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
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
                if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
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
                if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
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
     * 上传选择的图片
     * 逐个递归上传
     * 完成后上传其他相关信息
     */
    @SuppressWarnings("deprecation")
    private void uploadImage(final String id) {
        // 压缩图片
        final Bitmap bitmap = UtilBox.getLocalBitmap(
                adpt_publishPicture.pictureList.get(uploadedNum),
                UtilBox.getWidthPixels(this), UtilBox.getHeightPixels(this));

        OssUtil.uploadImage(UtilBox.compressImage(bitmap, Constants.SIZE_TASK_IMAGE),
                UtilBox.getObjectName(),
                new SaveCallback() {
                    @Override
                    public void onSuccess(String objectKey) {
                        // 已上传的图片数加一
                        uploadedNum++;

                        // 记录已上传的图片的文件名
                        pictureList.add(objectKey);

                        // -1是因为最后一张是本地的加号
                        if (uploadedNum < adpt_publishPicture.pictureList.size() - 1) {
                            // 接着上传下一张图片
                            uploadImage(id);
                        } else {
                            String[] key = {"taskid", "works"};
                            String[] value = {id, new JSONArray(pictureList).toString()};

                            VolleyUtil.requestWithCookie(Urls.UPDATE_TASK_PICTURE, key, value,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject responseJson = new JSONObject(response);

                                                if (!"1".equals(responseJson.getString("status"))
                                                        && !"900001".equals(responseJson.getString("status"))) {
                                                    System.out.println(response);

                                                    Toast.makeText(Aty_TaskPublish.this,
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
                                            volleyError.printStackTrace();

                                            Toast.makeText(Aty_TaskPublish.this,
                                                    R.string.usual_error,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onProgress(String objectKey, int i, int i1) {

                    }

                    @Override
                    public void onFailure(String objectKey, OSSException e) {
                        Looper.prepare();
                        Toast.makeText(Aty_TaskPublish.this,
                                R.string.usual_error,
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        Looper.loop();
                    }
                });
    }

    /**
     * 弹出成员选择对话框
     * 出现的+1-1都是因为memberList有空白的头尾
     *
     * @param viewID   显示控件的Id
     * @param whichExt 正在选择哪个
     */
    private void showMemberList(final int viewID, final String whichExt) {
        String[] members = new String[Adpt_Member.memberList.size() - 2];
        for (int i = 1; i < Adpt_Member.memberList.size() - 1; i++) {

            Member member = Adpt_Member.memberList.get(i);

            if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                members[i - 1] = member.getName();
            } else {
                members[i - 1] = member.getMobile();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Aty_TaskPublish.this);
        builder.setItems(members, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Member member = Adpt_Member.memberList.get(which + 1);
                if (!"null".equals(member.getName())
                        && !"".equals(member.getName())) {
                    ((TextView) Aty_TaskPublish.this.findViewById(viewID))
                            .setText(member.getName());
                } else {
                    ((TextView) Aty_TaskPublish.this.findViewById(viewID))
                            .setText(member.getMobile());
                }

                System.out.println(which);
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
                    ArrayList<String> path
                            = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

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
