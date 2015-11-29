package com.jiubai.taskmoment.presenter;

import android.os.Handler;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.view.IGetVerifyCodeView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by howell on 2015/11/28.
 * GetVerifyCodePresenter实现类
 */
public class GetVerifyCodePresenterImpl implements IGetVerifyCodePresenter {

    private IGetVerifyCodeView iGetVerifyCodeView;

    public GetVerifyCodePresenterImpl(IGetVerifyCodeView iGetVerifyCodeView) {
        this.iGetVerifyCodeView = iGetVerifyCodeView;
    }

    @Override
    public void doGetVerifyCode(final String phoneNum) {
        new Handler().post(new Runnable() {

            public void run() {

                iGetVerifyCodeView.onUpdateView();

                iGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.VISIBLE);

                if (Config.RANDOM == null) {
                    UtilBox.getRandom();
                }

                String[] soapKey = {"type", "table_name", "feedback_url", "return"};
                String[] soapValue = {"sms_send_verifycode", Config.RANDOM, "", "1"};
                String[] httpKey = {"mobile"};
                String[] httpValue = {phoneNum};
                VolleyUtil.requestWithSoap(soapKey, soapValue, httpKey, httpValue,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                iGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                                try {
                                    JSONObject obj = new JSONObject(response);
                                    iGetVerifyCodeView.onGetVerifyCodeResult(true, obj.getString("info"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                iGetVerifyCodeView.onSetRotateLoadingVisibility(Constants.INVISIBLE);

                                iGetVerifyCodeView.onGetVerifyCodeResult(false, "");

                                if (volleyError != null
                                        && volleyError.getMessage() != null) {
                                    System.out.println(volleyError.getMessage());
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onSetRotateLoadingVisibility(int visibility) {
        iGetVerifyCodeView.onSetRotateLoadingVisibility(visibility);
    }
}
