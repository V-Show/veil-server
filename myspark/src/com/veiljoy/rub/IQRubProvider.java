package com.veiljoy.rub;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class IQRubProvider extends IQProvider<RubInfo> {

	@Override
	public RubInfo parse(XmlPullParser parser, int initialDepth)
			throws XmlPullParserException, IOException, SmackException {
		String name = parser.getName();
		String namespace = parser.getNamespace();
		RubInfo info = new RubInfo(name, namespace);
		info.setCreate(true);
		info.setRoom("my room");
		System.out.println("IQRubProvider rub info: " + info.toString());
		return info;
	}

}
