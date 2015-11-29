package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadTask;
import com.alibaba.sdk.android.media.utils.FailReason;
import com.jiubai.taskmoment.BaseActivity;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_PersonalInfo;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.customview.SlidingLayout;
import com.jiubai.taskmoment.net.BaseUploadListener;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.other.UtilBox;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人信息页面
 */
public class Aty_PersonalInfo extends BaseActivity {
    @Bind(R.id.lv_personalInfo)
    ListView lv;

    @Bind(R.id.tv_title)
    TextView tv_title;

    private String mid, nickname;
    private Adpt_PersonalInfo adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_personal_info);

        new SlidingLayout(this);

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

        if (!mid.equals(Config.MID)) {
            adapter = new Adpt_PersonalInfo(this, mid, nickname);
        } else {
            if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                adapter = new Adpt_PersonalInfo(this, mid, "昵称");
            } else {
                adapter = new Adpt_PersonalInfo(this, mid, Config.NICKNAME);
            }
        }

        lv.setAdapter(adapter);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(this, R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final Bitmap bitmap = data.getParcelableExtra("data");

                    final String objectName = Config.MID + ".jpg";

                    final UploadListener uploadListener = new BaseUploadListener() {
                        @Override
                        public void onUploadFailed(UploadTask uploadTask, FailReason failReason) {
                            System.out.println(failReason.getMessage());

                            Toast.makeText(Aty_PersonalInfo.this,
                                    "头像上传失败，请重试",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUploadComplete(UploadTask uploadTask) {
                            Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + objectName;

                            // 更新时间戳
                            Config.TIME = Calendar.getInstance().getTimeInMillis();

                            SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME,
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(Constants.SP_KEY_PORTRAIT,
                                    Config.PORTRAIT);
                            editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                            editor.apply();

                            adapter.notifyDataSetChanged();

                            // 发送更新头像广播
                            sendBroadcast(new Intent(Constants.ACTION_CHANGE_PORTRAIT));
                        }
                    };

                    MediaServiceUtil.uploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            Constants.DIR_PORTRAIT, objectName, uploadListener);
                }
                break;
        }
    }

}
