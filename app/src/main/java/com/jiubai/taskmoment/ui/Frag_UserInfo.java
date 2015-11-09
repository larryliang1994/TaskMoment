package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadTask;
import com.alibaba.sdk.android.media.utils.FailReason;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_UserInfo;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.BaseUploadListener;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.receiver.Receiver_UpdateView;

import java.util.Calendar;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心
 */
public class Frag_UserInfo extends Fragment {
    private Adpt_UserInfo adapter;
    private Receiver_UpdateView nicknameReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_userinfo, container, false);

        initView(view);

        return view;
    }

    /**
     * 初始化界面
     */
    private void initView(View view) {
        adapter = new Adpt_UserInfo(getActivity(), this);
        ListView lv_userInfo = (ListView) view.findViewById(R.id.lv_userInfo);
        lv_userInfo.setAdapter(adapter);

        Button btn_logout = (Button) view.findViewById(R.id.btn_logout);

        GradientDrawable logoutBgShape = (GradientDrawable) btn_logout.getBackground();
        logoutBgShape.setColor(ContextCompat.getColor(getActivity(), R.color.primary));

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialDialog dialog = new MaterialDialog(getActivity());
                dialog.setTitle("注销")
                        .setMessage("真的要注销吗?")
                        .setPositiveButton("真的", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                                UtilBox.clearAllData(getActivity());

                                startActivity(new Intent(getActivity(), Aty_Login.class));
                                getActivity().finish();
                                getActivity().overridePendingTransition(
                                        R.anim.in_left_right, R.anim.out_left_right);
                            }
                        })
                        .setNegativeButton("假的", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        })
                        .setCanceledOnTouchOutside(true)
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        nicknameReceiver = new Receiver_UpdateView(getActivity(),
                new Receiver_UpdateView.UpdateCallBack() {
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
        getActivity().unregisterReceiver(nicknameReceiver);

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(getActivity(), R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final Bitmap bitmap = data.getParcelableExtra("data");

                    final String objectName = Config.MID + ".jpg";

                    final UploadListener uploadListener = new BaseUploadListener() {
                        @Override
                        public void onUploadFailed(UploadTask uploadTask, FailReason failReason) {
                            System.out.println(failReason.getMessage());

                            Toast.makeText(getActivity(),
                                    "头像上传失败，请重试",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onUploadComplete(UploadTask uploadTask) {
                            // 更新时间戳
                            Config.TIME = Calendar.getInstance().getTimeInMillis();

                            Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + objectName;

                            SharedPreferences sp = getActivity()
                                    .getSharedPreferences(Constants.SP_FILENAME,
                                            Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(Constants.SP_KEY_PORTRAIT,
                                    Config.PORTRAIT);
                            editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                            editor.apply();

                            adapter.notifyDataSetChanged();

                            // 发送更新头像广播
                            getActivity().sendBroadcast(
                                    new Intent(Constants.ACTION_CHANGE_PORTRAIT));

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
