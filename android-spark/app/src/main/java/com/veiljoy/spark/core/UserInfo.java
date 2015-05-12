package com.veiljoy.spark.core;

/**
 * Created by Administrator on 2015/5/8.
 */
public class UserInfo {
    private String username;
    private String nickname;
    private byte[] avatar;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }
}
