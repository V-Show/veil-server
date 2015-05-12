package com.veiljoy.spark.core;

/**
 * Created by Administrator on 2015/5/11.
 */
public class SimpleSparkListener implements SparkListener {
    @Override
    public void onConnect() {
    }

    @Override
    public void onLogin() {
    }

    @Override
    public void onRegister() {
    }

    @Override
    public void onUploadVCard() {
    }

    @Override
    public void onRub(String room, boolean create) {
    }

    @Override
    public void onEnterRoom() {
    }

    @Override
    public void onSendMessage(int id) {
    }

    @Override
    public void onReceiveMessage(String from, String body, String subject) {
    }

    @Override
    public void onError(SparkError error, SparkAction action) {
    }

    @Override
    public void onJoin(int index, UserInfo[] users) {
    }

    @Override
    public void onLeft(int index, UserInfo[] users) {
    }

    @Override
    public void onKick(String nickname) {
    }
}
