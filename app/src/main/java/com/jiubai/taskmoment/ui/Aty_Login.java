package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.customview.RippleView;
import com.jiubai.taskmoment.customview.RotateLoading;
import com.jiubai.taskmoment.other.SmsContentUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.presenter.GetVerifyCodePresenterImpl;
import com.jiubai.taskmoment.presenter.IGetVerifyCodePresenter;
import com.jiubai.taskmoment.presenter.ILoginPresenter;
import com.jiubai.taskmoment.presenter.LoginPresenterImpl;
import com.jiubai.taskmoment.view.IGetVerifyCodeView;
import com.jiubai.taskmoment.view.ILoginView;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 登录页面
 */
public class Aty_Login extends Activity implements ILoginView, IGetVerifyCodeView, TextWatcher,
        RippleView.OnRippleCompleteListener {
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
    private ILoginPresenter loginPresenter;
    private IGetVerifyCodePresenter getVerifyCodePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        //UtilBox.setStatusBarTint(this, R.color.primary);

        setContentView(R.layout.aty_login);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.dialog_rotate_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        rl = (RotateLoading) dialog.findViewById(R.id.rl_dialog);

        edt_telephone.addTextChangedListener(this);

        edt_verifyCode.addTextChangedListener(this);

        GradientDrawable verifyBgShape = (GradientDrawable) btn_getVerifyCode.getBackground();
        verifyBgShape.setColor(ContextCompat.getColor(this, R.color.gray));

        GradientDrawable submitBgShape = (GradientDrawable) btn_submit.getBackground();
        submitBgShape.setColor(ContextCompat.getColor(this, R.color.gray));

        getVerifyCodePresenter = new GetVerifyCodePresenterImpl(this);
        loginPresenter = new LoginPresenterImpl(this, this);
        loginPresenter.onSetRotateLoadingVisibility(Constants.INVISIBLE);
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
                verifyBgShape.setColor(ContextCompat.getColor(this, R.color.primary));
            }

            if (edt_verifyCode.getText().toString().length() == 6) {
                rv_btn_submit.setOnRippleCompleteListener(this);
                submitBgShape.setColor(ContextCompat.getColor(this, R.color.primary));
            } else {
                rv_btn_submit.setOnRippleCompleteListener(null);
                submitBgShape.setColor(ContextCompat.getColor(this, R.color.gray));
            }
        } else {
            rv_btn_getVerifyCode.setOnRippleCompleteListener(null);
            rv_btn_submit.setOnRippleCompleteListener(null);
            submitBgShape.setColor(ContextCompat.getColor(this, R.color.gray));
            verifyBgShape.setColor(ContextCompat.getColor(this, R.color.gray));
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
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(Aty_Login.this, R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                getVerifyCodePresenter.doGetVerifyCode(edt_telephone.getText().toString());
                break;

            case R.id.rv_btn_submit:
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(Aty_Login.this, R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                loginPresenter.doLogin(edt_telephone.getText().toString(),
                        edt_verifyCode.getText().toString());
                break;
        }
    }

    /**
     * 登录结果回调
     *
     * @param result true表示登录成功
     * @param info 返回的信息
     */
    @Override
    public void onLoginResult(boolean result, String info) {
        if (result) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, Aty_Company.class);
            intent.putExtra("isLogin", true);

            startActivity(intent);
            Aty_Login.this.finish();
            overridePendingTransition(R.anim.in_right_left, R.anim.scale_stay);
        } else {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取验证码结果回调
     *
     * @param result true表示获取成功
     * @param info 返回的信息
     */
    @Override
    public void onGetVerifyCodeResult(boolean result, String info) {
        if(result){
            Toast.makeText(Aty_Login.this, info, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(Aty_Login.this, "获取失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击获取验证码后更新view
     */
    @Override
    public void onUpdateView() {
        // 注册短信变化监听
        SmsContentUtil smsContent = new SmsContentUtil(Aty_Login.this,
                new Handler(), edt_verifyCode);
        Aty_Login.this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, smsContent);

        new TimeCount(60000, 1000).start();
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param visibility visible代表显示, invisible代表隐藏
     */
    @Override
    public void onSetRotateLoadingVisibility(int visibility) {
        if (visibility == Constants.VISIBLE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                    rl.start();
                }
            });
        } else if (visibility == Constants.INVISIBLE) {
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

    /**
     * 倒计时
     */
    class TimeCount extends CountDownTimer {
        // 参数依次为总时长,和计时的时间间隔
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        // 计时完毕时触发
        @Override
        public void onFinish() {
            isCounting = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rv_btn_getVerifyCode.setOnRippleCompleteListener(Aty_Login.this);
                    btn_getVerifyCode.setText("获取");

                    edt_telephone.addTextChangedListener(Aty_Login.this);

                    final GradientDrawable verifyBgShape
                            = (GradientDrawable) btn_getVerifyCode.getBackground();
                    verifyBgShape.setColor(ContextCompat.getColor(Aty_Login.this, R.color.primary));
                }
            });
        }

        // 计时过程显示
        @Override
        public void onTick(final long millisUntilFinished) {
            isCounting = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rv_btn_getVerifyCode.setOnRippleCompleteListener(null);
                    btn_getVerifyCode.setText(millisUntilFinished / 1000 + "秒");

                    edt_telephone.removeTextChangedListener(Aty_Login.this);

                    final GradientDrawable verifyBgShape
                            = (GradientDrawable) btn_getVerifyCode.getBackground();
                    verifyBgShape.setColor(ContextCompat.getColor(Aty_Login.this, R.color.gray));
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
