package com.jiubai.taskmoment.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jiubai.taskmoment.R;

/**
 * 偏好设置
 */
public class Frag_Preference extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_preference, container, false);

        initView(view);

        return view;
    }

    @SuppressWarnings("unused")
    private void initView(View view){

    }
}
