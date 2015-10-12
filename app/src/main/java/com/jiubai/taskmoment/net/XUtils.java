package com.jiubai.taskmoment.net;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.File;

/**
 * XUtils框架
 */
public class XUtils {
    /**
     * 上传图片
     *
     * @param url 上传地址
     * @param path 图片路径
     * @param callBack 请求回调
     */
    public static void uploadImage(final String url, final String memberCookie, final String bodyKey,
                                   final String path, RequestCallBack<String> callBack) {
        RequestParams params = new RequestParams();
        //params.addQueryStringParameter("memberCookie", memberCookie);

        params.addBodyParameter("memberCookie", memberCookie);
        params.addQueryStringParameter("memberCookie", memberCookie);

        // 加入文件参数后默认使用MultipartEntity（"multipart/form-data"），
        // 如需"multipart/related"，xUtils中提供的MultipartEntity支持设置subType为"related"。
        params.setContentType("multipart/form-data");
        params.addBodyParameter(bodyKey, new File(path));

        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, Urls.SERVER_URL + url + "?memberCookie=" + memberCookie,
                params, callBack);
    }
}
