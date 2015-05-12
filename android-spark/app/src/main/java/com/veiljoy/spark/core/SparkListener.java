package com.veiljoy.spark.core;

import com.veiljoy.spark.android.core.SparkApplication;

/**
 * Created by Administrator on 2015/5/11.
 */
public interface SparkListener {
    void onConnect();

    void onLogin();

    void onRegister();

    void onUploadVCard();

    void onRub(String room, boolean create);

    void onEnterRoom();

    void onSendMessage(int id);

    void onReceiveMessage(String from, String body, String subject);

    void onError(SparkError error, SparkAction action);

    void onJoin(int index, UserInfo[] users);

    void onLeft(int index, UserInfo[] users);

    void onKick(String nickname);
}
