package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.receiver.Receiver_UpdateView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 主activity界面
 */
public class Aty_Main extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.dw_main)
    DrawerLayout dw;

    @Bind(R.id.iBtn_back)
    ImageButton iBtn_back;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iBtn_publish)
    ImageButton iBtn_publish;

    @Bind(R.id.nv_main)
    NavigationView nv;

    private Frag_Timeline frag_timeline = new Frag_Timeline();
    private Frag_Member frag_member = new Frag_Member();
    private Frag_UserInfo frag_userInfo = new Frag_UserInfo();
    private Frag_Preference frag_preference = new Frag_Preference();

    public static LinearLayout toolbar;
    private LinearLayout ll_nvHeader;
    private FragmentManager fragmentManager;
    private CircleImageView iv_navigation;
    private TextView tv_nickname;

    Receiver_UpdateView nicknameReceiver, portraitReceiver;

    private int currentItem = 0;
    private long doubleClickTime = 0;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_main);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        toolbar = (LinearLayout) findViewById(R.id.toolBar);

        tv_title.setText(Config.COMPANY_NAME + "的" + getResources().getString(R.string.timeline));
        tv_title.setOnClickListener(this);

        iBtn_back.setImageResource(R.drawable.navigation);

        iBtn_publish.setVisibility(View.VISIBLE);

        // 默认显示任务圈
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag_main, frag_timeline).commit();

        // 设置抽屉
        ll_nvHeader = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.navigation_header, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.height = UtilBox.dip2px(this, 212);
        ll_nvHeader.setLayoutParams(lp);

        // 设置昵称
        tv_nickname = (TextView) ll_nvHeader.findViewById(R.id.tv_navigation_nickname);
        if (!"".equals(Config.NICKNAME) && !"null".equals(Config.NICKNAME)) {
            tv_nickname.setText(Config.NICKNAME);
        }

        // 获取抽屉的头像
        iv_navigation = (CircleImageView) ll_nvHeader.findViewById(R.id.iv_navigation);
        if (Config.PORTRAIT != null) {
            ImageLoader.getInstance().displayImage(Config.PORTRAIT, iv_navigation);
        } else {
            iv_navigation.setImageResource(R.drawable.portrait_default);
        }

        nv.addHeaderView(ll_nvHeader);
        nv.getMenu().getItem(0).setChecked(true);
        nv.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#212121")));
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                dw.closeDrawer(GravityCompat.START);
                switch (menuItem.getItemId()) {
                    case R.id.navItem_timeLine:
                        if (currentItem == 0) {
                            break;
                        }
                        nv.getMenu().getItem(0).setChecked(true);
                        tv_title.setText(Config.COMPANY_NAME + "的" + getResources().getString(R.string.timeline));
                        nv.getMenu().getItem(currentItem).setChecked(false);

                        switchContent(frag_timeline);

                        tv_title.setOnClickListener(Aty_Main.this);

                        currentItem = 0;

                        iBtn_publish.setVisibility(View.VISIBLE);
                        break;

                    case R.id.navItem_member:
                        if (currentItem == 1) {
                            break;
                        }
                        nv.getMenu().getItem(1).setChecked(true);
                        tv_title.setText(R.string.member);
                        nv.getMenu().getItem(currentItem).setChecked(false);

                        switchContent(frag_member);

                        tv_title.setOnClickListener(null);

                        currentItem = 1;

                        iBtn_publish.setVisibility(View.GONE);
                        break;

                    case R.id.navItem_userInfo:
                        if (currentItem == 2) {
                            break;
                        }
                        nv.getMenu().getItem(2).setChecked(true);
                        tv_title.setText(R.string.userInfo);
                        nv.getMenu().getItem(currentItem).setChecked(false);

                        switchContent(frag_userInfo);

                        tv_title.setOnClickListener(null);

                        currentItem = 2;

                        iBtn_publish.setVisibility(View.GONE);
                        break;

                    case R.id.navItem_preference:
                        if (currentItem == 3) {
                            break;
                        }
                        nv.getMenu().getItem(3).setChecked(true);
                        tv_title.setText(R.string.timeline);
                        nv.getMenu().getItem(currentItem).setChecked(false);

                        switchContent(frag_preference);

                        tv_title.setOnClickListener(null);

                        currentItem = 3;

                        iBtn_publish.setVisibility(View.GONE);
                        break;

                    case R.id.navItem_chooseCompany:
                        Intent intent = new Intent(Aty_Main.this, Aty_Company.class);
                        intent.putExtra("hide", true);
                        startActivityForResult(intent, Constants.CODE_CHANGE_COMPANY);
                        overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
                        break;
                }

                return true;
            }
        });
    }


    /**
     * 切换fragment
     *
     * @param to 需要切换到的fragment
     */
    public void switchContent(Fragment to) {
        Fragment from = null;
        switch (currentItem) {
            case 0:
                from = frag_timeline;
                break;

            case 1:
                from = frag_member;
                break;

            case 2:
                from = frag_userInfo;
                break;

            case 3:
                from = frag_preference;
                break;
        }

        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);

        if (!to.isAdded()) {    // 先判断是否被add过
            transaction.hide(from).add(R.id.frag_main, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
        } else {
            transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }


    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                dw.openDrawer(GravityCompat.START);
                break;

            case R.id.tv_title:
                if ((System.currentTimeMillis() - doubleClickTime) > 500) {
                    doubleClickTime = System.currentTimeMillis();
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Frag_Timeline.sv.fullScroll(View.FOCUS_UP);
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHANGE_COMPANY:
                if (resultCode == RESULT_OK) {
                    recreate();
                }
                break;
        }
    }

    /**
     * 点击返回，回到桌面
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (dw.isDrawerOpen(GravityCompat.START)) {
                dw.closeDrawer(GravityCompat.START);
            } else if (currentItem == 0 && Frag_Timeline.commentWindowIsShow) {
                UtilBox.setViewParams(Frag_Timeline.space, 1, 0);
                Frag_Timeline.ll_comment.setVisibility(View.GONE);
                Frag_Timeline.commentWindowIsShow = false;
                UtilBox.toggleSoftInput(Frag_Timeline.ll_comment, false);
            } else if ((currentItem == 0 && Frag_Timeline.auditWindowIsShow)) {
                Frag_Timeline.ll_audit.setVisibility(View.GONE);
                Frag_Timeline.auditWindowIsShow = false;
            } else {
                Intent MyIntent = new Intent(Intent.ACTION_MAIN);
                MyIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(MyIntent);
            }

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) { // 小米居然没用。。
            Toast.makeText(this, "menu", Toast.LENGTH_SHORT).show();
            if (!dw.isDrawerOpen(GravityCompat.START)) {
                dw.openDrawer(GravityCompat.START);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        nicknameReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        tv_nickname.setText(msg);
                        nv.removeHeaderView(ll_nvHeader);
                        nv.addHeaderView(ll_nvHeader);
                    }
                });
        nicknameReceiver.registerAction(Constants.ACTION_CHANGE_NICKNAME);

        portraitReceiver = new Receiver_UpdateView(this,
                new Receiver_UpdateView.UpdateCallBack() {
                    @Override
                    public void updateView(String msg, Object... objects) {
                        ImageLoader.getInstance().displayImage(
                                Config.PORTRAIT, iv_navigation);
                        nv.removeHeaderView(ll_nvHeader);
                        nv.addHeaderView(ll_nvHeader);
                    }
                });
        portraitReceiver.registerAction(Constants.ACTION_CHANGE_PORTRAIT);

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(nicknameReceiver);
        unregisterReceiver(portraitReceiver);

        super.onDestroy();
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
