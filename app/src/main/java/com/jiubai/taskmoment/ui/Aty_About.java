package com.jiubai.taskmoment.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.view.SlidingLayout;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 关于页面
 */
public class Aty_About extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_version)
    TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_about);

        new SlidingLayout(this);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText(R.string.about);

        // 获取PackageManager的实例
        PackageManager packageManager = getPackageManager();

        try {
            // getPackageName()是当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            tv_version.setText(packInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            finish();
            overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
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
