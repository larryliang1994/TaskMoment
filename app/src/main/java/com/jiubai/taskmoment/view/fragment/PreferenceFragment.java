package com.jiubai.taskmoment.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.view.activity.AboutActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.EmailHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

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
                final UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");

                // 设置分享内容
                mController.setShareContent("友盟社会化组件（SDK）让移动应用快速整合社交分享功能，http://www.umeng.com/social");

                // 设置分享图片, 参数2为图片的url地址
                mController.setShareMedia(new UMImage(getActivity(),
                        "http://a.hiphotos.baidu.com/image/pic/item/83025aafa40f4bfb23babefc064f78f0f736182b.jpg"));

//                // 添加微信平台
//                UMWXHandler wxHandler = new UMWXHandler(getActivity(),
//                        Constants.APP_ID_WX, Constants.APP_SECRET_WX);
//                wxHandler.addToSocialSDK();
//
//                // 添加微信朋友圈
//                UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(),
//                        Constants.APP_ID_WX,Constants.APP_SECRET_WX);
//                wxCircleHandler.setToCircle(true);
//                wxCircleHandler.addToSocialSDK();
//
//                // 添加QQ平台
//                UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(),
//                        Constants.APP_ID_QQ, Constants.APP_KEY_QQ);
//                qqSsoHandler.addToSocialSDK();
//
//                // 添加QQ控件
//                QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(),
//                        Constants.APP_ID_QQ, Constants.APP_KEY_QQ);
//                qZoneSsoHandler.addToSocialSDK();

                //设置新浪SSO handler
                mController.getConfig().setSsoHandler(new SinaSsoHandler());

                //设置腾讯微博SSO handler
                mController.getConfig().setSsoHandler(new TencentWBSsoHandler());

//                //添加人人网SSO授权功能
//                RenrenSsoHandler renrenSsoHandler = new RenrenSsoHandler(getActivity(),
//                        Constants.APP_ID_REN, Constants.APP_KEY_REN, Constants.APP_SECRET_REN);
//                mController.getConfig().setSsoHandler(renrenSsoHandler);

//                // 添加豆瓣平台
//                mController.getConfig().setPlatforms(SHARE_MEDIA.DOUBAN);

                // 添加短信
                SmsHandler smsHandler = new SmsHandler();
                smsHandler.addToSocialSDK();

                // 添加email
                EmailHandler emailHandler = new EmailHandler();
                emailHandler.addToSocialSDK();

                mController.openShare(getActivity(), false);
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
                startActivity(new Intent(getActivity(), AboutActivity.class));
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
                break;
        }
    }
}
