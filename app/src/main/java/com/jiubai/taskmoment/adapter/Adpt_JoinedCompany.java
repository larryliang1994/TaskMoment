package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.classes.Company;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.ui.Aty_Company;
import com.jiubai.taskmoment.ui.Aty_Main;
import com.jiubai.taskmoment.view.RippleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的加入的公司ListView适配器
 */
public class Adpt_JoinedCompany extends BaseAdapter {

    public List<Company> companyList;
    private Context context;
    public boolean isEmpty = false;

    public Adpt_JoinedCompany(Context context, String companyInfo) {
        try {
            this.context = context;

            companyList = new ArrayList<>();
            companyList.add(new Company("", "", ""));

            JSONObject companyJson = new JSONObject(companyInfo);

            if (!"null".equals(companyJson.getString("info"))) {
                isEmpty = false;
                JSONArray companyArray = companyJson.getJSONArray("info");
                for (int i = 0; i < companyArray.length(); i++) {
                    JSONObject obj = new JSONObject(companyArray.getString(i));
                    companyList.add(new Company(obj.getString("name"),
                            obj.getString("cid"), obj.getString("mid")));
                }
            } else {
                isEmpty = true;
                companyList.add(new Company("你还没有加入任何公司", "", ""));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return companyList.size();
    }

    @Override
    public Object getItem(int position) {
        return companyList.get(position);
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
            tv.setText("加入的公司");
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_body, null);

            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_body);
            tv.setText(companyList.get(position).getName());

            if (isEmpty || position == getCount() - 1) {
                convertView.findViewById(R.id.iv_item_divider).setVisibility(View.GONE);
            }

            if (!isEmpty) {
                RippleView rv = (RippleView) convertView.findViewById(R.id.rv_item_body);
                rv.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                        Aty_Company.changeLoadingState(context, "show");

                        Config.COMPANY_NAME = companyList.get(position).getName();
                        Config.CID = companyList.get(position).getCid();
                        Config.COMPANY_BACKGROUND
                                = Urls.MEDIA_CENTER_BACKGROUND + Config.CID + ".jpg";
                        Config.COMPANY_CREATOR = companyList.get(position).getCreator();

                        // 保存公司信息
                        SharedPreferences sp = context.getSharedPreferences(Constants.SP_FILENAME,
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(Constants.SP_KEY_COMPANY_NAME, Config.COMPANY_NAME);
                        editor.putString(Constants.SP_KEY_COMPANY_ID, Config.CID);
                        editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND, Config.COMPANY_BACKGROUND);
                        editor.putString(Constants.SP_KEY_COMPANY_CREATOR, Config.COMPANY_CREATOR);
                        editor.apply();

                        UtilBox.getMember(context, new UtilBox.GetMemberCallBack() {
                            @Override
                            public void successCallback() {
                                Aty_Company.changeLoadingState(context, "dismiss");

                                Intent intent = new Intent(context, Aty_Main.class);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                                ((Activity) context).overridePendingTransition(
                                        R.anim.in_right_left, R.anim.out_right_left);
                            }

                            @Override
                            public void failedCallback() {
                                Aty_Company.changeLoadingState(context, "dismiss");

                                Toast.makeText(context,
                                        "获取成员列表失败，请重试",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }

        return convertView;
    }

}
