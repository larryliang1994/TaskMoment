package com.jiubai.taskmoment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.jiubai.taskmoment.config.Constants;

/**
 * 截取短信
 */
public class Receiver_SmsReader extends BroadcastReceiver {
    private static MessageListener mMessageListener;

    public Receiver_SmsReader() {
        super();
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] objs = (Object[]) bundle.get("pdus");

        if (objs != null) {
            SmsMessage[] msgs = new SmsMessage[objs.length];

            System.out.println("!!");

            for (int i = 0; i < msgs.length; i++) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) objs[i]);

                // 获取短信的发送者号码和短信内容
                String sender = msg.getDisplayOriginatingAddress();
                String content = msg.getMessageBody();

                System.out.println(sender);

                // 如果短信来自服务器,不再往下传递
                if (Constants.SERVER_PHONE_NUMBER_TELECOM.equals(sender)
                        || Constants.SERVER_PHONE_NUMBER＿UNICOM.equals(sender)) {
                    mMessageListener.OnReceived(content);
                    abortBroadcast();
                }
            }
        } else {
            System.out.println("null");
        }
    }

    // 回调接口
    public interface MessageListener {
        void OnReceived(String message);
    }

    public void setOnReceivedMessageListener(MessageListener messageListener) {
        mMessageListener = messageListener;
    }
}
