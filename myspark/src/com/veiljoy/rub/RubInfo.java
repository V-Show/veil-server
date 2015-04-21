package com.veiljoy.rub;

import org.jivesoftware.smack.packet.IQ;

public class RubInfo extends IQ {
	
	String room;
	boolean create;

	protected RubInfo(String childElementName, String childElementNamespace) {
		super(childElementName, childElementNamespace);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
			IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.element("room", room);
		xml.element("create", Boolean.toString(create));
		return xml;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}
}
