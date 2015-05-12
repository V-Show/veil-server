package com.veiljoy.spark.android.net.xmpp;

/**
 * Created by Administrator on 2015/5/9.
 */
public class JID {
    public static String getNode(String jid) {
        String node = jid;
        int atIndex = jid.indexOf("@");

        // Node
        if (atIndex > 0) {
            node = jid.substring(0, atIndex);
        }

        return node;
    }

    public static String getNickname(String roomJid) {
        String nickname = roomJid;
        int index = roomJid.indexOf("/");

        if (index > 0) {
            nickname = roomJid.substring(index + 1);
        }
        return nickname;
    }

    public static String getBaredID(String jid) {
        String baredId = jid;
        int index = jid.indexOf("/");

        if (index > 0) {
            baredId = baredId.substring(0, index);
        }
        return baredId;
    }
}
