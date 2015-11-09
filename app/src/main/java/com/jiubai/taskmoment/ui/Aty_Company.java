package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_JoinedCompany;
import com.jiubai.taskmoment.adapter.Adpt_MyCompany;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.view.RotateLoading;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 我创建的公司与我加入的公司
 */
public class Aty_Company extends AppCompatActivity {
    @Bind(R.id.swipe_company)
    SwipeRefreshLayout srl;

    @Bind(R.id.lv_myCompany)
    ListView lv_myCompany;

    @Bind(R.id.lv_joinedCompany)
    ListView lv_joinedCompany;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iBtn_back)
    ImageButton iBtn_back;

    @Bind(R.id.iBtn_more)
    ImageButton iBtn_more;

    private static Dialog dialog;
    private static RotateLoading rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_company);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView() {
        tv_title.setText(R.string.myCompany);

        // 若不来自切换公司，则不需要返回键
        if (!getIntent().getBooleanExtra("hide", false)) {
            iBtn_back.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(UtilBox.dip2px(this, 16), 0, 0, 0);
            lp.gravity = Gravity.CENTER_VERTICAL;

            tv_title.setLayoutParams(lp);
        }

        iBtn_more.setVisibility(View.VISIBLE);

        srl.setColorSchemeResources(R.color.primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListView();
            }
        });

        srl.setEnabled(true);

        // 到达顶端才能下拉刷新
        lv_myCompany.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    srl.setEnabled(true);
                } else {
                    srl.setEnabled(false);
                }
            }
        });

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshListView();
            }
        }, 100);

    }

    @OnClick({R.id.iBtn_back, R.id.iBtn_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                Aty_Company.this.setResult(RESULT_CANCELED);
                Aty_Company.this.finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
                break;

            case R.id.iBtn_more:
                @SuppressLint("InflateParams")
                View contentView = LayoutInflater.from(this).inflate(
                        R.layout.popup_logout, null);

                final PopupWindow popupWindow = new PopupWindow(contentView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, true);

                contentView.findViewById(R.id.tv_popupWindow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();

                        final MaterialDialog dialog = new MaterialDialog(Aty_Company.this);
                        dialog.setTitle("注销")
                                .setMessage("真的要注销吗?")
                                .setPositiveButton("真的", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();

                                        UtilBox.clearAllData(Aty_Company.this);

                                        startActivity(new Intent(Aty_Company.this, Aty_Login.class));
                                        finish();
                                        overridePendingTransition(
                                                R.anim.in_left_right, R.anim.out_left_right);
                                    }
                                })
                                .setNegativeButton("假的", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                })
                                .setCanceledOnTouchOutside(true)
                                .show();
                    }
                });

                popupWindow.setBackgroundDrawable(
                        ContextCompat.getDrawable(this, R.drawable.white));

                popupWindow.showAsDropDown(view);
                break;
        }
    }

    /**
     * 从服务器获取公司数据
     */
    private void refreshListView() {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(this, R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        srl.setRefreshing(true);

        VolleyUtil.requestWithCookie(Urls.MY_COMPANY, null, null,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        showCompany("my_company", response);
                        UtilBox.setListViewHeightBasedOnChildren(lv_myCompany);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        srl.setRefreshing(false);
                        Toast.makeText(Aty_Company.this, "获取公司列表失败，下拉重试一下吧？",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        VolleyUtil.requestWithCookie(Urls.MY_JOIN_COMPANY, null, null,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        showCompany("my_join_company", response);
                        UtilBox.setListViewHeightBasedOnChildren(lv_joinedCompany);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        srl.setRefreshing(false);
                        Toast.makeText(Aty_Company.this, "获取公司列表失败，下拉重试一下吧？",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 如果cookie可用，则显示公司，否则返回登录页面
     *
     * @param which    my_company代表我创建的公司，my_join_company代表我加入的公司
     * @param response 我的公司的返回结果
     */
    private void showCompany(String which, String response) {
        try {
            JSONObject responseJson = new JSONObject(response);

            String responseStatus = responseJson.getString("status");

            if (("1".equals(responseStatus) || "900001".equals(responseStatus))
                    && (("1".equals(responseStatus) || "900001".equals(responseStatus)))) {

                if ("my_company".equals(which)) {
                    lv_myCompany.setAdapter(new Adpt_MyCompany(Aty_Company.this, response));
                } else if ("my_join_company".equals(which)) {
                    lv_joinedCompany.setAdapter(new Adpt_JoinedCompany(Aty_Company.this, response));
                }

            } else {
                // cookie有误，清空cookie
                SharedPreferences sp = getSharedPreferences(Constants.SP_FILENAME,
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(Constants.SP_KEY_COOKIE);
                editor.apply();

                Config.COOKIE = null;

                Toast.makeText(getApplicationContext(), "登录信息已过期，请重新登录",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Aty_Login.class));
                finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(false);
            }
        }, 1000);
    }

    /**
     * 显示或隐藏旋转进度条
     *
     * @param which show代表显示, dismiss代表隐藏
     */
    public static void changeLoadingState(Context context, String which) {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.dialog);
            dialog.setContentView(R.layout.dialog_rotate_loading);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            rl = (RotateLoading) dialog.findViewById(R.id.rl_dialog);
        }

        if ("show".equals(which)) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                    rl.start();
                }
            });
        } else if ("dismiss".equals(which)) {
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rl.stop();
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_ADD_COMPANY:
                if (resultCode == RESULT_OK) {
                    refreshListView();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 点击返回，回到桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!getIntent().getBooleanExtra("hide", false)) {
                Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                MyIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(MyIntent);
            } else {
                Aty_Company.this.setResult(RESULT_CANCELED);
                Aty_Company.this.finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
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
