package com.jiubai.taskmoment.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;

/**
 * 新消息接收器
 */
public class Receiver_UpdateView extends BroadcastReceiver {
    private UpdateCallBack callBack;
    private Context context;

    public Receiver_UpdateView() {
    }

    public Receiver_UpdateView(Context context, UpdateCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }

    // 注册
    public void registerAction(String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.ACTION_NEWS.equals(intent.getAction())) {
            // 添加一条未读消息
            Config.NEWS_NUM++;

            if (callBack != null) {
                callBack.updateView(intent.getStringExtra("msg"));
            }

            // 防止多次调用
            abortBroadcast();
        } else if (Constants.ACTION_CHANGE_NICKNAME.equals(intent.getAction())) {
            if (callBack != null) {
                callBack.updateView(intent.getStringExtra("nickname"));
            }
        } else if (Constants.ACTION_CHANGE_PORTRAIT.equals(intent.getAction())) {
            if (callBack != null) {
                callBack.updateView(null);
            }
        }
    }

    public interface UpdateCallBack {
        void updateView(String msg);
    }
}
