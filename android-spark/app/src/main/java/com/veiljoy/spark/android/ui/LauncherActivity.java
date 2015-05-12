package com.veiljoy.spark.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.veiljoy.spark.android.R;
import com.veiljoy.spark.android.core.SparkApplication;
import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.net.xmpp.MUC;
import com.veiljoy.spark.android.utils.SharePreferenceUtil;
import com.veiljoy.spark.core.SimpleSparkListener;
import com.veiljoy.spark.core.SparkAction;
import com.veiljoy.spark.core.SparkError;
import com.veiljoy.spark.core.SparkListener;

/**
 * Created by Administrator on 2015/5/1.
 */
public class LauncherActivity extends Activity {

    Runnable enterRegister;
    Runnable enterChat;
    int mErrorHandler = 2; // 0: none; 1: toast, 2: alert dialog
    String username;
    String password;

    SparkApplication mApp;
    LauncherSparkListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        mApp = (SparkApplication) getApplication();
        mListener = new LauncherSparkListener();

        // init share preference util at launcher activity
        SharePreferenceUtil.init(getApplicationContext());
        username = SharePreferenceUtil.getUsername();
        password = SharePreferenceUtil.getPassword();

        enterRegister = new Runnable() {
            public void run() {
                Intent intent = new Intent(LauncherActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        };
        enterChat = new Runnable() {
            public void run() {
                Intent intent = new Intent(LauncherActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        };

        mApp.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mApp.registerSparkListener(mListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mApp.unregisterSparkListener(mListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // eat key back
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    class LauncherSparkListener extends SimpleSparkListener {

        @Override
        public void onConnect() {
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                mApp.login(username, password);
            } else {
                Handler handler = new Handler();
                handler.postDelayed(enterRegister, 1000);
            }
        }

        @Override
        public void onLogin() {
            try {
                MUC.getInstance();
                startChatActivity();
            } catch (Exception e) {
                e.printStackTrace();
                mApp.rub();
            }
        }

        @Override
        public void onRub(String room, boolean create) {
            mApp.enterRoom(room, create);
        }

        @Override
        public void onEnterRoom() {
            startChatActivity();
        }

        @Override
        public void onError(SparkError error, final SparkAction action) {
            Resources resources = getResources();
            if (mErrorHandler == 1) {
                Toast.makeText(getApplicationContext(), resources.getString(R.string.dialog_message), Toast.LENGTH_SHORT).show();
            } else if (mErrorHandler == 2) {
                new AlertDialog.Builder(LauncherActivity.this).setTitle(resources.getString(R.string.dialog_title))
                        .setMessage(resources.getString(R.string.dialog_message) + ": " + error)
                        .setCancelable(false)
                        .setNegativeButton(resources.getString(R.string.dialog_retry), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mApp.retry(action);
                            }
                        }).setPositiveButton(resources.getString(R.string.dialog_exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // exit application
                        finish();
                    }
                }).show();
            }
        }
    }
}