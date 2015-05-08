package com.veiljoy.spark.android;

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

import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.Configs;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.utils.SharePreferenceUtil;

/**
 * Created by Administrator on 2015/5/1.
 */
public class LauncherActivity extends Activity {

    LauncherHandler mHandler;
    Runnable enterRegister;
    Runnable enterChat;
    Handler netHandler;
    int mErrorHandler = 2; // 0: none; 1: toast, 2: alert dialog
    int mState = 0; // 0: idle; 1: connect; 2: login
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        // init share preference util at launcher activity
        SharePreferenceUtil.init(getApplicationContext());
        username = SharePreferenceUtil.getUsername();
        password = SharePreferenceUtil.getPassword();

        mHandler = new LauncherHandler();
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

        netHandler = NetThread.getInstance().getHandler();

        connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // eat key back
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class LauncherHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Carriers.CARRIER_CONNECT: {
                    Carriers.ConnectCarrier carrier = (Carriers.ConnectCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error ||
                            carrier.error == Carriers.Error.already_connected) {
                        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                            login();
                        } else {
                            mHandler.postDelayed(enterRegister, 1000);
                        }
                    } else {
                        onError(carrier.error);
                    }
                }
                break;
                case Carriers.CARRIER_LOGIN: {
                    Carriers.LoginCarrier carrier = (Carriers.LoginCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error ||
                            carrier.error == Carriers.Error.already_logged_in) {
                        mHandler.postDelayed(enterChat, 1000);
                    } else {
                        onError(carrier.error);
                    }
                }
                break;
            }
        }
    }

    void onError(Carriers.Error error) {
        Resources resources = getResources();
        if (mErrorHandler == 1) {
            Toast.makeText(getApplicationContext(), resources.getString(R.string.dialog_message), Toast.LENGTH_SHORT).show();
        } else if (mErrorHandler == 2) {
            new AlertDialog.Builder(this).setTitle(resources.getString(R.string.dialog_title))
                    .setMessage(resources.getString(R.string.dialog_message) + ": " + error)
                    .setNegativeButton(resources.getString(R.string.dialog_retry), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mState == 1) {
                                // reconnect
                                connect();
                            } else if (mState == 2) {
                                login();
                            }
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

    private void connect() {
        Message msg = netHandler.obtainMessage();
        Carriers.connect(msg, new Carriers.ConnectCarrier(mHandler));
        netHandler.sendMessage(msg);

        mState = 1;
    }

    private void login() {
        Message msg = netHandler.obtainMessage();
        Carriers.login(msg, new Carriers.LoginCarrier(username, password, mHandler));
        netHandler.sendMessage(msg);

        mState = 2;
    }
}