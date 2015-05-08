package com.veiljoy.spark.android.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.veiljoy.spark.android.net.xmpp.Connection;
import com.veiljoy.spark.android.net.xmpp.MUC;
import com.veiljoy.spark.android.net.xmpp.rub.IQRubProvider;
import com.veiljoy.spark.android.net.xmpp.rub.RubInfo;
import com.veiljoy.spark.android.net.xmpp.rub.RubReq;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2015/5/5.
 */
public class NetThread extends HandlerThread {
    static final String TAG = "NetThread";
    private final boolean debug = true;

    private static NetThread mInstance;
    Handler mHandler;

    public static NetThread getInstance() {
        if (mInstance == null) {
            mInstance = new NetThread();
            mInstance.start();
        }
        return mInstance;
    }

    private NetThread() {
        super("net thread");
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new NetHandler(this.getLooper());
        }
        return mHandler;
    }

    class NetHandler extends Handler {
        public NetHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("<<: " + msg.obj.toString());

            boolean ignore = false;
            switch (msg.what) {
                case Carriers.CARRIER_CONNECT: {
                    Carriers.ConnectCarrier carrier = (Carriers.ConnectCarrier) msg.obj;
                    Connection.getInstance().connect();

                    carrier.error = Carriers.convertError(Connection.getInstance().getLastException());

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_CONNECT;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                case Carriers.CARRIER_REGISTER: {
                    Carriers.RegisterCarrier carrier = (Carriers.RegisterCarrier) msg.obj;
                    AccountManager aMgr = AccountManager.getInstance(Connection.getInstance().getConnection());
                    aMgr.sensitiveOperationOverInsecureConnection(true);
                    try {
                        Map<String, String> attributes = new HashMap<>();
                        attributes.put("name", carrier.nickname);
                        aMgr.createAccount(carrier.username, carrier.password, attributes);
                    } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                        e.printStackTrace();
                        carrier.error = Carriers.convertError(e);
                    }

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_REGISTER;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                case Carriers.CARRIER_LOGIN: {
                    Carriers.LoginCarrier carrier = (Carriers.LoginCarrier) msg.obj;
                    try {
                        Connection.getInstance().getConnection().login(carrier.username, carrier.password);
                    } catch (XMPPException | SmackException | IOException e) {
                        e.printStackTrace();
                        carrier.error = Carriers.convertError(e);
                    }

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_LOGIN;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                case Carriers.CARRIER_UPLOAD_VCARD: {
                    Carriers.UploadVCardCarrier carrier = (Carriers.UploadVCardCarrier) msg.obj;

                    VCardManager vCardManager = VCardManager.getInstanceFor(Connection.getInstance().getConnection());
                    try {
                        vCardManager.saveVCard(carrier.vCard);
                    } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                        e.printStackTrace();
                        carrier.error = Carriers.convertError(e);
                    }

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_UPLOAD_VCARD;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                case Carriers.CARRIER_RUB: {
                    Carriers.RubCarrier carrier = (Carriers.RubCarrier) msg.obj;

                    ProviderManager.addIQProvider("query", "com.veil.rub",
                            new IQRubProvider());
                    try {
                        // send a rub request
                        final RubReq req = new RubReq();
                        RubInfo info = Connection.getInstance().getConnection().createPacketCollectorAndSend(
                                new StanzaTypeFilter(RubInfo.class), req)
                                .nextResultOrThrow();
                        carrier.room = info.getRoom();
                        carrier.create = info.isCreate();
                    } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                        e.printStackTrace();
                        carrier.error = Carriers.convertError(e);
                    }

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_RUB;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                case Carriers.CARRIER_ENTER_ROOM: {
                    Carriers.EnterRoomCarrier carrier = (Carriers.EnterRoomCarrier) msg.obj;
                    try {
                        MUC.getInstanceFor(carrier.room, carrier.create);
                    } catch (Exception e) {
                        e.printStackTrace();
                        carrier.error = Carriers.convertError(e);
                    }

                    Message rsp = carrier.handler.obtainMessage();
                    rsp.what = Carriers.CARRIER_ENTER_ROOM;
                    rsp.obj = carrier;
                    carrier.handler.sendMessage(rsp);
                }
                break;
                default:
                    ignore = true;
                    break;
            }
            if (!ignore) {
                log(">>: " + msg.obj.toString());
            }
        }
    }

    public void log(String msg) {
        if (debug) {
            Log.d(TAG, msg);
        }
    }
}
