package com.veiljoy.spark.android;

import android.app.Activity;
import android.content.Intent;
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

import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.Configs;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.utils.DeviceUtil;
import com.veiljoy.spark.android.utils.SharePreferenceUtil;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;


/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends Activity {

    // UI references.
    private AutoCompleteTextView mNicknameView;
    private EditText mPasswordView;
    private RadioButton maleRadio;
    private boolean mWaitLogin;

    // handler
    private Handler netHandler;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        mHandler = new LoginHandler();
        netHandler = NetThread.getInstance().getHandler();
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
        String nickname = mNicknameView.getText().toString();
        String password = mPasswordView.getText().toString();

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
            register(nickname, password);
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

    private void login(String username, String password) {
        Message msg = netHandler.obtainMessage();
        Carriers.login(msg, new Carriers.LoginCarrier(username, password, mHandler));
        netHandler.sendMessage(msg);
    }

    private void register(String nickname, String password) {
        if (!mWaitLogin) {
            String username = DeviceUtil.getUniqueId(this);
            if (TextUtils.isEmpty(password)) {
                password = Configs.defaultPassword;
            }

            Message msg = netHandler.obtainMessage();
            Carriers.register(msg, new Carriers.RegisterCarrier(username, nickname, password, mHandler));
            netHandler.sendMessage(msg);

            mWaitLogin = true;
        }
    }

    private void uploadVCard() {
        String gender = Carriers.Constants.GenderMale;
        if (!maleRadio.isChecked()) {
            gender = Carriers.Constants.GenderFemale;
        }

        VCard vCard = new VCard();
        vCard.setField(Carriers.Constants.GenderField, gender);
        // FIXME use nickname field to save the real name of user
        vCard.setNickName(mNicknameView.getText().toString());

        Message msg = netHandler.obtainMessage();
        Carriers.uploadVCard(msg, new Carriers.UploadVCardCarrier(vCard, mHandler));
        netHandler.sendMessage(msg);
    }

    private void rub() {
        Message msg = netHandler.obtainMessage();
        Carriers.rub(msg, new Carriers.RubCarrier(mHandler));
        netHandler.sendMessage(msg);
    }

    private void enterRoom(String room, boolean create) {
        Message msg = netHandler.obtainMessage();
        Carriers.enterRoom(msg, new Carriers.EnterRoomCarrier(room, create, mHandler));
        netHandler.sendMessage(msg);
    }

    private void startChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    class LoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Carriers.CARRIER_LOGIN: {
                    Carriers.LoginCarrier carrier = (Carriers.LoginCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        uploadVCard();
                    } else {
                        onError(carrier.error);
                        mWaitLogin = false;
                    }
                }
                break;
                case Carriers.CARRIER_REGISTER: {
                    Carriers.RegisterCarrier carrier = (Carriers.RegisterCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error ||
                            carrier.error == Carriers.Error.conflict) {
                        // save username and password to share preference
                        SharePreferenceUtil.setUsername(carrier.username);
                        SharePreferenceUtil.setPassword(carrier.password);
                        // and then login
                        login(carrier.username, carrier.password);
                    } else {
                        onError(carrier.error);
                        mWaitLogin = false;
                    }
                }
                break;
                case Carriers.CARRIER_UPLOAD_VCARD: {
                    Carriers.UploadVCardCarrier carrier = (Carriers.UploadVCardCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        rub();
                    } else {
                        onError(carrier.error);
                        mWaitLogin = false;
                    }
                }
                break;
                case Carriers.CARRIER_RUB: {
                    Carriers.RubCarrier carrier = (Carriers.RubCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        enterRoom(carrier.room, carrier.create);
                    } else {
                        onError(carrier.error);
                        mWaitLogin = false;
                    }
                }
                break;
                case Carriers.CARRIER_ENTER_ROOM: {
                    Carriers.EnterRoomCarrier carrier = (Carriers.EnterRoomCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        // next activity
                        startChatActivity();
                    } else {
                        onError(carrier.error);
                    }
                    mWaitLogin = false;
                }
                break;
            }
        }
    }

    private void onError(Carriers.Error error) {
        Toast.makeText(getApplicationContext(), error + "", Toast.LENGTH_SHORT).show();
    }
}



