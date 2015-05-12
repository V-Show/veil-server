package com.veiljoy.spark.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.veiljoy.spark.android.R;
import com.veiljoy.spark.android.core.SparkApplication;
import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.net.xmpp.JID;
import com.veiljoy.spark.android.net.xmpp.MUC;
import com.veiljoy.spark.core.SimpleSparkListener;
import com.veiljoy.spark.core.SparkAction;
import com.veiljoy.spark.core.SparkError;
import com.veiljoy.spark.core.UserInfo;

/**
 * Created by Administrator on 2015/5/7.
 */
public class ChatActivity extends Activity {
    TextView textView;
    Button sendBtn;

    SparkApplication mApp;
    ChatSparkListener mListener;

    TextView[] nicknames = new TextView[4];
    Button[] kickButtons = new Button[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mApp = (SparkApplication) getApplication();
        mListener = new ChatSparkListener();

        textView = (TextView) findViewById(R.id.text_view);
        sendBtn = (Button) findViewById(R.id.send_button);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = SparkApplication.generateUniqueID();
                mApp.sendMessage(id, "hello");
                prepend(">> " + id + ": hello - sending\n");
            }
        });

        nicknames[0] = (TextView) (findViewById(R.id.room_owner).findViewById(R.id.nickname));
        nicknames[1] = (TextView) (findViewById(R.id.roommate_1).findViewById(R.id.nickname));
        nicknames[2] = (TextView) (findViewById(R.id.roommate_2).findViewById(R.id.nickname));
        nicknames[3] = (TextView) (findViewById(R.id.roommate_3).findViewById(R.id.nickname));

        kickButtons[0] = (Button) (findViewById(R.id.roommate_1).findViewById(R.id.kick));
        kickButtons[1] = (Button) (findViewById(R.id.roommate_2).findViewById(R.id.kick));
        kickButtons[2] = (Button) (findViewById(R.id.roommate_3).findViewById(R.id.kick));

        for (int i = 0; i < 3; i++) {
            kickButtons[i].setTag(i);
            kickButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int) v.getTag();
                    mApp.kick(nicknames[index + 1].getText().toString());
                }
            });
        }

        for (int i = 0; i < 4; i++) {
            nicknames[i].setText("");
        }
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

    private void prepend(String text) {
        textView.setText(text + textView.getText());
    }

    class ChatSparkListener extends SimpleSparkListener {
        @Override
        public void onReceiveMessage(String from, String body, String subject) {
            prepend(JID.getNickname(from) + ": " + body + "\n");
        }

        @Override
        public void onSendMessage(int id) {
            prepend(id + " - sent.\n");
        }

        @Override
        public void onError(SparkError error, SparkAction action) {
            if (action.getAction() == SparkAction.Action.send_message) {
                prepend(" - send failed.\n");
            }
        }

        @Override
        public void onJoin(int index, UserInfo[] users) {
            nicknames[index].setText(users[index].getNickname());
        }

        @Override
        public void onLeft(int index, UserInfo[] users) {
            nicknames[index].setText("");
        }
    }
}
