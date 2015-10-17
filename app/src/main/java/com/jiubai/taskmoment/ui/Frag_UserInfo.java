package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.adapter.Adpt_UserInfo;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.OssUtil;

/**
 * 个人中心
 */
public class Frag_UserInfo extends Fragment {

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
        ListView lv_userInfo = (ListView) view.findViewById(R.id.lv_userInfo);
        lv_userInfo.setAdapter(new Adpt_UserInfo(getActivity()));
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

                    Adpt_UserInfo.iv_portrait.setImageBitmap(bitmap);

                    final String objectName = UtilBox.getObjectName();

                    OssUtil.uploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            objectName,
                            new SaveCallback() {
                                @Override
                                public void onSuccess(String objectKey) {
                                    System.out.println("upload success!");
                                }

                                @Override
                                public void onProgress(String objectKey, int i, int i1) {

                                }

                                @Override
                                public void onFailure(String objectKey, OSSException e) {
                                    System.out.println(objectKey + " failed..");
                                    e.printStackTrace();

                                    Toast.makeText(getActivity(),
                                            R.string.usual_error,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                break;
        }
    }
}
