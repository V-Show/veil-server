package com.veil.balcony.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.clearspace.ClearspaceManager;
import org.jivesoftware.openfire.muc.HistoryStrategy;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.muc.spi.MUCPersistenceManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import com.veil.balcony.spi.ConversationLogEntry;
import com.veil.balcony.spi.PersistenceManager;

public class BalconyService implements MultiUserChatService {
	public static final String SERVICE_NAME = "Balcony";
	private static final String DESCRIPTION = "Balcony Services";

	private static final Logger log = LoggerFactory
			.getLogger(ClearspaceManager.class);

	/**
	 * Returns the permission policy for creating rooms. A true value means that
	 * not anyone can create a room, only the JIDs listed in
	 * <code>allowedToCreate</code> are allowed to create rooms.
	 */
	private boolean roomCreationRestricted = false;
	/**
	 * Bare jids of users that are allowed to create MUC rooms. An empty list
	 * means that anyone can create a room.
	 */
	private List<JID> allowedToCreate = new CopyOnWriteArrayList<JID>();

	/**
	 * Bare jids of users that are system administrators of the MUC service. A
	 * sysadmin has the same permissions as a room owner.
	 */
	private List<JID> sysadmins = new CopyOnWriteArrayList<JID>();

	/**
	 * Task that flushes room conversation logs to the database.
	 */
	private LogConversationTask logConversationTask;
	/**
	 * The time to elapse between logging the room conversations.
	 */
	private int log_timeout = 300000; // 5min
	/**
	 * The number of messages to log on each run of the logging process.
	 */
	private int log_batch_size = 50;
	/**
	 * Queue that holds the messages to log for the rooms that need to log their
	 * conversations.
	 */
	private Queue<ConversationLogEntry> logQueue = new LinkedBlockingQueue<ConversationLogEntry>(
			100000);

	/**
	 * Flag that indicates if balcony service is enabled.
	 */
	private boolean serviceEnabled = true;

