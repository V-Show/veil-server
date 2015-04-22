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
		
		String room = "hall";
		boolean create = false;
		try {
			int eventType = parser.getEventType();
			String tag = null;
			boolean done = false;
			while (!done) {
				switch (eventType) {
				case XmlPullParser.START_TAG: {
					tag = parser.getName();
				}
					break;
				case XmlPullParser.END_TAG: {
					String tagName = parser.getName();
					if (tagName.equals(name)) {
						done = true;
					}
				}
					break;
				case XmlPullParser.TEXT:
					if (tag != null)
						if (tag.equals("room")) {
							room = parser.getText();
						} else if (tag.equals("create")) {
							create = Boolean.parseBoolean(parser.getText());
						}
					break;
				default:
					break;
				}

				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		info.setCreate(create);
		info.setRoom(room);
		System.out.println("IQRubProvider rub info: " + info.toString());
		return info;
	}

}
