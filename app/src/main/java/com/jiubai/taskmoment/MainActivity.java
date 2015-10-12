package com.jiubai.taskmoment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.Aty_Company;
import com.jiubai.taskmoment.ui.Aty_Login;
import com.jiubai.taskmoment.ui.Aty_Main;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化请求队列
        VolleyUtil.initRequestQueue(getApplicationContext());

        // 初始化网络状态
        getNetworkState();

        // 读取cookie
        loadCookie();

        // 读取上次进入的公司
        loadCompanyInfo();

        // 获取五位随机数
        getRandom();

        // 初始化图片加载框架
        initImageLoader();

        if (Config.COOKIE == null) {
            startActivity(new Intent(this, Aty_Login.class));
        } else if (Config.CID == null) {
            startActivity(new Intent(this, Aty_Company.class));
        } else {
            startActivity(new Intent(this, Aty_Main.class));
        }

        finish();
    }

    private void loadCookie() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        Config.COOKIE = sp.getString("cookie", null);
    }

    private void loadCompanyInfo() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        Config.COMPANY_NAME = sp.getString("companyName", null);
        Config.CID = sp.getString("companyId", null);
    }

    @SuppressWarnings("deprecation")
    private void initImageLoader(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory().cacheOnDisc().build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext()).defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    private void getNetworkState() {
        // 获取网络连接管理器对象（系统服务对象）
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取网络状态
        NetworkInfo info = cm.getActiveNetworkInfo();

        Config.IS_CONNECTED = info != null && info.isAvailable();
    }

    private static void getRandom() {
        int number = (int) (Math.random() * 100000);
        if (number < 90000) {
            number += 10000;
        }
        Config.RAMDOM = String.valueOf(number);
    }

    /**
     * 开一个线程把assert目录下的图片复制到SD卡目录下
     * @param testImageOnSdCard 图片文件
     */
    private void copyTestImageToSdCard(final File testImageOnSdCard) {
        new Thread(new Runnable() {
            @SuppressLint("LongLogTag")
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(testImageOnSdCard);
                    byte[] buffer = new byte[8192];
                    int read;
                    try (InputStream is = getAssets().open("")) {
                        while ((read = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, read); // 写入输出流
                        }
                    } finally {
                        fos.flush();        // 写入SD卡
                        fos.close();        // 关闭输出流

                    }
                } catch (IOException e) {
                    Log.i("Can't copy test image onto SD card", "");
                }
            }
        }).start();     // 启动线程
    }
}