	private HistoryStrategy historyStrategy = new HistoryStrategy(null);;

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return SERVICE_NAME;
	}

	@Override
	public void initialize(JID jid, ComponentManager componentManager)
			throws ComponentException {
		historyStrategy.setContext(getServiceName(), "history");
	}

	@Override
	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown() {
		logAllConversation();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getServiceDomain() {
		return this.getServiceName() + "."
				+ XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public Collection<JID> getSysadmins() {
		return Collections.unmodifiableCollection(sysadmins);
	}

	@Override
	public void addSysadmin(JID userJID) {
		final JID bareJID = new JID(userJID.toBareJID());

		sysadmins.add(bareJID);
	}

	@Override
	public void removeSysadmin(JID userJID) {
		final JID bareJID = new JID(userJID.toBareJID());

		sysadmins.remove(bareJID);
	}

	@Override
	public boolean isRoomCreationRestricted() {
		return roomCreationRestricted;
	}

	@Override
	public void setRoomCreationRestricted(boolean roomCreationRestricted) {
		this.roomCreationRestricted = roomCreationRestricted;
	}

	@Override
	public Collection<JID> getUsersAllowedToCreate() {
		return Collections.unmodifiableCollection(allowedToCreate);
	}

	@Override
	public void addUserAllowedToCreate(JID userJID) {
		List<JID> asList = new ArrayList<JID>();
		asList.add(userJID);
		addUsersAllowedToCreate(asList);
	}

	@Override
	public void addUsersAllowedToCreate(Collection<JID> userJIDs) {
		for (JID userJID : userJIDs) {
			allowedToCreate.add(userJID);
		}
	}

	@Override
	public void removeUserAllowedToCreate(JID userJID) {
		List<JID> asList = new ArrayList<JID>();
		asList.add(userJID);
		addUsersAllowedToCreate(asList);
	}

	@Override
	public void removeUsersAllowedToCreate(Collection<JID> userJIDs) {
		for (JID userJID : userJIDs) {
			allowedToCreate.remove(userJID);
		}
	}

	@Override
	public void setKickIdleUsersTimeout(int timeout) {
		// Ignore this function since we do not kick idle users.
	}

	@Override
	public int getKickIdleUsersTimeout() {
		// Ignore this function since we do not kick idle users.
		return 0;
	}

	@Override
	public void setUserIdleTime(int idle) {
		// Ignore this function since we do not kick idle users.
	}

	@Override
	public int getUserIdleTime() {
		// Ignore this function since we do not kick idle users.
		return 0;
	}

	@Override
	public void setLogConversationsTimeout(int timeout) {
		if (this.log_timeout == timeout) {
			return;
		}
		// Cancel the existing task because the timeout has changed
		if (logConversationTask != null) {
			logConversationTask.cancel();
		}
		this.log_timeout = timeout;
		// Create a new task and schedule it with the new timeout
		logConversationTask = new LogConversationTask();
		TaskEngine.getInstance().schedule(logConversationTask, log_timeout,
				log_timeout);
	}

	@Override
	public int getLogConversationsTimeout() {
		return log_timeout;
	}

	@Override
	public void setLogConversationBatchSize(int size) {
		if (this.log_batch_size == size) {
			return;
		}
		this.log_batch_size = size;
		// Set the new property value
		MUCPersistenceManager.setProperty(getServiceName(),
				"tasks.log.batchsize", Integer.toString(size));
	}

	@Override
	public int getLogConversationBatchSize() {
		return log_batch_size;
	}

	@Override
	public HistoryStrategy getHistoryStrategy() {
		return historyStrategy;
	}

	@Override
	public MUCRoom getChatRoom(String roomName, JID userjid)
			throws NotAllowedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MUCRoom getChatRoom(String roomName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshChatRoom(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<MUCRoom> getChatRooms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChatRoom(String roomName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void chatRoomRemoved(LocalMUCRoom room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void chatRoomAdded(LocalMUCRoom room) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeChatRoom(String roomName) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<MUCRole> getMUCRoles(JID user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTotalChatTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberChatRooms() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberConnectedUsers(boolean onlyLocal) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberRoomOccupants() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getIncomingMessageCount(boolean resetAfter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getOutgoingMessageCount(boolean resetAfter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void logConversation(MUCRoom room, Message message, JID sender) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageBroadcastedTo(int numOccupants) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableService(boolean enabled, boolean persistent) {
		if (isServiceEnabled() == enabled) {
			// Do nothing if the service status has not changed
			return;
		}
		if (!enabled) {
			// Stop the service/module
			shutdown();
		}
		if (persistent) {
			MUCPersistenceManager.setProperty(getServiceName(), "enabled",
					Boolean.toString(enabled));
		}
		serviceEnabled = enabled;
		if (enabled) {
			// Start the service/module
			start();
		}
	}

	@Override
	public boolean isServiceEnabled() {
		return serviceEnabled;
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	/**
	 * Logs the conversation of the rooms that have this feature enabled.
	 */
	private class LogConversationTask extends TimerTask {
		@Override
		public void run() {
			try {
				logConversation();
			} catch (Throwable e) {
				log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			}
		}
	}

	private void logConversation() {
		ConversationLogEntry entry;
		boolean success;
		for (int index = 0; index <= log_batch_size && !logQueue.isEmpty(); index++) {
			entry = logQueue.poll();
			if (entry != null) {
				success = PersistenceManager.saveConversationLogEntry(entry);
				if (!success) {
					logQueue.add(entry);
				}
			}
		}
	}

	/**
	 * Logs all the remaining conversation log entries to the database. Use this
	 * method to force saving all the conversation log entries before the
	 * service becomes unavailable.
	 */
	private void logAllConversation() {
		ConversationLogEntry entry;
		while (!logQueue.isEmpty()) {
			entry = logQueue.poll();
			if (entry != null) {
				PersistenceManager.saveConversationLogEntry(entry);
			}
		}
	}
}
