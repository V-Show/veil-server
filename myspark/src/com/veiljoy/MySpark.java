package com.veiljoy;

import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import com.veiljoy.rub.IQRubProvider;
import com.veiljoy.rub.RubInfo;
import com.veiljoy.rub.RubReq;

public class MySpark {
	static boolean sendMessageTest = false;

	static void init(AbstractXMPPConnection connection)
			throws XMPPErrorException, SmackException {
		ProviderManager.addIQProvider("query", "com.veil.rub",
				new IQRubProvider());

		if (sendMessageTest) {
			ChatTest chatTest = new ChatTest(connection);
			Chat newChat = chatTest.newChat("suyu2@veil");
			newChat.sendMessage("Hello!");
		}

		// send a room iq
		final IQ iq = new RubReq();
		iq.setType(IQ.Type.get);
		connection.sendStanza(iq);

		StanzaFilter filter = new StanzaFilter() {
			public boolean accept(Stanza stanza) {
				boolean ret = false;
				IQ iq = (IQ) stanza;
				if (iq != null) {
					if (iq.getType() == IQ.Type.result
							&& iq.getChildElementNamespace().equals(
									"com.veil.rub")) {
						ret = true;
					}
				}
				return ret;
			}
		};

		PacketCollector collector = connection.createPacketCollector(filter);
		RubInfo rubInfo = collector.nextResult(SmackConfiguration
				.getDefaultPacketReplyTimeout());
		collector.cancel();
		String rubXml = rubInfo.toString();
		System.out.print("rub info: " + rubXml);

		// create a multi-user-chat
		if (false) {
			MultiUserChatManager muChatManager = MultiUserChatManager
					.getInstanceFor(connection);
			// get the room list of service
			List<HostedRoom> list = muChatManager
					.getHostedRooms("conference.veil");
			System.out.print(list.toString());
			// if the room is existed
			RoomInfo roomInfo = muChatManager
					.getRoomInfo("suyus5@conference.veil");
			System.out.print(roomInfo.toString());
			MultiUserChat muc = muChatManager
					.getMultiUserChat("suyus@conference.veil");
			try {
				muc.join("suyu");
			} catch (XMPPErrorException e) {
				e.getXMPPError();
			}

			if (false) {
				muc.create("suyu");
				// 获得聊天室的配置表单
				Form form = muc.getConfigurationForm();
				// 根据原始表单创建一个要提交的新表单。
				Form submitForm = form.createAnswerForm();
				// 向要提交的表单添加默认答复
				for (Iterator<FormField> fields = form.getFields().iterator(); fields
						.hasNext();) {
					FormField field = (FormField) fields.next();
					if (!FormField.Type.hidden.equals(field.getType())
							&& field.getVariable() != null) {
						// 设置默认值作为答复
						submitForm.setDefaultAnswer(field.getVariable());
					}
				}
				// 设置聊天室的新拥有者
				// List owners = new ArrayList();
				// owners.add("liaonaibo2\\40slook.cc");
				// owners.add("liaonaibo1\\40slook.cc");
				// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
				// 设置聊天室是持久聊天室，即将要被保存下来
				submitForm.setAnswer("muc#roomconfig_persistentroom", true);
				// 房间仅对成员开放
				submitForm.setAnswer("muc#roomconfig_membersonly", false);
				// 允许占有者邀请其他人
				submitForm.setAnswer("muc#roomconfig_allowinvites", true);
				// 能够发现占有者真实 JID 的角色
				// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
				// 登录房间对话
				submitForm.setAnswer("muc#roomconfig_enablelogging", true);
				// 仅允许注册的昵称登录
				submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
				// 允许使用者修改昵称
				submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
				// 允许用户注册房间
				submitForm.setAnswer("x-muc#roomconfig_registration", false);
				// 发送已完成的表单（有默认值）到服务器来配置聊天室
				muc.sendConfigurationForm(submitForm);
			}

			// 获取房间成员
			Iterator<String> it = muc.getOccupants().iterator();
			while (it.hasNext()) {
				String name = it.next();
				name = name.substring(name.indexOf("/") + 1);
				System.out.printf("成员名字: " + name);
			}
		}

		// Disconnect from the server
		// connection.disconnect();

		// main loop
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

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
		final String username = "lucy";
		final String password = "lcboat";
		AccountTest account = new AccountTest(connection);
		connection = account.login(username, password);
		if (connection == null) {
			return;
		}

		// vcard
		final String usergender = "female";
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
			chatTest.newChat("suyu2@veil");
			chatTest.sendMessage("Hello!");
		}

		// rub
		RubTest rub = new RubTest(connection);
		RubInfo info = rub.getRubInfo();
		if (info != null) {
			System.out.println("rub room: " + info.getRoom() + ", create: "
					+ info.isCreate());
		}

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
