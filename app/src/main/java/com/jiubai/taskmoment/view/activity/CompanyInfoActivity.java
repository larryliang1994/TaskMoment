package com.jiubai.taskmoment.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberAdapter;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.widget.SlidingLayout;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 公司信息页面
 */
public class CompanyInfoActivity extends BaseActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_companyName)
    TextView tv_companyName;

    @Bind(R.id.tv_companyCreator)
    TextView tv_companyCreator;

    @Bind(R.id.iv_companyInfo_qr)
    ImageView iv_qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_company_info);

        new SlidingLayout(this);

        UtilBox.setStatusBarTint(this, R.color.statusBar);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText(R.string.companyInfo);

        tv_companyName.setText(Config.COMPANY_NAME);

        MemberAdapter.getMember(this, new MemberAdapter.GetMemberCallBack() {
            @Override
            public void successCallback() {
                setCreator();
            }

            @Override
            public void failedCallback() {
            }
        });

        JSONObject object = new JSONObject();
        try {
            object.put("type", Constants.QR_TYPE_COMPANYINFO);
            object.put("name", Config.COMPANY_NAME);
            object.put("cid", Config.CID);
            object.put("creator", Config.COMPANY_CREATOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        iv_qr.setImageBitmap(UtilBox.getQRImage(object.toString(), UtilBox.dip2px(this, 200), UtilBox.dip2px(this, 200)));
    }

    private void setCreator() {
        String name = null;

        for (int i = 0; i < MemberAdapter.memberList.size(); i++) {
            Member member = MemberAdapter.memberList.get(i);
            if (member.getMid().equals(Config.COMPANY_CREATOR)) {
                if ("".equals(member.getName()) || "null".equals(member.getName())) {
                    name = member.getMobile();
                } else {
                    name = member.getName();
                }
                break;
            }
        }

        tv_companyCreator.setText(name);
    }

    @OnClick({R.id.iBtn_back, R.id.tv_companyName})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.scale_stay, R.anim.out_left_right);
                break;

            case R.id.tv_companyName:
//                String[] key = {"id", "name"};
//                String[] value = {Config.CID, "玖佰网测试"};
//                VolleyUtil.requestWithCookie(Urls.UPDATE_COMPANY_INFO, key, value,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String s) {
//                                System.out.println(s);
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError volleyError) {
//
//                            }
//                        });
                break;
        }
    }
}
