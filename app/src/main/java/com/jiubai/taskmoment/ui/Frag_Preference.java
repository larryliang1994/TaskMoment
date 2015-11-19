package com.jiubai.taskmoment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.view.RippleView;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 偏好设置
 */
public class Frag_Preference extends Fragment implements RippleView.OnRippleCompleteListener{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_preference, container, false);

        initView(view);

        return view;
    }

    private void initView(View view){
        ((RippleView)view.findViewById(R.id.rv_companyInfo)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_share)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_feedback)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_update)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_about)).setOnRippleCompleteListener(this);
    }

    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()){
            case R.id.rv_companyInfo:
                break;

            case R.id.rv_share:
                break;

            case R.id.rv_feedback:
                break;

            case R.id.rv_update:
                UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
                    @Override
                    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                        switch (updateStatus) {
                            case UpdateStatus.NoneWifi:
                            case UpdateStatus.Yes: // has update
                                UmengUpdateAgent.showUpdateDialog(getActivity(), updateInfo);
                                break;

                            case UpdateStatus.No: // has no update
                            case UpdateStatus.Timeout: // time out
                                Toast.makeText(getActivity(),
                                        "已经是最新版本",
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

                    @Override
                    public void onClick(int status) {
                        switch (status) {
                            case UpdateStatus.Update:
                                Toast.makeText(getActivity(), "开始下载更新", Toast.LENGTH_SHORT).show();
                                break;
                            case UpdateStatus.Ignore:
                            case UpdateStatus.NotNow:
                                break;
                        }
                    }
                });
                UmengUpdateAgent.update(getActivity());
                break;

            case R.id.rv_about:
                startActivity(new Intent(getActivity(), Aty_About.class));
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;
        }
    }
}
