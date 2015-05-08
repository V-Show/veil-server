package com.veiljoy.spark.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.veiljoy.spark.android.net.xmpp.MUC;

/**
 * Created by Administrator on 2015/5/7.
 */
public class ChatActivity extends Activity {
    ChatHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mHandler = new ChatHandler();

        try {
            MUC.getInstance().registerHandler(mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getRoomInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            MUC.getInstance().unregisterHandler(mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getRoomInfo() {

    }

    class ChatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
        }
    }
}
