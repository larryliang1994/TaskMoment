package com.jiubai.taskmoment.view.fragment;

import android.app.Activity;
import android.content.Intent;
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

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.UserInfoAdapter;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewReceiver;
import com.jiubai.taskmoment.view.activity.LoginActivity;
import com.jiubai.taskmoment.view.iview.IUploadImageView;

import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心
 */
public class UserInfoFragment extends Fragment implements IUploadImageView{
    private UserInfoAdapter adapter;
    private UpdateViewReceiver nicknameReceiver;
    private IUploadImagePresenter uploadImagePresenter;

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
        adapter = new UserInfoAdapter(getActivity(), this);
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

                                startActivity(new Intent(getActivity(), LoginActivity.class));
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

        uploadImagePresenter = new UploadImagePresenterImpl(getActivity(), this);
    }

    @Override
    public void onStart() {
        nicknameReceiver = new UpdateViewReceiver(getActivity(),
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
            Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {

    }
}
