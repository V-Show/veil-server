package com.veiljoy;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smackx.iqregister.AccountManager;

public class AccountTest {
	AbstractXMPPConnection connection;
	AccountManager amgr;
	String username;
	String password;

	public AccountTest(AbstractXMPPConnection connection) {
		this.connection = connection;
		flushAccountManager();
	}
	
	private void flushAccountManager() {
		this.amgr = AccountManager.getInstance(connection);

		// allow sensitive operations like account creation or password changes
		// over an insecure (e.g. unencrypted) connections.
		this.amgr.sensitiveOperationOverInsecureConnection(true);
	}

	public boolean createAccount(String username, String password) {
		boolean ret = true;
		System.out.println("create account: " + username + ":" + password);
		try {
			amgr.createAccount(username, password);
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public AbstractXMPPConnection login(String username, String password) {
		AbstractXMPPConnection connection = this.connection;
		this.username = username;
		this.password = password;
		try {
			connection.login(username, password);
		} catch (XMPPException | SmackException | IOException e) {
			e.printStackTrace();
			connection = null;
			if (e instanceof SASLErrorException) {
				// maybe username and password is not correct or user is not
				// existed.
				SASLErrorException se = (SASLErrorException) e;
				if (se.getSASLFailure().getSASLErrorString()
						.equals("not-authorized")) {
					// need create account
					if (createAccount(username, password)) {
						// we need new another connection after create account
						ConnectionTest conn = new ConnectionTest();
						if (conn.connect()) {
							this.connection = conn.getConnection();
							flushAccountManager();
							connection = this.login(username, password);
						}
					}
				}
			}
		}
		return connection;
	}
}
