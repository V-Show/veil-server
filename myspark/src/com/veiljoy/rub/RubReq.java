package com.veiljoy.rub;

import org.jivesoftware.smack.packet.IQ;

public class RubReq extends IQ {

	public RubReq(String childElementName, String childElementNamespace) {
		super(childElementName, childElementNamespace);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
			IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		return xml;
	}
}
