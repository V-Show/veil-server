package com.veiljoy;

import java.util.Iterator;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import com.veiljoy.rub.RubInfo;

public class MUCTest {
	AbstractXMPPConnection connection;
	MultiUserChatManager mucManager;
	MultiUserChat muc;
	String roomName;
	String userName;

	public MUCTest(AbstractXMPPConnection connection) {
		this.connection = connection;

		mucManager = MultiUserChatManager.getInstanceFor(connection);
		userName = JID.getNode(connection.getUser());
	}

	public void init(RubInfo info) {
		// debug
		 info.setRoom("chatroom");
		 info.setCreate(false);
		
		roomName = info.getRoom();
		muc = mucManager.getMultiUserChat(roomName + "@conference.veil");
		muc.addParticipantStatusListener(new MyParticipantStatusListener());
		
		// create room if need
		try {
			if (info.isCreate()) {
				createRoom();
			} else {
				muc.join(userName);
			}
			
			// 获取房间成员
			Iterator<String> it = muc.getOccupants().iterator();
			while (it.hasNext()) {
				String name = it.next();
				name = name.substring(name.indexOf("/") + 1);
				System.out.printf("成员名字: " + name);
			}
		} catch (XMPPErrorException | SmackException e) {
			e.printStackTrace();
		}
	}

	void createRoom() throws NoResponseException, XMPPErrorException,
			SmackException {
		muc.create(userName);
		
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
		submitForm.setAnswer("muc#roomconfig_persistentroom", false);
		// 房间仅对成员开放
		submitForm.setAnswer("muc#roomconfig_membersonly", false);
		// 允许占有者邀请其他人
		submitForm.setAnswer("muc#roomconfig_allowinvites", true);
		// 能够发现占有者真实 JID 的角色
		// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
		// 登录房间对话
		submitForm.setAnswer("muc#roomconfig_enablelogging", false);
		// 仅允许注册的昵称登录
		submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
		// 允许使用者修改昵称
		submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
		// 允许用户注册房间
		submitForm.setAnswer("x-muc#roomconfig_registration", false);
		// 发送已完成的表单（有默认值）到服务器来配置聊天室
		muc.sendConfigurationForm(submitForm);
	}
	
	class MyParticipantStatusListener implements ParticipantStatusListener {

		@Override
		public void joined(String participant) {
			System.out.println("join: " + participant);
		}

		@Override
		public void left(String participant) {
			System.out.println("left: " + participant);
		}

		@Override
		public void kicked(String participant, String actor, String reason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void voiceGranted(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void voiceRevoked(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void banned(String participant, String actor, String reason) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void membershipGranted(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void membershipRevoked(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void moderatorGranted(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void moderatorRevoked(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ownershipGranted(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ownershipRevoked(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void adminGranted(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void adminRevoked(String participant) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void nicknameChanged(String participant, String newNickname) {
			// TODO Auto-generated method stub
			
		}
	}
}
