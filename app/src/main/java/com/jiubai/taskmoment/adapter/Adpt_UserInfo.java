package com.jiubai.taskmoment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.view.RippleView;

import java.util.ArrayList;

/**
 * 个人中心适配器
 */
public class Adpt_UserInfo extends BaseAdapter {
    private ArrayList<String> itemList;
    private Context context;

    public Adpt_UserInfo(Context context) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.clear();

        itemList.add("Leung_Howell");
        itemList.add("我发布的任务");
        itemList.add("我参与的任务");
        itemList.add("我审核的任务");

        this.context = context;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position == 0) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_userinfo_head, null);
            ((TextView) convertView.findViewById(R.id.tv_nickname)).setText(itemList.get(position));

            ((RippleView) convertView.findViewById(R.id.rv_portrait_nickname)).setOnRippleCompleteListener(
                    new RippleView.OnRippleCompleteListener() {
                        @Override
                        public void onComplete(RippleView rippleView) {

                        }
                    });
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_body, null);
            ((TextView) convertView.findViewById(R.id.tv_item_body)).setText(itemList.get(position));

            ((RippleView) convertView.findViewById(R.id.rv_item_body)).setOnRippleCompleteListener(
                    new RippleView.OnRippleCompleteListener() {
                        @Override
                        public void onComplete(RippleView rippleView) {

                        }
                    });

            if (position == getCount() - 1) {
                convertView.findViewById(R.id.iv_item_body).setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
