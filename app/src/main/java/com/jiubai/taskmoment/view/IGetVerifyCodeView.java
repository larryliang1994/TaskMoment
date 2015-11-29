package com.jiubai.taskmoment.view;

/**
 * Created by howell on 2015/11/28.
 * GetVerifyCodeView接口
 */
public interface IGetVerifyCodeView {
    void onGetVerifyCodeResult(boolean result, String info);
    void onUpdateView();
    void onSetRotateLoadingVisibility(int visibility);
}
