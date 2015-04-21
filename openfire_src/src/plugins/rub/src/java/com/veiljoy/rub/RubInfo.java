package com.veiljoy.rub;

import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

public class RubInfo extends IQ {
	public RubInfo(String name, String namespace, JID user) {
		super();

		// default gender is female
		String gender = "female";

		// get user's vard
		Element vcard = VCardManager.getInstance().getVCard(user.getNode());
		if (vcard != null) {
			gender = vcard.element("GENDER").getText();
		}

		String room;
		boolean create;

		if (gender.equals("female")) {
			room = user.getNode();
			create = true;
		} else {
			// find a room
			room = findRoom();
			create = false;
		}

		Element roomElement = docFactory.createDocument().addElement("room");
		roomElement.setText(room);
		Element createElement = docFactory.createDocument()
				.addElement("create");
		createElement.setText(Boolean.toString(create));
		Element element = docFactory.createDocument().addElement(name,
				namespace);
		element.add(roomElement);
		element.add(createElement);
		setChildElement(element);
	}

	public String findRoom() {
		String name = "hall";
		List<MultiUserChatService> services = XMPPServer.getInstance()
				.getMultiUserChatManager().getMultiUserChatServices();
		for (MultiUserChatService service : services) {
			List<MUCRoom> rooms = service.getChatRooms();
			for (MUCRoom room : rooms) {
				if (!room.isLocked() && room.getOccupants().size() < 3) {
					name = room.getJID().getNode();
					return name;
				}
			}
		}
		return name;
	}
}
