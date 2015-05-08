package com.veiljoy.spark.android.net.xmpp;

import com.veiljoy.spark.android.net.Configs;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Created by Administrator on 2015/5/7.
 */
public class Connection {
    private static Connection mInstance;

    private AbstractXMPPConnection mXMPPConnection;
    private Exception mLastException;

    public static Connection getInstance() {
        if (mInstance == null) {
            mInstance = new Connection();
        }
        return mInstance;
    }

    public Connection() {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
                .builder()
                .setServiceName(Configs.SERVICE_NAME).setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setResource(Configs.RESOURCE_NAME).setDebuggerEnabled(true)
                .setHost(Configs.HOST).build();
        mXMPPConnection = new XMPPTCPConnection(config);
    }

    public void connect() {
        try {
            mXMPPConnection.connect();
            mLastException = null;
        } catch (SmackException | IOException | XMPPException e) {
            e.printStackTrace();
            mLastException = e;
        }
    }

    public Exception getLastException() {
        return mLastException;
    }

    public AbstractXMPPConnection getConnection() {
        return mXMPPConnection;
    }
}
