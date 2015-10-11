package com.jiubai.taskmoment.adapter;

/**
 * 自定义Fragment适配器
 */

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class Adpt_FragmentPager extends FragmentPagerAdapter {
    private ArrayList<Fragment> list;

    public Adpt_FragmentPager(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        this.list = list;
    }

    //获取当前现实的Fragment
    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    //获取Fragment的个数
    @Override
    public int getCount() {
        return list.size();
    }
}
