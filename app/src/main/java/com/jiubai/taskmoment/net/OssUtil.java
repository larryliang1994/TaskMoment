package com.jiubai.taskmoment.net;


import android.content.Context;
import android.graphics.Bitmap;

import com.aliyun.mbaas.oss.OSSClient;
import com.aliyun.mbaas.oss.callback.GetBytesCallback;
import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.AccessControlList;
import com.aliyun.mbaas.oss.model.OSSException;
import com.aliyun.mbaas.oss.model.TokenGenerator;
import com.aliyun.mbaas.oss.storage.OSSBucket;
import com.aliyun.mbaas.oss.storage.OSSData;
import com.aliyun.mbaas.oss.util.OSSToolKit;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.config.Constants;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Oss对象上传下载工具
 */
public class OssUtil {
    private static OSSBucket bucket;

    public static void init(Context context) {
        OSSClient.setApplicationContext(context);

        // 设置全局默认加签器
        OSSClient.setGlobalDefaultTokenGenerator(new TokenGenerator() {
            @Override
            public String generateToken(String httpMethod, String md5, String type, String date,
                                        String ossHeaders, String resource) {

                String signature = null;

                String content = httpMethod + "\n" + md5 + "\n" + type + "\n" + date + "\n"
                        + ossHeaders + resource;

                try {
                    signature = OSSToolKit.getHmacSha1Signature(content, Constants.OSSSCRECTKEY);

                    signature = signature.trim();

                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    e.printStackTrace();
                }

                signature = "OSS " + Constants.OSSACCESSKEY + ":" + signature;

                return signature;
            }
        });

        // 设置全局默认数据中心域名
        //OSSClient.setGlobalDefaultHostId(Urls.HOST_ID);

        // 设置全局默认bucket访问权限
        OSSClient.setGlobalDefaultACL(AccessControlList.PUBLIC_READ_WRITE);

        bucket = new OSSBucket(Constants.BUCKET_NAME);
    }

    /**
     * 上传一张图片的服务器
     *
     * @param bitmap    需要上传的图片
     * @param objectKey 图片保存的名字
     * @param callback  上传后回调
     */
    public static void uploadImage(Bitmap bitmap, String objectKey, SaveCallback callback) {

        OSSData ossData = new OSSData(bucket, objectKey);

        try {
            ossData.setData(UtilBox.bitmap2Bytes(bitmap), "multipart/form-data");
        } catch (OSSException e) {
            e.printStackTrace();
        }

        ossData.uploadInBackground(callback);
    }
}
