package com.jiubai.taskmoment.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.view.activity.AboutActivity;
import com.jiubai.taskmoment.view.activity.CompanyInfoActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 偏好设置
 */
public class PreferenceFragment extends Fragment implements RippleView.OnRippleCompleteListener{

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
        ((RippleView)view.findViewById(R.id.rv_getPreview)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_update)).setOnRippleCompleteListener(this);
        ((RippleView)view.findViewById(R.id.rv_about)).setOnRippleCompleteListener(this);
    }

    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()){
            case R.id.rv_companyInfo:
                startActivity(new Intent(getActivity(), CompanyInfoActivity.class));
//                getActivity().overridePendingTransition(
//                        R.anim.in_right_left, R.anim.scale_stay);
                break;

            case R.id.rv_getPreview:
                final MaterialDialog downloadDialog = new MaterialDialog(getActivity());
                downloadDialog.setMessage("即将跳转到浏览器下载\n当前版本任务圈将会被覆盖，真的要获取吗")
                        .setPositiveButton("真的", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downloadDialog.dismiss();

                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://adm.jiubaiwang.cn/WebSite/20055/uploadfile/webeditor2/android/Taskmoment_Material.apk")));
                            }
                        })
                        .setNegativeButton("假的", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downloadDialog.dismiss();
                            }
                        })
                        .setCanceledOnTouchOutside(true)
                        .show();
                break;

            case R.id.rv_share:
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain"); // 纯文本
                intent.putExtra(Intent.EXTRA_TEXT, Constants.SHARE_TEXT);
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getActivity().getTitle()));
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
                startActivity(new Intent(getActivity(), AboutActivity.class));
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;
        }
    }
}