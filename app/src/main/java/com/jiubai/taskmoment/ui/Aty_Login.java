package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.other.SmsContentUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.net.SoapUtil;
import com.jiubai.taskmoment.view.RippleView;
import com.jiubai.taskmoment.view.RotateLoading;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 登录页面
 */
public class Aty_Login extends Activity implements RippleView.OnRippleCompleteListener, TextWatcher {
    @Bind(R.id.edt_telephone)
    EditText edt_telephone;

    @Bind(R.id.edt_verifyCode)
    EditText edt_verifyCode;

    @Bind(R.id.btn_getVerifyCode)
    Button btn_getVerifyCode;

    @Bind(R.id.btn_submit)
    Button btn_submit;

    @Bind(R.id.rv_btn_getVerifyCode)
    RippleView rv_btn_getVerifyCode;

    @Bind(R.id.rv_btn_submit)
    RippleView rv_btn_submit;

    private RotateLoading rl;
    private Dialog dialog = null;
    private boolean isCounting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_login);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.dialog_rotate_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        rl = (RotateLoading) dialog.findViewById(R.id.rl_dialog);

        edt_telephone.addTextChangedListener(this);

        edt_verifyCode.addTextChangedListener(this);
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param which show代表显示, dismiss代表隐藏
     */
    private void changeLoadingState(String which) {
        if ("show".equals(which)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                    rl.start();
                }
            });
        } else if ("dismiss".equals(which)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl.stop();
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * 处理登录成功后的cookie
     *
     * @param cookie 登录成功后返回的cookie
     */
    private void handleLoginResponse(String cookie) {

        // 延长cookie可用时间
        SoapUtil.extendCookieLifeTime(cookie);

        // 保存cookie
        SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (Config.COOKIE != null) {
            editor.putString(Constants.SP_KEY_COOKIE, Config.COOKIE);
        }

        editor.apply();
    }

    /**
     * 点击返回，回到桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent MyIntent = new Intent(Intent.ACTION_MAIN);
            MyIntent.addCategory(Intent.CATEGORY_HOME);
            startActivity(MyIntent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * 监听输入
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final GradientDrawable verifyBgShape = (GradientDrawable) btn_getVerifyCode.getBackground();
        final GradientDrawable submitBgShape = (GradientDrawable) btn_submit.getBackground();

        String content = edt_telephone.getText().toString();

        if (UtilBox.isTelephoneNumber(content)) {
            if (!isCounting) {
                rv_btn_getVerifyCode.setOnRippleCompleteListener(this);
                verifyBgShape.setColor(getResources().getColor(R.color.primary));
            }

            if (edt_verifyCode.getText().toString().length() == 6) {
                rv_btn_submit.setOnRippleCompleteListener(this);
                submitBgShape.setColor(getResources().getColor(R.color.primary));
            } else {
                rv_btn_submit.setOnRippleCompleteListener(null);
                submitBgShape.setColor(getResources().getColor(R.color.gray));
            }
        } else {
            rv_btn_getVerifyCode.setOnRippleCompleteListener(null);
            rv_btn_submit.setOnRippleCompleteListener(null);
            submitBgShape.setColor(getResources().getColor(R.color.gray));
            verifyBgShape.setColor(getResources().getColor(R.color.gray));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * RippleView动画完毕后执行
     *
     * @param rippleView 点击的RippleView
     */
    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()) {
            case R.id.rv_btn_getVerifyCode:
                new Handler().post(new Runnable() {

                    public void run() {

                        final String tele = edt_telephone.getText().toString();

                        if (!Config.IS_CONNECTED) {
                            Toast.makeText(Aty_Login.this, R.string.cant_access_network,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 注册短信变化监听
                        SmsContentUtil smsContent = new SmsContentUtil(Aty_Login.this,
                                new Handler(), edt_verifyCode);
                        Aty_Login.this.getContentResolver().registerContentObserver(
                                Uri.parse("content://sms/"), true, smsContent);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();

                                new TimeCount(60000, 1000).start();

                                changeLoadingState("show");

                                String[] soapKey = {"type", "table_name", "feedback_url", "return"};
                                String[] soapValue = {"sms_send_verifycode", Config.RANDOM, "", "1"};
                                String[] httpKey = {"mobile"};
                                String[] httpValue = {tele};
                                VolleyUtil.requestWithSoap(soapKey, soapValue, httpKey, httpValue,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                changeLoadingState("dismiss");

                                                try {
                                                    JSONObject obj = new JSONObject(response);
                                                    Toast.makeText(Aty_Login.this,
                                                            obj.getString("info"),
                                                            Toast.LENGTH_SHORT).show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {
                                                changeLoadingState("dismiss");

                                                if (volleyError != null
                                                        && volleyError.getMessage() != null) {
                                                    System.out.println(volleyError.getMessage());
                                                }
                                                Toast.makeText(Aty_Login.this,
                                                        R.string.usual_error,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                Looper.loop();
                            }
                        }).start();
                    }
                });
                break;

            case R.id.rv_btn_submit:
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {

                        final String tele = edt_telephone.getText().toString();
                        final String verify = edt_verifyCode.getText().toString();

                        if (!Config.IS_CONNECTED) {
                            Toast.makeText(Aty_Login.this, R.string.cant_access_network,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();

                                changeLoadingState("show");

                                String[] soapKey = {"type", "table_name", "feedback_url", "return"};
                                String[] soapValue = {"mobile_login", Config.RANDOM, "", "1"};
                                String[] httpKey = {"mobile", "check_code"};
                                String[] httpValue = {tele, verify};
                                VolleyUtil.requestWithSoap(soapKey, soapValue, httpKey, httpValue,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(final String response) {
                                                changeLoadingState("dismiss");

                                                System.out.println(response);

                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Looper.prepare();
                                                        try {
                                                            JSONObject responseJson = new JSONObject(response);
                                                            // 若登录成功，则延长cookie寿命，并跳转
                                                            if ("900001".equals(responseJson.getString("status"))) {
                                                                handleLoginResponse(responseJson.getString("memberCookie"));

                                                                Toast.makeText(Aty_Login.this, "登录成功",
                                                                        Toast.LENGTH_SHORT).show();

                                                                Intent intent = new Intent(Aty_Login.this,
                                                                        Aty_Company.class);
                                                                intent.putExtra("isLogin", true);
                                                                startActivity(intent);
                                                                Aty_Login.this.finish();
                                                                overridePendingTransition(R.anim.in_right_left,
                                                                        R.anim.out_right_left);

                                                            } else {
                                                                Toast.makeText(Aty_Login.this,
                                                                        responseJson.getString("info"),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        Looper.loop();
                                                    }
                                                }).start();

                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {
                                                changeLoadingState("dismiss");
                                                Toast.makeText(Aty_Login.this,
                                                        R.string.usual_error,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                Looper.loop();
                            }
                        }).start();
                    }
                });
                break;
        }
    }

    /* 倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            isCounting = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rv_btn_getVerifyCode.setOnRippleCompleteListener(Aty_Login.this);
                    btn_getVerifyCode.setText("获取");

                    edt_telephone.addTextChangedListener(Aty_Login.this);

                    final GradientDrawable verifyBgShape = (GradientDrawable) btn_getVerifyCode.getBackground();
                    verifyBgShape.setColor(getResources().getColor(R.color.primary));
                }
            });
        }

        @Override
        public void onTick(final long millisUntilFinished) {//计时过程显示
            isCounting = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rv_btn_getVerifyCode.setOnRippleCompleteListener(null);
                    btn_getVerifyCode.setText(millisUntilFinished / 1000 + "秒");

                    edt_telephone.removeTextChangedListener(Aty_Login.this);

                    final GradientDrawable verifyBgShape = (GradientDrawable) btn_getVerifyCode.getBackground();
                    verifyBgShape.setColor(getResources().getColor(R.color.gray));
                }
            });
        }
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
