package com.jiubai.taskmoment.net;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;

import java.util.HashMap;
import java.util.Map;

/**
 * Volley框架
 */
public class VolleyUtil {
    public static RequestQueue requestQueue = null;

    public static void initRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    /**
     * 进行soap通信获取后缀，然后进行http请求
     *
     * @param soapKey         soap通信的键
     * @param soapValue       soap通信的值
     * @param httpKey         http请求的键
     * @param httpValue       http请求的值
     * @param successCallback 通信成功回调
     * @param errorCallback   通信失败回调
     */
    public static void requestWithSoap(final String[] soapKey, final String[] soapValue,
                                       final String[] httpKey, final String[] httpValue,
                                       Response.Listener<String> successCallback,
                                       Response.ErrorListener errorCallback) {
        // 先进行soap通信，获取url后缀
        String soapUrl = SoapUtil.getUrlBySoap("ajax", soapKey, soapValue);

        // 构建Post请求对象
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Urls.SERVER_URL + "/act/ajax.php?a=" + soapUrl,
                successCallback, errorCallback) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (httpKey != null) {
                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < httpKey.length; i++) {
                        map.put(httpKey[i], httpValue[i]);
                    }
                    return map;
                } else {
                    return super.getParams();
                }
            }
        };

        // 加入请求队列
        requestQueue.add(stringRequest);
    }

    /**
     * 进行带有Cookie的网络请求
     *
     * @param url             请求参数
     * @param key             请求参数的键
     * @param value           请求参数的值
     * @param successCallback 通信成功回调
     * @param errorCallback   通信失败回调
     */
    public static void requestWithCookie(final String url, final String[] key, final String[] value,
                                         Response.Listener<String> successCallback,
                                         Response.ErrorListener errorCallback) {
        // 构建Post请求对象
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Urls.SERVER_URL + "/ajax.php?a=" + url,
                successCallback, errorCallback) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Cookie", "memberCookie=" + Config.COOKIE);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (key != null) {
                    Map<String, String> params = new HashMap<>();
                    for (int i = 0; i < key.length; i++) {
                        params.put(key[i], value[i]);
                    }
                    return params;
                } else {
                    return super.getParams();
                }
            }
        };

        // 加入请求队列
        requestQueue.add(stringRequest);
    }

    /**
     * 请求图片资源
     *
     * @param url             图片的url
     * @param successCallback 获取成功回调
     * @param maxWidth        最大宽度
     * @param maxHeight       最大高度
     * @param errorCallback   获取失败回调
     */
    public static void imageRequest(final String url, Response.Listener<Bitmap> successCallback,
                                    int maxWidth, int maxHeight, Response.ErrorListener errorCallback) {
        ImageRequest imageRequest = new ImageRequest(url, successCallback,
                maxWidth, maxHeight, Bitmap.Config.ALPHA_8, errorCallback);

        requestQueue.add(imageRequest);
    }

    /**
     * 设置网络图片
     *
     * @param niv               需要设置的图片控件
     * @param url               图片的url
     * @param defaultImageResId 未加载完成的图片
     * @param errorImageResId   加载失败的图片
     */
    public static void setNetWorkImageWithCache(NetworkImageView niv, final String url,
                                                int defaultImageResId, int errorImageResId) {
        LruImageCache lruImageCache = LruImageCache.instance();

        ImageLoader imageLoader = new ImageLoader(requestQueue, lruImageCache);

        niv.setDefaultImageResId(defaultImageResId);
        niv.setErrorImageResId(errorImageResId);
        niv.setImageUrl(url, imageLoader);
    }
}
