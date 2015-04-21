package com.veiljoy;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;

import com.veiljoy.rub.IQRubProvider;
import com.veiljoy.rub.RubInfo;
import com.veiljoy.rub.RubReq;

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
			final IQ req = new RubReq("query", "com.veil.rub");
			req.setType(IQ.Type.get);
			info = connection.createPacketCollectorAndSend(
					new StanzaIdFilter(req.getStanzaId()), req)
					.nextResultOrThrow();
		} catch (NotConnectedException | NoResponseException
				| XMPPErrorException e) {
			e.printStackTrace();
		}
		return info;
	}
}
