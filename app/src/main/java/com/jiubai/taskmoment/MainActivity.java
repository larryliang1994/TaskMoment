package com.jiubai.taskmoment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.SoapUtil;
import com.jiubai.taskmoment.ui.Aty_Company;
import com.jiubai.taskmoment.ui.Aty_Login;
import com.jiubai.taskmoment.ui.Aty_Main;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Config.COOKIE == null) {
            startActivity(new Intent(this, Aty_Login.class));
        } else if (Config.CID == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(MainActivity.this, R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 获取用户信息
                        getUserInfo();

                        // 清除原有的cache
                        MemoryCacheUtils.removeFromCache(Config.PORTRAIT,
                                ImageLoader.getInstance().getMemoryCache());
                        DiskCacheUtils.removeFromCache(Config.PORTRAIT,
                                ImageLoader.getInstance().getDiskCache());
                    }
                    startActivity(new Intent(MainActivity.this, Aty_Company.class));
                }
            }).start();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(MainActivity.this, R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 获取用户信息
                        getUserInfo();
                    }

                    startActivity(new Intent(MainActivity.this, Aty_Main.class));
                }
            }).start();
        }

        finish();
    }

    private void getUserInfo() {
        String[] decodeKey = {"string", "operation"};
        String[] decodeValue = {Config.COOKIE, "DECODE"};
        String userInfo = SoapUtil.getUrlBySoap("authcode", decodeKey, decodeValue);

        try {
            JSONObject jsonObject = new JSONObject(userInfo);
            Config.MID = jsonObject.getString("id");
            Config.NICKNAME = jsonObject.getString("real_name");
            Config.PORTRAIT = Constants.HOST_ID + "task_moment/" + Config.MID + ".jpg";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SP_KEY_MID, Config.MID);
        editor.putString(Constants.SP_KEY_NICKNAME, Config.NICKNAME);
        editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
        editor.apply();
    }
}
