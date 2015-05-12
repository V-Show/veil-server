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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.veiljoy.spark.android.R;
import com.veiljoy.spark.android.core.SparkApplication;
import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.Configs;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.utils.DeviceUtil;
import com.veiljoy.spark.android.utils.SharePreferenceUtil;
import com.veiljoy.spark.core.SimpleSparkListener;
import com.veiljoy.spark.core.SparkAction;
import com.veiljoy.spark.core.SparkError;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;


/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {

    // UI references.
    private AutoCompleteTextView mNicknameView;
    private EditText mPasswordView;
    private RadioButton maleRadio;

    private String username;
    private String nickname;
    private String password;
    // flag
    private boolean mWaitLogin;

    SparkApplication mApp;
    RegisterSparkListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mApp = (SparkApplication)getApplication();
        mListener = new RegisterSparkListener();

        // Set up the register form.
        mNicknameView = (AutoCompleteTextView) findViewById(R.id.nickname);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        maleRadio = (RadioButton) findViewById(R.id.radioMale);

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mNicknameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        nickname = mNicknameView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid nickname address.
        if (TextUtils.isEmpty(nickname)) {
            mNicknameView.setError(getString(R.string.error_field_required));
            focusView = mNicknameView;
            cancel = true;
        } else if (!isNicknameValid(nickname)) {
            mNicknameView.setError(getString(R.string.error_invalid_email));
            focusView = mNicknameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            username = DeviceUtil.getUniqueId(this);
            if (TextUtils.isEmpty(password)) {
                password = Configs.defaultPassword;
            }
            mApp.register(username, nickname, password);
        }
    }

    private boolean isNicknameValid(String nickname) {
        boolean valid = true;
        if (nickname.length() > 18) {
            valid = false;
        }
        return valid;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void startChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    class RegisterSparkListener extends SimpleSparkListener {
        @Override
        public void onRegister() {
            SharePreferenceUtil.setUsername(username);
            SharePreferenceUtil.setPassword(password);

            mApp.login(username, password);
        }

        @Override
        public void onLogin() {
            String gender = Carriers.Constants.GenderMale;
            if (!maleRadio.isChecked()) {
                gender = Carriers.Constants.GenderFemale;
            }

            VCard vCard = new VCard();
            vCard.setField(Carriers.Constants.GenderField, gender);
            // FIXME use nickname field to save the real name of user
            vCard.setNickName(mNicknameView.getText().toString());

            mApp.uploadVCard(vCard);
        }

        @Override
        public void onUploadVCard() {
            mApp.rub();
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
            new AlertDialog.Builder(RegisterActivity.this).setTitle(resources.getString(R.string.dialog_title))
                    .setMessage(resources.getString(R.string.dialog_message) + ": " + error)
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



