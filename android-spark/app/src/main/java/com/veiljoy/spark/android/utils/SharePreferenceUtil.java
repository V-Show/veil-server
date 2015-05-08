package com.veiljoy.spark.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015/5/8.
 */
public class SharePreferenceUtil {
    public static final String SHARE_PREFERENCE_NAME = "veil";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    public static SharedPreferences sp;
    public static SharedPreferences.Editor editor;

    public static void init(Context context) {
        sp = context.getSharedPreferences(SHARE_PREFERENCE_NAME, context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public static String getUsername() {
        return sp.getString(KEY_USERNAME, "");
    }

    public static void setPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public static String getPassword() {
        return sp.getString(KEY_PASSWORD, "");
    }
}
