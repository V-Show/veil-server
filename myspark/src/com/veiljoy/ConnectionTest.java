package com.veiljoy;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class ConnectionTest {
	private AbstractXMPPConnection connection;
	static final String SERIVCE_NAME = "veil";
	static final String HOST = "veiljoy.com";

	ConnectionTest() {
	}

	public boolean connect() {
		boolean ret = true;
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
				.builder()
				// .setUsernameAndPassword("suyu", "lcboat")
				.setServiceName(SERIVCE_NAME).setSecurityMode(SecurityMode.disabled)
				.setResource("myspark").setDebuggerEnabled(true)
				.setHost(HOST).build();
		connection = new XMPPTCPConnection(config);
		// connection.setFromMode(FromMode.USER);
		try {
			connection.connect();
		} catch (SmackException | IOException | XMPPException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public AbstractXMPPConnection getConnection() {
		return connection;
	}
}
