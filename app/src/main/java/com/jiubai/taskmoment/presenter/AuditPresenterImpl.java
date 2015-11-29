package com.jiubai.taskmoment.presenter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.view.IAuditView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/29.
 * AuditPresenter实现类
 */
public class AuditPresenterImpl implements IAuditPresenter {
    private IAuditView iAuditView;

    public AuditPresenterImpl(IAuditView iAuditView) {
        this.iAuditView = iAuditView;
    }

    @Override
    public void doAudit(String taskID, String audit_result) {
        String[] key = {"id", "level"};
        String[] value = {taskID, audit_result};

        VolleyUtil.requestWithCookie(Urls.SEND_AUDIT, key, value,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseJson = new JSONObject(response);

                            iAuditView.onAuditResult(
                                    responseJson.getString("status"),
                                    responseJson.getString("info"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();

                        iAuditView.onAuditResult(Constants.FAILED, "审核失败，请重试");
                    }
                });
    }
}
