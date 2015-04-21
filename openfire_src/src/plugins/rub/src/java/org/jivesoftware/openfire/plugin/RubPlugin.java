package org.jivesoftware.openfire.plugin;

import java.io.File;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Packet;

import com.veiljoy.rub.IQRubHandler;

public class RubPlugin implements PacketInterceptor, Plugin {
	private XMPPServer server;

	private boolean trace = false;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		server = XMPPServer.getInstance();

		InterceptorManager.getInstance().addInterceptor(this);

		server.getIQRouter().addHandler(new IQRubHandler());

		System.out.println("initialize rub plugin");
	}

	@Override
	public void destroyPlugin() {
		System.out.println("destroy rub plugin");
	}

	@Override
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		if (session != null) {
			debug(packet, incoming, processed, session);
		}
	}

	/**
	 * <b>function:</b> 调试信息
	 * 
	 * @author suyu
	 * @createDate 2015-04-21
	 * @param packet
	 *            数据包
	 * @param incoming
	 *            如果为ture就表明是发送者
	 * @param processed
	 *            执行
	 * @param session
	 *            当前用户session
	 */
	private void debug(Packet packet, boolean incoming, boolean processed,
			Session session) {
		String info = "[ packetID: " + packet.getID() + ", to: "
				+ packet.getTo() + ", from: " + packet.getFrom()
				+ ", incoming: " + incoming + ", processed: " + processed
				+ " ]";

		long timed = System.currentTimeMillis();
		debug("################### start ###################" + timed);
		debug("id:" + session.getStreamID() + ", address: "
				+ session.getAddress());
		debug("info: " + info);
		debug("xml: " + packet.toXML());
		debug("################### end #####################" + timed);
	}

	private void debug(Object message) {
		if (trace) {
			System.out.println(message);
		}
	}
}