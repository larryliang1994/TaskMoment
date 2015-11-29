package com.jiubai.taskmoment.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.alibaba.sdk.android.media.upload.UploadListener;
import com.alibaba.sdk.android.media.upload.UploadTask;
import com.alibaba.sdk.android.media.utils.FailReason;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.BaseUploadListener;
import com.jiubai.taskmoment.net.MediaServiceUtil;
import com.jiubai.taskmoment.view.IUploadImageView;

import java.util.Calendar;

/**
 * Created by howell on 2015/11/29.
 * UploadPresenter实现类
 */
public class UploadImagePresenterImpl implements IUploadImagePresenter {
    private IUploadImageView iUploadImageView;
    private Context context;

    public UploadImagePresenterImpl(Context context, IUploadImageView iUploadImageView) {
        this.context = context;
        this.iUploadImageView = iUploadImageView;
    }

    @Override
    public void doUploadImage(Bitmap bitmap, String dir, final String objectName, final String type) {
        UploadListener listener = new BaseUploadListener() {

            @Override
            public void onUploadFailed(UploadTask uploadTask, FailReason failReason) {
                System.out.println(failReason.getMessage());

                iUploadImageView.onUploadImageResult(Constants.FAILED, "图片上传失败，请重试");
            }

            @Override
            public void onUploadComplete(UploadTask uploadTask) {

                didUploadImageComplete(objectName, type);

                iUploadImageView.onUploadImageResult(Constants.SUCCESS, "");
            }

        };

        MediaServiceUtil.uploadImage(bitmap, Constants.DIR_BACKGROUND, objectName, listener);
    }

    private void didUploadImageComplete(String objectName, String type) {
        switch (type){
            case Constants.SP_KEY_COMPANY_BACKGROUND:
                Config.COMPANY_BACKGROUND = Urls.MEDIA_CENTER_BACKGROUND + objectName;

                // 更新时间戳
                Config.TIME = Calendar.getInstance().getTimeInMillis();

                SharedPreferences sp = context.getSharedPreferences(
                        Constants.SP_FILENAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND,
                        Config.COMPANY_BACKGROUND);
                editor.putLong(Constants.SP_KEY_TIME, Config.TIME);
                editor.apply();
                break;
        }
    }
}
