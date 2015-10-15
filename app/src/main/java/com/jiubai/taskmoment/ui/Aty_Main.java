package com.jiubai.taskmoment.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 主activity界面
 */
public class Aty_Main extends AppCompatActivity {
    @Bind(R.id.dw_main)
    DrawerLayout dw;

    @Bind(R.id.nv_main)
    NavigationView nv;

    @Bind(R.id.iBtn_back)
    ImageButton iBtn_back;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iBtn_publish)
    ImageButton iBtn_publish;

    @Bind(R.id.iv_navigation)
    CircleImageView iv_navigation;

    @Bind(R.id.tv_navigation_nickname)
    TextView tv_nickname;

    private Frag_Timeline frag_timeline = new Frag_Timeline();
    private Frag_Member frag_member = new Frag_Member();
    private Frag_UserInfo frag_userInfo = new Frag_UserInfo();
    private Frag_Preference frag_preference = new Frag_Preference();

    private int currentItem = 0;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_main);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        tv_title.setText(Config.COMPANY_NAME + "的" + getResources().getString(R.string.timeline));

        tv_nickname.setText("Leung_Howell");

        iBtn_back.setImageResource(R.drawable.navigation);

        iBtn_publish.setVisibility(View.VISIBLE);

        // 默认显示任务圈
        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag_main, frag_timeline).commit();

        nv.getMenu().getItem(0).setChecked(true);
        nv.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#212121")));
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                dw.closeDrawer(GravityCompat.START);
                FragmentTransaction fragmentTransaction;
                switch (menuItem.getItemId()) {
                    case R.id.navItem_timeLine:
                        if (currentItem == 0) {
                            break;
                        }
                        nv.getMenu().getItem(0).setChecked(true);
                        tv_title.setText(Config.COMPANY_NAME + "的" + getResources().getString(R.string.timeline));
                        nv.getMenu().getItem(currentItem).setChecked(false);

                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
                        fragmentTransaction.replace(R.id.frag_main, frag_timeline);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
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

                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
                        fragmentTransaction.replace(R.id.frag_main, frag_member);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

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

                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
                        fragmentTransaction.replace(R.id.frag_main, frag_userInfo);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

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

                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
                        fragmentTransaction.replace(R.id.frag_main, frag_preference);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

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

        // 获取抽屉的头像
        ImageLoader loader = ImageLoader.getInstance();
        loader.displayImage(Urls.PICTURE_3, iv_navigation);
    }

    @OnClick({R.id.iBtn_back, R.id.iBtn_publish})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                dw.openDrawer(GravityCompat.START);
                break;

            case R.id.iBtn_publish:
                startActivityForResult(
                        new Intent(this, Aty_TaskPublish.class), Constants.CODE_PUBLISH_TASK);
                overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
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

            case Constants.CODE_PUBLISH_TASK:
                if (resultCode == RESULT_OK) {

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
            } else if(currentItem == 0 && Frag_Timeline.commentWindowIsShow){
                Frag_Timeline.ll_comment.setVisibility(View.GONE);
                Frag_Timeline.commentWindowIsShow = false;
                UtilBox.toggleSoftInput(Frag_Timeline.ll_comment, false);
            }
            else {
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
