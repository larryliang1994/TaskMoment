package com.jiubai.taskmoment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
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

                    UtilBox.getMember(MainActivity.this, new UtilBox.GetMemberCallBack() {
                        @Override
                        public void successCallback() {
                            startActivity(new Intent(MainActivity.this, Aty_Main.class));
                        }

                        @Override
                        public void failedCallback() {
                        }
                    });
                }
            }).start();
        }

        finish();
    }

    private void getUserInfo() {
        VolleyUtil.requestWithCookie(Urls.GET_USER_INFO, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);

                            if("900001".equals(object.getString("status"))){
                                JSONObject data = new JSONObject(object.getString("data"));

                                Config.MID = data.getString("id");
                                Config.NICKNAME = data.getString("real_name");
                                Config.PORTRAIT = Constants.HOST_ID + "task_moment/" + Config.MID + ".jpg";

                                SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString(Constants.SP_KEY_MID, Config.MID);
                                editor.putString(Constants.SP_KEY_NICKNAME, Config.NICKNAME);
                                editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
                                editor.apply();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        object.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(MainActivity.this,
                                R.string.usual_error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
