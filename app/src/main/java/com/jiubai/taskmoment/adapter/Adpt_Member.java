package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.classes.Member;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 成员管理的ListView适配器
 */
public class Adpt_Member extends BaseAdapter {

    public static List<Member> memberList;
    private Context context;
    public boolean isEmpty = false;

    public Adpt_Member(Context context, String memberInfo) {
        try {
            this.context = context;

            memberList = new ArrayList<>();
            memberList.add(new Member("", "", "", ""));

            JSONObject memberJson = new JSONObject(memberInfo);

            if (!"null".equals(memberJson.getString("info"))) {
                isEmpty = false;
                JSONArray memberArray = memberJson.getJSONArray("info");

                for (int i = 0; i < memberArray.length(); i++) {
                    JSONObject obj = new JSONObject(memberArray.getString(i));
                    memberList.add(new Member(obj.getString("real_name"),
                            obj.getString("mobile"), obj.getString("id"), obj.getString("mid")));
                }
            } else {
                isEmpty = true;
                memberList.add(new Member("暂无成员", "", "", ""));
            }

            memberList.add(new Member("", "", "", ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_head, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_head);
            tv.setText("成员管理");
        } else if (position == memberList.size() - 1) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_foot_no_ripple, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_foot);
            tv.setText("添加成员");
        } else {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_with_button, null);

                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.tv_item_with_button);
                holder.btn = (Button) convertView.findViewById(R.id.btn_item_with_button);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!"null".equals(memberList.get(position).getName())
                    && !"".equals(memberList.get(position).getName())) {
                holder.tv.setText(memberList.get(position).getName());
            } else {
                holder.tv.setText(memberList.get(position).getMobile());
            }

            if (isEmpty) {
                holder.btn.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView tv;
        Button btn;
    }
}
