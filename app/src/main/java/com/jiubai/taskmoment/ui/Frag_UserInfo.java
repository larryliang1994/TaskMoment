package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_UserInfo;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.OssUtil;
import com.jiubai.taskmoment.other.UtilBox;

/**
 * 个人中心
 */
public class Frag_UserInfo extends Fragment {
    private Adpt_UserInfo adapter;
    private ListView lv_userInfo;

    @Nullable
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
        lv_userInfo = (ListView) view.findViewById(R.id.lv_userInfo);
        lv_userInfo.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case Constants.CODE_CHOOSE_PORTRAIT:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(getActivity(), R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final Bitmap bitmap = data.getParcelableExtra("data");

                    final String objectName = UtilBox.getObjectName();

                    OssUtil.uploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            objectName,
                            new SaveCallback() {
                                @Override
                                public void onSuccess(final String objectKey) {
                                    System.out.println(objectKey + " upload success!");

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Config.PORTRAIT = Constants.HOST_ID + objectKey;

                                            SharedPreferences sp = getActivity()
                                                    .getSharedPreferences("config",
                                                            Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString(Constants.SP_KEY_PORTRAIT,
                                                    Config.PORTRAIT);
                                            editor.apply();

                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onProgress(String objectKey, int i, int i1) {

                                }

                                @Override
                                public void onFailure(String objectKey, OSSException e) {
                                    Looper.prepare();

                                    System.out.println(objectKey + " failed..");
                                    e.printStackTrace();

                                    Toast.makeText(getActivity(),
                                            R.string.usual_error,
                                            Toast.LENGTH_SHORT).show();

                                    Looper.loop();
                                }
                            });
                }
                break;
        }
    }
}
