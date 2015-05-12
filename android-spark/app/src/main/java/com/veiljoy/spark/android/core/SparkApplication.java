package com.veiljoy.spark.android.core;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.NetThread;
import com.veiljoy.spark.android.net.xmpp.MUC;
import com.veiljoy.spark.android.utils.SharePreferenceUtil;
import com.veiljoy.spark.core.SparkAction;
import com.veiljoy.spark.core.SparkError;
import com.veiljoy.spark.core.SparkListener;
import com.veiljoy.spark.core.UserInfo;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/5/11.
 */
public class SparkApplication extends Application {
    public static final String TAG = "CoreApplication";
    private static boolean debug = true;
    private static int unique_id = 0;

    CoreHandler mHandler;
    Handler netHandler;

    class User {
        Handler handler;
        SparkListener listener;

        public User(SparkListener listener) {
            this.handler = new Handler();
            this.listener = listener;
        }
    }

    List<User> mUsers = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new CoreHandler();
        netHandler = NetThread.getInstance().getHandler();
    }

    public void registerSparkListener(SparkListener listener) {
        unregisterSparkListener(listener);

        User user = new User(listener);
        mUsers.add(user);
    }

    public void unregisterSparkListener(SparkListener listener) {
        int i;
        for (i = 0; i < mUsers.size(); i++)
            if (listener == mUsers.get(i).listener) {
                break;
            }
        if (i < mUsers.size()) {
            mUsers.remove(i);
        }
    }

    public void connect() {
        Message msg = netHandler.obtainMessage();
        Carriers.connect(msg, new Carriers.ConnectCarrier(mHandler));
        netHandler.sendMessage(msg);
    }

    public void register(String username, String nickname, String password) {
        Message msg = netHandler.obtainMessage();
        Carriers.register(msg, new Carriers.RegisterCarrier(username, nickname, password, mHandler));
        netHandler.sendMessage(msg);
    }

    public void login(String username, String password) {
        Message msg = netHandler.obtainMessage();
        Carriers.login(msg, new Carriers.LoginCarrier(username, password, mHandler));
        netHandler.sendMessage(msg);
    }

    public void uploadVCard(VCard vCard) {
        Message msg = netHandler.obtainMessage();
        Carriers.uploadVCard(msg, new Carriers.UploadVCardCarrier(vCard, mHandler));
        netHandler.sendMessage(msg);
    }

    public void rub() {
        Message msg = netHandler.obtainMessage();
        Carriers.rub(msg, new Carriers.RubCarrier(mHandler));
        netHandler.sendMessage(msg);
    }

    public void enterRoom(String room, boolean create) {
        Message msg = netHandler.obtainMessage();
        Carriers.enterRoom(msg, new Carriers.EnterRoomCarrier(room, create, mHandler));
        netHandler.sendMessage(msg);
    }

    public void sendMessage(int id, String body) {
        Message msg = netHandler.obtainMessage();
        Carriers.sendMessage(msg, new Carriers.SendMessageCarrier(id, body, mHandler));
        netHandler.sendMessage(msg);
    }

    public void kick(String nickname) {
        Message msg = netHandler.obtainMessage();
        Carriers.kick(msg, new Carriers.KickCarrier(nickname, mHandler));
        netHandler.sendMessage(msg);
    }

    public void retry(SparkAction action) {
        if (action.getAction() == SparkAction.Action.connect) {
            connect();
        } else if (action.getAction() == SparkAction.Action.register) {
            Object[] args = action.getArgs();
            register((String) args[0], (String) args[1], (String) args[2]);
        } else if (action.getAction() == SparkAction.Action.login) {
            Object[] args = action.getArgs();
            login((String) args[0], (String) args[1]);
        } else if (action.getAction() == SparkAction.Action.upload_vcard) {
            Object[] args = action.getArgs();
            uploadVCard((VCard) args[0]);
        } else if (action.getAction() == SparkAction.Action.rub) {
            rub();
        } else if (action.getAction() == SparkAction.Action.enter_room) {
            Object[] args = action.getArgs();
            enterRoom((String) args[0], (boolean) args[1]);
        } else if (action.getAction() == SparkAction.Action.send_message) {
            Object[] args = action.getArgs();
            sendMessage((int) args[0], (String) args[1]);
        } else if (action.getAction() == SparkAction.Action.kick) {
            Object[] args = action.getArgs();
            kick((String) args[0]);
        }
    }

    class CoreHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Carriers.CARRIER_CONNECT: {
                    Carriers.ConnectCarrier carrier = (Carriers.ConnectCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error ||
                            carrier.error == Carriers.Error.already_connected) {
                        onConnect();
                    } else {
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.connect));
                    }
                }
                break;
                case Carriers.CARRIER_REGISTER: {
                    Carriers.RegisterCarrier carrier = (Carriers.RegisterCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onRegister();
                    } else {
                        Object[] args = new Object[3];
                        args[0] = carrier.username;
                        args[1] = carrier.nickname;
                        args[2] = carrier.password;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.register, args));
                    }
                }
                break;
                case Carriers.CARRIER_LOGIN: {
                    Carriers.LoginCarrier carrier = (Carriers.LoginCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error ||
                            carrier.error == Carriers.Error.already_logged_in) {
                        onLogin();
                    } else {
                        Object[] args = new Object[2];
                        args[0] = carrier.username;
                        args[1] = carrier.password;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.login, args));
                    }
                }
                break;
                case Carriers.CARRIER_UPLOAD_VCARD: {
                    Carriers.UploadVCardCarrier carrier = (Carriers.UploadVCardCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onUploadVCard();
                    } else {
                        Object[] args = new Object[1];
                        args[0] = carrier.vCard;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.upload_vcard, args));
                    }
                }
                break;
                case Carriers.CARRIER_RUB: {
                    Carriers.RubCarrier carrier = (Carriers.RubCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onRub(carrier.room, carrier.create);
                    } else {
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.rub));
                    }
                }
                break;
                case Carriers.CARRIER_ENTER_ROOM: {
                    Carriers.EnterRoomCarrier carrier = (Carriers.EnterRoomCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onEnterRoom();
                    } else {
                        Object[] args = new Object[2];
                        args[0] = carrier.room;
                        args[1] = carrier.create;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.enter_room, args));
                    }
                }
                break;
                case Carriers.CARRIER_RECEIVE_MESSAGE: {
                    Carriers.ReceiveMessageCarrier carrier = (Carriers.ReceiveMessageCarrier) msg.obj;
                    boolean self = false;
                    try {
                        MUC muc = MUC.getInstance();
                        String nickname = muc.getNickname();
                        String room = muc.getRoom();
                        if (carrier.from.equals(room + "/" + nickname)) {
                            // send by self
                            onSendMessage(Integer.parseInt(carrier.subject));
                            self = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!self) {
                        onReceiveMessage(carrier.from, carrier.body, carrier.subject);
                    }
                }
                break;
                case Carriers.CARRIER_SEND_MESSAGE: {
                    Carriers.SendMessageCarrier carrier = (Carriers.SendMessageCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onSendMessage(carrier.id);
                    } else {
                        Object[] args = new Object[2];
                        args[0] = carrier.id;
                        args[1] = carrier.body;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.send_message, args));
                    }
                }
                break;
                case Carriers.CARRIER_UPDATE_OCCUPANTS: {
                    Carriers.UpdateOccupantsCarrier carrier = (Carriers.UpdateOccupantsCarrier) msg.obj;
                    if (carrier.event == Carriers.UpdateOccupantsCarrier.Event.join) {
                        onJoin(carrier.index, carrier.users);
                    } else if (carrier.event == Carriers.UpdateOccupantsCarrier.Event.left) {
                        onLeft(carrier.index, carrier.users);
                    }
                }
                break;
                case Carriers.CARRIER_KICK: {
                    Carriers.KickCarrier carrier = (Carriers.KickCarrier) msg.obj;
                    if (carrier.error == Carriers.Error.no_error) {
                        onKick(carrier.nickname);
                    } else {
                        Object[] args = new Object[1];
                        args[0] = carrier.nickname;
                        onError(new SparkError(carrier.error), new SparkAction(SparkAction.Action.kick, args));
                    }
                }
                break;
            }
        }
    }

    private void onConnect() {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onConnect();
                }
            });
        }
    }

    private void onRegister() {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onRegister();
                }
            });
        }
    }

    private void onLogin() {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onLogin();
                }
            });
        }
    }

    private void onUploadVCard() {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onUploadVCard();
                }
            });
        }
    }

    private void onRub(final String room, final boolean create) {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onRub(room, create);
                }
            });
        }
    }

    private void onEnterRoom() {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onEnterRoom();
                }
            });
        }
    }

    private void onReceiveMessage(final String from, final String body, final String subject) {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onReceiveMessage(from, body, subject);
                }
            });
        }
    }

    private void onJoin(final int index, final UserInfo[] users) {
        log("onJoin index=" + index + ", users=" + users);
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onJoin(index, users);
                }
            });
        }
    }

    private void onLeft(final int index, final UserInfo[] users) {
        log("onLeft index=" + index + ", users=" + users);
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onLeft(index, users);
                }
            });
        }
    }

    private void onSendMessage(final int id) {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onSendMessage(id);
                }
            });
        }
    }

    private void onKick(final String nickname) {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onKick(nickname);
                }
            });
        }
    }

    private void onError(final SparkError error, final SparkAction action) {
        for (final User user : mUsers) {
            user.handler.post(new Runnable() {
                @Override
                public void run() {
                    user.listener.onError(error, action);
                }
            });
        }
    }

    synchronized public static int generateUniqueID() {
        return unique_id++;
    }

    public void log(String msg) {
        if (debug) {
            Log.d(TAG, msg);
        }
    }
}
