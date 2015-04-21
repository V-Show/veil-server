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

	ConnectionTest() {
	}

	public boolean connect() {
		boolean ret = true;
		XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration
				.builder()
				// .setUsernameAndPassword("suyu", "lcboat")
				.setServiceName("veil").setSecurityMode(SecurityMode.disabled)
				.setResource("myspark").setDebuggerEnabled(true)
				.setHost("localhost").build();
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
