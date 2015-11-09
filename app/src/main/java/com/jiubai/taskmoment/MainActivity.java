package com.jiubai.taskmoment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.guidepage);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setDeltaUpdate(true);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.NoneWifi:
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(MainActivity.this, updateInfo);
                        break;

                    case UpdateStatus.No: // has no update
                    case UpdateStatus.Timeout: // time out
                        getStart();
                        break;
                }
            }
        });
        UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

            @Override
            public void onClick(int status) {
                switch (status) {
                    case UpdateStatus.Update:
                        Toast.makeText(MainActivity.this, "开始下载更新", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateStatus.Ignore:
                    case UpdateStatus.NotNow:
                        getStart();
                        break;
                }
            }
        });
        UmengUpdateAgent.update(this);
    }

    // 进入正式页面
    private void getStart(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Config.COOKIE == null) {
                    startActivity(new Intent(MainActivity.this, Aty_Login.class));
                    finish();
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

                            finish();
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!Config.IS_CONNECTED) {
                                Toast.makeText(MainActivity.this,
                                        R.string.cant_access_network,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // 获取用户信息
                                getUserInfo();
                            }

                            UtilBox.getMember(MainActivity.this, new UtilBox.GetMemberCallBack() {
                                @Override
                                public void successCallback() {
                                    startActivity(new Intent(MainActivity.this, Aty_Main.class));

                                    finish();
                                }

                                @Override
                                public void failedCallback() {
                                    startActivity(new Intent(MainActivity.this, Aty_Main.class));

                                    finish();
                                }
                            });
                        }
                    }).start();
                }
            }
        }, 1500);
    }

    private void getUserInfo() {
        VolleyUtil.requestWithCookie(Urls.GET_USER_INFO, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);

                            if ("900001".equals(object.getString("status"))) {
                                JSONObject data = new JSONObject(object.getString("data"));

                                Config.MID = data.getString("id");
                                Config.NICKNAME = data.getString("real_name");
                                Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg";

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
                                "获取用户信息失败，请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
