package org.jivesoftware.openfire.plugin;

import java.io.File;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;

import com.veil.balcony.service.BalconyService;

public class BalconyPlugin implements Plugin {
	private XMPPServer server;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		server = XMPPServer.getInstance();
		
		server.getMultiUserChatManager().registerMultiUserChatService(new BalconyService());

		System.out.println("initialize balcony plugin");
	}

	@Override
	public void destroyPlugin() {
		System.out.println("destroy balcony plugin");
	}

}