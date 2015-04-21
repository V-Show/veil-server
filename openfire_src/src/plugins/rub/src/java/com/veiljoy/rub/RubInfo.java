package com.veiljoy.rub;

import org.jivesoftware.openfire.vcard.VCardManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.dom4j.Element;

public class RubInfo extends IQ {
	public RubInfo(String name, String namespace, JID user) {
		super();
		setChildElement(name, namespace);

		// get user's vard
		Element vard = VCardManager.getInstance().getVCard(user.toString());
		if (vard != null) {
			System.out.print("vard of user" + user + ": " + vard.toString());
		} else {
			System.out.print("vard of user is not existed.");
		}
	}
}
