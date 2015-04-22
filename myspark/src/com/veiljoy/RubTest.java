package com.veiljoy;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.provider.ProviderManager;

import com.veiljoy.rub.IQRubProvider;
import com.veiljoy.rub.RubInfo;
import com.veiljoy.rub.RubReq;

/**
 * https://community.igniterealtime.org/message/236652#236652
 * 
 * @author suyu
 * 
 */
public class RubTest {
	AbstractXMPPConnection connection;

	public RubTest(AbstractXMPPConnection connection) {
		this.connection = connection;

		ProviderManager.addIQProvider("query", "com.veil.rub",
				new IQRubProvider());
	}

	public RubInfo getRubInfo() {
		RubInfo info = null;
		try {
			// send a rub request
			final RubReq req = new RubReq();
			info = connection.createPacketCollectorAndSend(
					new StanzaTypeFilter(RubInfo.class), req)
					.nextResultOrThrow();
		} catch (NotConnectedException | NoResponseException
				| XMPPErrorException e) {
			e.printStackTrace();
		}
		return info;
	}
}
