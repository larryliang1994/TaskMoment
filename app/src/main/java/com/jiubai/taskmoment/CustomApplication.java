package com.jiubai.taskmoment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.OssUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * 程序入口
 */
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化请求队列
        VolleyUtil.initRequestQueue(getApplicationContext());

        // 初始化网络状态
        getNetworkState();

        // 读取存储好的数据——cookie,公司信息,个人信息
        loadStorageData();

        // 初始化图片加载框架
        initImageLoader();

        // 初始化图片上传下载OSS
        OssUtil.init(getApplicationContext());
    }

    private void loadStorageData() {
        SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME, MODE_PRIVATE);
        Config.COOKIE = sp.getString(Constants.SP_KEY_COOKIE, null);
        Config.COMPANY_NAME = sp.getString(Constants.SP_KEY_COMPANY_NAME, null);
        Config.CID = sp.getString(Constants.SP_KEY_COMPANY_ID, null);
        Config.COMPANY_BACKGROUND = sp.getString(Constants.SP_KEY_COMPANY_BACKGROUND, null);
        Config.PORTRAIT = sp.getString(Constants.SP_KEY_PORTRAIT, null);
        Config.MID = sp.getString(Constants.SP_KEY_MID, null);
        Config.NICKNAME = sp.getString(Constants.SP_KEY_NICKNAME, "null");
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    private void getNetworkState() {
        // 获取网络连接管理器对象（系统服务对象）
        ConnectivityManager cm
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取网络状态
        NetworkInfo info = cm.getActiveNetworkInfo();

        Config.IS_CONNECTED = info != null && info.isAvailable();
    }
}
