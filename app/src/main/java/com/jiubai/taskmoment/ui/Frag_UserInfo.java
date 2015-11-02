package com.jiubai.taskmoment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心
 */
public class Frag_UserInfo extends Fragment {
    private Adpt_UserInfo adapter;

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
        ListView lv_userInfo = (ListView) view.findViewById(R.id.lv_userInfo);
        lv_userInfo.setAdapter(adapter);

        Button btn_logout = (Button) view.findViewById(R.id.btn_logout);

        GradientDrawable logoutBgShape = (GradientDrawable) btn_logout.getBackground();
        logoutBgShape.setColor(getResources().getColor(R.color.primary));

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

                    final String objectName = "task_moment/" + Config.MID + ".jpg";

                    OssUtil.uploadImage(
                            UtilBox.compressImage(bitmap, Constants.SIZE_PORTRAIT),
                            objectName,
                            new SaveCallback() {
                                @Override
                                public void onSuccess(final String objectKey) {
                                    System.out.println(objectKey + " upload success!");

                                    // 清除原有的cache
                                    MemoryCacheUtils.removeFromCache(Config.PORTRAIT,
                                            ImageLoader.getInstance().getMemoryCache());
                                    DiskCacheUtils.removeFromCache(Config.PORTRAIT,
                                            ImageLoader.getInstance().getDiskCache());

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

                                            // 发送更新头像广播
                                            getActivity().sendBroadcast(
                                                    new Intent(Constants.ACTION_CHANGE_PORTRAIT));
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
