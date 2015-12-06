package com.jiubai.taskmoment.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.PersonalInfoAdapter;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.view.iview.IUploadImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人信息页面
 */
public class PersonalInfoActivity extends BaseActivity implements IUploadImageView {
    @Bind(R.id.lv_personalInfo)
    ListView lv;

    @Bind(R.id.tv_title)
    TextView tv_title;

    private String mid, nickname;
    private PersonalInfoAdapter adapter;
    private IUploadImagePresenter uploadImagePresenter;
    private UpdateViewReceiver nicknameReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personal_info);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

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
        tv_title.setText(nickname);

        if (!mid.equals(Config.MID)) {
            adapter = new PersonalInfoAdapter(this, mid, nickname);
        } else {
            if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                adapter = new PersonalInfoAdapter(this, mid, "昵称");
            } else {
                adapter = new PersonalInfoAdapter(this, mid, Config.NICKNAME);
            }
        }

        lv.setAdapter(adapter);

        uploadImagePresenter = new UploadImagePresenterImpl(this, this);
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
    public void onStart() {
        nicknameReceiver = new UpdateViewReceiver(this,
                new UpdateViewReceiver.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        adapter.notifyDataSetChanged();
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        super.onStart();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(nicknameReceiver);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(this, R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final Bitmap bitmap = data.getParcelableExtra("data");

                    final String objectName = Config.MID + ".jpg";

                    uploadImagePresenter.doUploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            Constants.DIR_PORTRAIT, objectName, Constants.SP_KEY_PORTRAIT);
                }
                break;
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}