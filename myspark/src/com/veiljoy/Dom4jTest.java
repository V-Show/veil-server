package com.veiljoy;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Dom4jTest {
	public Dom4jTest() {
	}
	
	public boolean test() {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("query", "com.veil.rub");
		root.addAttribute("type", "result");
		
		Element room = DocumentHelper.createElement("room");
		room.setText("my room");
		root.add(room);
		
		Element create = DocumentHelper.createElement("create");
		create.setText("true");
		root.add(create);
		
		System.out.println(root.asXML());
		
		return false;
	}
}
