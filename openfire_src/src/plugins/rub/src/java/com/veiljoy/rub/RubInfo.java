package com.veiljoy.rub;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

public class RubInfo extends IQ {
	public final static String USER_CARD_FILED_GENDER = "user_gender";
	public final static int USER_GENDER_MALE = 0;
	public final static int USER_GENDER_FEMALE = 1;
	
	public final static String FILED_GENDER = "GENDER";

	public RubInfo(String name, String namespace, JID user) {
		super();

		// default gender is male
		String gender = "male";

		// get user's vCard
		Element vcard = VCardManager.getInstance().getVCard(user.getNode());
		if (vcard != null) {
			String vcardString = vcard.toString();
			System.out.println("vCard: " + vcardString);
			
			Element genderElement = vcard.element(USER_CARD_FILED_GENDER);
			if (genderElement != null) {
				gender = genderElement.getText();
			}
			Attribute genderAttribute = vcard.attribute(USER_CARD_FILED_GENDER);
			if (genderAttribute != null) {
				gender = genderAttribute.getValue();
			}
			if (gender != null) {
				if (gender.equals("1")) {
					gender = "female";
				}
			}
			
			// second try
			genderElement = vcard.element(FILED_GENDER);
			if (genderElement != null) {
				gender = genderElement.getText();
			}
			genderAttribute = vcard.attribute(FILED_GENDER);
			if (genderAttribute != null) {
				gender = genderAttribute.getValue();
			}
		} else {
			System.out.println("not found vCard for use(" + user + ")");
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
				if (!room.isLocked() && room.getOccupants().size() < 4 && !room.getName().equals("hall")) {
					name = room.getJID().getNode();
					return name;
				}
			}
		}
		return name;
	}
}
