package com.veiljoy;

import org.jivesoftware.smack.AbstractXMPPConnection;

import com.veiljoy.rub.RubInfo;

public class MySpark {
	static boolean sendMessageTest = false;

	public static void main(String[] argus) {
		// test dom4j
		Dom4jTest dom4jTest = new Dom4jTest();
		if (dom4jTest.test()) {
			return;
		}

		// connect
		ConnectionTest conn = new ConnectionTest();
		if (!conn.connect()) {
			return;
		}
		AbstractXMPPConnection connection = conn.getConnection();

		// login
		final String username = "myspark-male";
		final String usergender = "male";
//		final String username = "myspark-female";
//		final String usergender = "female";
		final String password = "lcboat";
		AccountTest account = new AccountTest(connection);
		connection = account.login(username, password);
		if (connection == null) {
			return;
		}

		// vcard
		VCardTest vcard = new VCardTest(connection);
		String gender = vcard.getField("GENDER");
		System.out.println("1: GENDER: " + gender);
		if (!usergender.equals(gender)) {
			vcard.setField("GENDER", usergender);
			vcard.flush();

			// renew a vcard to check gender
			vcard = new VCardTest(connection);
			gender = vcard.getField("GENDER");
			System.out.println("2: GENDER: " + gender);
		}

		// chat
		if (sendMessageTest) {
			ChatTest chatTest = new ChatTest(connection);
			chatTest.newChat("kofspark@veil");
			chatTest.sendMessage("Hello!");
		}

		// rub
		RubTest rub = new RubTest(connection);
		RubInfo info = rub.getRubInfo();
		if (info != null) {
			System.out.println("rub room: " + info.getRoom() + ", create: "
					+ info.isCreate());
		}

		// muc test
		MUCTest muc = new MUCTest(connection);
		muc.init(info);

		// main loop
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
