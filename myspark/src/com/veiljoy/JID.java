package com.veiljoy;

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
}
