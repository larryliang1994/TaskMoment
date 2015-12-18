package com.jiubai.taskmoment.view.activity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.widget.RippleView;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建公司
 */
public class AddCompanyActivity extends BaseActivity
        implements RippleView.OnRippleCompleteListener, TextWatcher {
    @Bind(R.id.rv_btn_submit)
    RippleView rv_btn_submit;

    @Bind(R.id.btn_submit)
    Button btn_submit;

    @Bind(R.id.edt_companyName)
    EditText edt_companyName;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_addcompany);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {

        tv_title.setText(R.string.addCompany);

        edt_companyName.addTextChangedListener(this);

        final GradientDrawable submitBgShape = (GradientDrawable) btn_submit.getBackground();
        submitBgShape.setColor(ContextCompat.getColor(this, R.color.gray));
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                AddCompanyActivity.this.setResult(RESULT_CANCELED);
                AddCompanyActivity.this.finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
                break;
        }
    }

    /**
     * RippleView动画完毕后执行
     *
     * @param rippleView 点击的RippleView
     */
    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()) {
            case R.id.rv_btn_submit:
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(AddCompanyActivity.this, R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                rv_btn_submit.setOnRippleCompleteListener(null);

                btn_submit.setText("提交中");

                String[] key = {"name"};
                String[] value = {edt_companyName.getText().toString()};
                VolleyUtil.requestWithCookie("create_company", key, value,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                rv_btn_submit.setOnRippleCompleteListener(AddCompanyActivity.this);
                                checkResponse(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                btn_submit.setText(R.string.addCompany);
                                rv_btn_submit.setOnRippleCompleteListener(AddCompanyActivity.this);
                                Toast.makeText(AddCompanyActivity.this, "创建失败，请重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }

    /**
     * 检查是否创建成功
     *
     * @param response 返回的json
     */
    private void checkResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String status = json.getString("status");

            if ("1".equals(status) || "900001".equals(status)) {
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
            } else {
                btn_submit.setText(R.string.addCompany);
                Toast.makeText(this, json.getString("info"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final GradientDrawable submitBgShape = (GradientDrawable) btn_submit.getBackground();

        if (edt_companyName.getText().length() != 0) {
            rv_btn_submit.setOnRippleCompleteListener(this);
            submitBgShape.setColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            rv_btn_submit.setOnRippleCompleteListener(null);
            submitBgShape.setColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}