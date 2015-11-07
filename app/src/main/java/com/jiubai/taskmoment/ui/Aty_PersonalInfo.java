package com.jiubai.taskmoment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_PersonalInfo;
import com.jiubai.taskmoment.other.UtilBox;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人信息页面
 */
public class Aty_PersonalInfo extends AppCompatActivity {
    @Bind(R.id.lv_personalInfo)
    ListView lv;

    @Bind(R.id.tv_title)
    TextView tv_title;

    private String mid, nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_personal_info);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        nickname = intent.getStringExtra("nickname");

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText("详细信息");

        lv.setAdapter(new Adpt_PersonalInfo(this, mid, nickname));
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.in_left_right,
                        R.anim.out_left_right);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

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
