package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.view.RippleView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建公司
 */
public class Aty_AddCompany extends AppCompatActivity
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

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_addcompany);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView() {

        tv_title.setText(R.string.addCompany);

        edt_companyName.addTextChangedListener(this);

        final GradientDrawable submitBgShape = (GradientDrawable) btn_submit.getBackground();
        submitBgShape.setColor(getResources().getColor(R.color.gray));
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                Aty_AddCompany.this.setResult(RESULT_CANCELED);
                Aty_AddCompany.this.finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
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
                    Toast.makeText(Aty_AddCompany.this, R.string.cant_access_network,
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
                                rv_btn_submit.setOnRippleCompleteListener(Aty_AddCompany.this);
                                checkResponse(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                btn_submit.setText(R.string.addCompany);
                                rv_btn_submit.setOnRippleCompleteListener(Aty_AddCompany.this);
                                Toast.makeText(Aty_AddCompany.this, R.string.usual_error,
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
                overridePendingTransition(R.anim.in_left_right,
                        R.anim.out_left_right);
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
            submitBgShape.setColor(getResources().getColor(R.color.primary));
        } else {
            rv_btn_submit.setOnRippleCompleteListener(null);
            submitBgShape.setColor(getResources().getColor(R.color.gray));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
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
