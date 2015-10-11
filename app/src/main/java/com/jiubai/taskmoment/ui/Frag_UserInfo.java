package com.jiubai.taskmoment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_UserInfo;

/**
 * 个人中心
 */
public class Frag_UserInfo extends Fragment{
    private ListView lv_userInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_userinfo, container, false);

        initView(view);

        return view;
    }

    /**
     * 初始化界面
     */
    private void initView(View view) {
        lv_userInfo = (ListView) view.findViewById(R.id.lv_userInfo);
        lv_userInfo.setAdapter(new Adpt_UserInfo(getActivity()));
    }

}
