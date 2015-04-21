package com.veiljoy;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

public class ChatTest {
	AbstractXMPPConnection connection;
	ChatManager chatManager;
	Chat chat;

	public ChatTest(AbstractXMPPConnection connection) {
		this.connection = connection;
		this.chatManager = ChatManager.getInstanceFor(this.connection);

		// the received message is not created locally mostly.
		this.chatManager.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally) {
					chat.addMessageListener(new TestChatMessageListener());
				}
			}
		});
	}

	public Chat newChat(String target) {
		Chat newChat = chatManager.createChat(target,
				new TestChatMessageListener());
		this.chat = newChat;
		return this.chat;
	}

	public boolean sendMessage(String message) {
		boolean ret = true;
		try {
			this.chat.sendMessage(message);
		} catch (NotConnectedException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	class TestChatMessageListener implements ChatMessageListener {
		@Override
		public void processMessage(Chat chat, Message message) {
			System.out.println("Received message from " + message.getFrom()
					+ ": " + message.getBody());
		}
	};
}
