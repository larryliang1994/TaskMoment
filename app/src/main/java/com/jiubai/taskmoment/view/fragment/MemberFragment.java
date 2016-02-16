package com.jiubai.taskmoment.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 成员管理
 */
public class MemberFragment extends Fragment {
    public static ListView lv_member;
    private SwipeRefreshLayout srl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_member, container, false);

        initView(view);

        return view;
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView(View view) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.swipe_member);
        lv_member = (ListView) view.findViewById(R.id.lv_member);

        srl.setColorSchemeResources(R.color.primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListView();
            }
        });

        srl.setEnabled(true);

        // 到达顶端才能下拉刷新
        lv_member.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    srl.setEnabled(true);
                else
                    srl.setEnabled(false);
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

    /**
     * 刷新ListView
     */
    private void refreshListView() {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(getActivity(),
                    R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        srl.setRefreshing(true);

        VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showMember(response);
                        UtilBox.setListViewHeightBasedOnChildren(lv_member);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        srl.setRefreshing(false);
                        Toast.makeText(getActivity(),
                                "获取成员列表失败，请重试",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_QR_ADD_MEMBER:
                if(resultCode == Activity.RESULT_OK){
                    refreshListView();
                }
                break;
        }
    }

    /**
     * 显示成员
     *
     * @param response 通信返回的json
     */
    private void showMember(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);

            String responseStatus = responseJson.getString("status");

            if (Constants.SUCCESS.equals(responseStatus)) {
                MemberAdapter adpt_member = (new MemberAdapter(getActivity(), response, this));
                lv_member.setAdapter(adpt_member);
            } else {
                Toast.makeText(getActivity(),
                        "数据有误，请重试",
                        Toast.LENGTH_SHORT).show();
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
}