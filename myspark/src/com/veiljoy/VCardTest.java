package com.veiljoy;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

public class VCardTest {
	AbstractXMPPConnection connection;
	VCard vcard;
	boolean changed;

	public VCardTest(AbstractXMPPConnection connection) {
		this.connection = connection;
	}

	public String getField(String field) {
		if (vcard == null) {
			load();
		}

		if (vcard == null) {
			return null;
		} else {
			return vcard.getField(field);
		}
	}

	public boolean setField(String field, String value) {
		if (vcard == null) {
			load();
		}

		if (vcard == null) {
			return false;
		} else {
			String oldValue = vcard.getField(field);
			if (!value.equals(oldValue)) {
				vcard.setField(field, value);
				changed = true;
			}
			return true;
		}
	}

	public boolean flush() {
		boolean ret = true;
		
		if (changed) {
			ret = this.save();
			if (ret) {
				changed = false;
			}
		}

		return ret;
	}

	private boolean load() {
		boolean ret = true;
		try {
			this.vcard = VCardManager.getInstanceFor(this.connection)
					.loadVCard();
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	private boolean save() {
		boolean ret = true;
		try {
			VCardManager.getInstanceFor(this.connection).saveVCard(this.vcard);
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
}
