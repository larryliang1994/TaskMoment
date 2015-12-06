package com.jiubai.taskmoment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.view.activity.CompanyActivity;
import com.jiubai.taskmoment.view.activity.LoginActivity;
import com.jiubai.taskmoment.view.activity.MainActivity;
import com.jiubai.taskmoment.widget.RotateLoading;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EntryActivity extends Activity {

    @Bind(R.id.ll_no_network)
    LinearLayout ll_no_network;

    private Dialog dialog;
    private RotateLoading rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.welcomeStatus);

        setContentView(R.layout.aty_welcome);

        ButterKnife.bind(this);

        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.setDeltaUpdate(true);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                    case UpdateStatus.NoneWifi:
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(EntryActivity.this, updateInfo);
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
                        Toast.makeText(EntryActivity.this, "开始下载更新", Toast.LENGTH_SHORT).show();
                        break;
                    case UpdateStatus.Ignore:
                    case UpdateStatus.NotNow:
                        getStart();
                        break;
                }
            }
        });
        UmengUpdateAgent.update(this);

        dialog = new Dialog(this, R.style.dialog);
        dialog.setContentView(R.layout.dialog_rotate_loading);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        rl = (RotateLoading) dialog.findViewById(R.id.rl_dialog);
    }

    // 进入正式页面
    private void getStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Config.COOKIE == null) {
                    startActivity(new Intent(EntryActivity.this, LoginActivity.class));
                    finish();
                    overridePendingTransition(R.anim.in_right_left, R.anim.scale_stay);
                } else if (Config.CID == null) {
                    if (!Config.IS_CONNECTED) {
                        changeLoadingState("dismiss");
                        ll_no_network.setVisibility(View.VISIBLE);

                        Toast.makeText(EntryActivity.this,
                                R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();

                                // 获取用户信息
                                getUserInfo();

                                changeLoadingState("dismiss");

                                startActivity(new Intent(EntryActivity.this, CompanyActivity.class));
                                finish();
                                overridePendingTransition(R.anim.in_right_left, R.anim.scale_stay);

                                Looper.loop();
                            }
                        }).start();
                    }
                } else {
                    if (!Config.IS_CONNECTED) {
                        changeLoadingState("dismiss");
                        ll_no_network.setVisibility(View.VISIBLE);

                        Toast.makeText(EntryActivity.this,
                                R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();

                                // 获取用户信息
                                getUserInfo();

                                changeLoadingState("dismiss");

                                startActivity(new Intent(EntryActivity.this, MainActivity.class));
                                finish();
                                overridePendingTransition(R.anim.zoom_in_scale,
                                        R.anim.zoom_out_scale);

                                Looper.loop();
                            }
                        }).start();
                    }
                }
            }
        }, 1500);
    }

    @OnClick(R.id.btn_reconnect)
    public void onClick(View v) {
        getStart();
        changeLoadingState("show");
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param which show代表显示, dismiss代表隐藏
     */
    private void changeLoadingState(String which) {
        if ("show".equals(which)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                    rl.start();
                }
            });
        } else if ("dismiss".equals(which)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl.stop();
                    dialog.dismiss();
                }
            });
        }
    }

    private void getUserInfo() {
        VolleyUtil.requestWithCookie(Urls.GET_USER_INFO, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);

                            if (Constants.SUCCESS.equals(object.getString("status"))) {
                                JSONObject data = new JSONObject(object.getString("data"));

                                Config.MID = data.getString("id");
                                Config.NICKNAME = data.getString("real_name");
                                Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg";

                                SharedPreferences.Editor editor = App.sp.edit();
                                editor.putString(Constants.SP_KEY_MID, Config.MID);
                                editor.putString(Constants.SP_KEY_NICKNAME, Config.NICKNAME);
                                editor.putString(Constants.SP_KEY_PORTRAIT, Config.PORTRAIT);
                                editor.apply();
                            } else {
                                Toast.makeText(EntryActivity.this,
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
                        Toast.makeText(EntryActivity.this,
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