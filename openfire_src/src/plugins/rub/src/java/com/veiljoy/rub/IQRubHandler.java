package com.veiljoy.rub;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

public class IQRubHandler extends IQHandler {

	IQHandlerInfo info;

	public IQRubHandler() {
		super("rub");
		info = new IQHandlerInfo("query", "com.veil.rub");
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		System.out.println("handle IQ: " + packet.toString());
		JID user = packet.getFrom();
		RubInfo reply = new RubInfo("query", "com.veil.rub", user);
		reply.setFrom(XMPPServer.getInstance().getServerInfo().getXMPPDomain());
		reply.setTo(user);
		reply.setType(IQ.Type.result);
		reply.setID(packet.getID());
		System.out.println("reply: " + reply.toString());
		return reply;
	}

	@Override
	public IQHandlerInfo getInfo() {
		return info;
	}

}
