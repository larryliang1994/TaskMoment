package com.jiubai.taskmoment.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.JoinedCompanyAdapter;
import com.jiubai.taskmoment.adapter.MyCompanyAdapter;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.SlidingLayout;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.CompanyPresenterImpl;
import com.jiubai.taskmoment.presenter.ICompanyPresenter;
import com.jiubai.taskmoment.view.iview.ICompanyView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 我创建的公司与我加入的公司
 */
public class CompanyActivity extends BaseActivity implements ICompanyView{
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

    private ICompanyPresenter companyPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_company);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        tv_title.setText(R.string.myCompany);

        iBtn_more.setVisibility(View.VISIBLE);

        // 若不来自切换公司，则不需要返回键
        if (!getIntent().getBooleanExtra("hide", false)) {
            iBtn_back.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(UtilBox.dip2px(this, 16), 0, 0, 0);
            lp.weight = 1;
            lp.width = 0;
            lp.height = LinearLayout.LayoutParams.MATCH_PARENT;

            tv_title.setLayoutParams(lp);

            new SlidingLayout(this).setEnable(false);
        } else {
            new SlidingLayout(this);
        }

        UtilBox.setStatusBarTint(this, R.color.statusBar);

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

        companyPresenter = new CompanyPresenterImpl(this);
        companyPresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);

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
                CompanyActivity.this.setResult(RESULT_CANCELED);
                CompanyActivity.this.finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
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

                        final MaterialDialog dialog = new MaterialDialog(CompanyActivity.this);
                        dialog.setTitle("注销")
                                .setMessage("真的要注销吗?")
                                .setPositiveButton("真的", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();

                                        UtilBox.clearAllData(CompanyActivity.this);

                                        startActivity(new Intent(CompanyActivity.this, LoginActivity.class));
                                        finish();
                                        overridePendingTransition(
                                                R.anim.scale_stay, R.anim.out_left_right);
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

        companyPresenter.getMyCompany();
        companyPresenter.getJoinedCompany();
    }

    @Override
    public void onGetMyCompanyResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){ // 获取成功
            lv_myCompany.setAdapter(new MyCompanyAdapter(CompanyActivity.this, info));
            UtilBox.setListViewHeightBasedOnChildren(lv_myCompany);
        } else if (Constants.EXPIRE.equals(result)){ // 登录信息过期
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
        } else if (Constants.FAILED.equals(result)){
            Toast.makeText(CompanyActivity.this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetJoinedCompanyResult(String result, String info) {
        if(Constants.SUCCESS.equals(result)){
            lv_joinedCompany.setAdapter(new JoinedCompanyAdapter(CompanyActivity.this, info));
            UtilBox.setListViewHeightBasedOnChildren(lv_joinedCompany);
        } else if (Constants.FAILED.equals(result)){
            Toast.makeText(CompanyActivity.this, info, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        if(visibility == Constants.VISIBLE){
            srl.setRefreshing(true);
        } else if (visibility == Constants.INVISIBLE){
            srl.setRefreshing(false);
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
                CompanyActivity.this.setResult(RESULT_CANCELED);
                CompanyActivity.this.finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
