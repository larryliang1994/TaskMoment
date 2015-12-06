package com.jiubai.taskmoment.view.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.common.UtilBox;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 关于页面
 */
public class AboutActivity extends BaseActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_version)
    TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_about);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

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
}
