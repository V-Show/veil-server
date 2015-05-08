package com.veil.balcony;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.muc.CannotBeInvitedException;
import org.jivesoftware.openfire.muc.ConflictException;
import org.jivesoftware.openfire.muc.ForbiddenException;
import org.jivesoftware.openfire.muc.HistoryRequest;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRole.Affiliation;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MUCRoomHistory;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.muc.NotAcceptableException;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.muc.RegistrationRequiredException;
import org.jivesoftware.openfire.muc.RoomLockedException;
import org.jivesoftware.openfire.muc.ServiceUnavailableException;
import org.jivesoftware.openfire.muc.spi.IQAdminHandler;
import org.jivesoftware.openfire.muc.spi.IQOwnerHandler;
import org.jivesoftware.openfire.muc.spi.LocalMUCRole;
import org.jivesoftware.openfire.muc.spi.LocalMUCUser;
import org.jivesoftware.openfire.muc.spi.MUCPersistenceManager;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Type;

public class Balcony implements MUCRoom {

	private static final Logger Log = LoggerFactory.getLogger(Balcony.class);

	/**
	 * The service hosting the room.
	 */
	private MultiUserChatService mucService;

	/**
	 * The name of the room.
	 */
	private String name;

	/**
	 * The ID of the room. If the room is temporary and does not log its
	 * conversation then the value will always be -1. Otherwise a value will be
	 * obtained from the database.
	 */
	private long roomID = -1;

	/**
	 * The date when the room was created.
	 */
	private Date creationDate;

	/**
	 * The last date when the room's configuration was modified.
	 */
	private Date modificationDate;

	/**
	 * The date when the last occupant left the room. A null value means that
	 * there are occupants in the room at the moment.
	 */
	private Date emptyDate;

	/**
	 * The role of the room itself.
	 */
	private MUCRole role = new RoomRole(this);

    /**
     * The occupants of the room accessible by the occupants full JID.
     */
    private Map<JID, MUCRole> occupantsByFullJID = new ConcurrentHashMap<JID, MUCRole>();

    /**
     * The occupants of the room accessible by the occupants nickname.
     */
    private Map<String, List<MUCRole>> occupantsByNickname = new ConcurrentHashMap<String, List<MUCRole>>();

    /**
     * The occupants of the room accessible by the occupants bare JID.
     */
    private Map<JID, List<MUCRole>> occupantsByBareJID = new ConcurrentHashMap<JID, List<MUCRole>>();
    
	public Balcony(MultiUserChatService chatservice, String roomname,
			PacketRouter packetRouter) {
		this.mucService = chatservice;
		this.name = name;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeExternal(ObjectOutput arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.util.resultsetmanager.Result#getUID()
	 */
	@Override
	public String getUID() {
		// name is unique for each one particular MUC service.
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public JID getJID() {
		return new JID(getName(), getMUCService().getServiceDomain(), null);
	}

	@Override
	public long getID() {
		if (isPersistent() || isLogEnabled()) {
			if (roomID == -1) {
				roomID = SequenceManager.nextID(JiveConstants.MUC_ROOM);
			}
		}
		return roomID;
	}

	@Override
	public void setID(long roomID) {
		this.roomID = roomID;
	}

	@Override
	public MultiUserChatService getMUCService() {
		return mucService;
	}

	@Override
	public void setMUCService(MultiUserChatService service) {
		this.mucService = service;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@Override
	public void setEmptyDate(Date emptyDate) {
		// Do nothing if old value is same as new value
		if (this.emptyDate == emptyDate) {
			return;
		}
		this.emptyDate = emptyDate;
		MUCPersistenceManager.updateRoomEmptyDate(this);
	}

	@Override
	public Date getEmptyDate() {
		return this.emptyDate;
	}

	@Override
	public MUCRole getRole() {
		return role;
	}

	@Override
    /**
     * @deprecated Prefer {@link #getOccupantsByNickname(String)} (user can be connected more than once)
     */
	public MUCRole getOccupant(String nickname) throws UserNotFoundException {
        if (nickname == null) {
            throw new UserNotFoundException();
       }
       List<MUCRole> roles = getOccupantsByNickname(nickname);
       if (roles != null && roles.size() > 0) {
       	return roles.get(0);
       }
       throw new UserNotFoundException();
	}

	@Override
	public List<MUCRole> getOccupantsByNickname(String nickname)
			throws UserNotFoundException {
        if (nickname == null) {
            throw new UserNotFoundException();
       }
       List<MUCRole> roles = occupantsByNickname.get(nickname.toLowerCase());
       if (roles != null && roles.size() > 0) {
       	return roles;
       }
       throw new UserNotFoundException();
	}

	@Override
	public List<MUCRole> getOccupantsByBareJID(JID jid)
			throws UserNotFoundException {
        List<MUCRole> roles = occupantsByBareJID.get(jid);
        if (roles != null && !roles.isEmpty()) {
            return Collections.unmodifiableList(roles);
        }
        throw new UserNotFoundException();
	}

	@Override
	public MUCRole getOccupantByFullJID(JID jid) {
        MUCRole role = occupantsByFullJID.get(jid);
        if (role != null) {
            return role;
        }
        return null;
	}

	@Override
	public Collection<MUCRole> getOccupants() {
		return Collections.unmodifiableCollection(occupantsByFullJID.values());
	}

	@Override
	public int getOccupantsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasOccupant(String nickname) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getReservedNickname(JID jid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Affiliation getAffiliation(JID bareJID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalMUCRole joinRoom(String nickname, String password,
			HistoryRequest historyRequest, LocalMUCUser user, Presence presence)
			throws UnauthorizedException, UserAlreadyExistsException,
			RoomLockedException, ForbiddenException,
			RegistrationRequiredException, ConflictException,
			ServiceUnavailableException, NotAcceptableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leaveRoom(MUCRole leaveRole) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroyRoom(JID alternateJID, String reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public Presence createPresence(Type type) throws UnauthorizedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void serverBroadcast(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getChatLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addFirstOwner(JID bareJID) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Presence> addOwner(JID jid, MUCRole senderRole)
			throws ForbiddenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addOwners(List<JID> newOwners, MUCRole senderRole)
			throws ForbiddenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addAdmins(List<JID> newAdmins, MUCRole senderRole)
			throws ForbiddenException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addAdmin(JID jid, MUCRole senderRole)
			throws ForbiddenException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addMember(JID jid, String nickname, MUCRole senderRole)
			throws ForbiddenException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addOutcast(JID jid, String reason, MUCRole senderRole)
			throws NotAllowedException, ForbiddenException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Presence> addNone(JID jid, MUCRole senderRole)
			throws ForbiddenException, ConflictException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Presence addModerator(JID fullJID, MUCRole senderRole)
			throws ForbiddenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Presence addParticipant(JID fullJID, String reason,
			MUCRole senderRole) throws NotAllowedException, ForbiddenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Presence addVisitor(JID jid, MUCRole senderRole)
			throws NotAllowedException, ForbiddenException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isManuallyLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void presenceUpdated(MUCRole occupantRole, Presence newPresence) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nicknameChanged(MUCRole occupantRole, Presence newPresence,
			String oldNick, String newNick) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeSubject(Message packet, MUCRole role)
			throws ForbiddenException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubject(String subject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPublicMessage(Message message, MUCRole senderRole)
			throws ForbiddenException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendPrivatePacket(Packet packet, MUCRole senderRole)
			throws NotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public Presence kickOccupant(JID fullJID, JID actorJID, String reason)
			throws NotAllowedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQOwnerHandler getIQOwnerHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQAdminHandler getIQAdminHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MUCRoomHistory getRoomHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<JID> getOwners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<JID> getAdmins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<JID> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<JID> getOutcasts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<MUCRole> getModerators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<MUCRole> getParticipants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canAnyoneDiscoverJID() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanAnyoneDiscoverJID(boolean canAnyoneDiscoverJID) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canOccupantsChangeSubject() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanOccupantsChangeSubject(boolean canOccupantsChangeSubject) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canOccupantsInvite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanOccupantsInvite(boolean canOccupantsInvite) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNaturalLanguageName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNaturalLanguageName(String naturalLanguageName) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMembersOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Presence> setMembersOnly(boolean membersOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLogEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLogEnabled(boolean logEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLoginRestrictedToNickname() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLoginRestrictedToNickname(boolean restricted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canChangeNickname() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setChangeNickname(boolean canChange) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRegistrationEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRegistrationEnabled(boolean registrationEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxUsers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxUsers(int maxUsers) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isModerated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setModerated(boolean moderated) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPasswordProtected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPassword(String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPersistent(boolean persistent) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean wasSavedToDB() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSavedToDB(boolean saved) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToDB() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPublicRoom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPublicRoom(boolean publicRoom) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getRolesToBroadcastPresence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRolesToBroadcastPresence(
			List<String> rolesToBroadcastPresence) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canBroadcastPresence(String roleToBroadcast) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void lock(MUCRole senderRole) throws ForbiddenException {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlock(MUCRole senderRole) throws ForbiddenException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendInvitation(JID to, String reason, MUCRole role,
			List<Element> extensions) throws ForbiddenException,
			CannotBeInvitedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendInvitationRejection(JID to, String reason, JID from) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Packet packet) {
		// TODO Auto-generated method stub

	}

	/**
	 * An empty role that represents the room itself in the chatroom. Chatrooms
	 * need to be able to speak (server messages) and so must have their own
	 * role in the chatroom.
	 */
	private class RoomRole implements MUCRole {

		private MUCRoom room;

		private RoomRole(MUCRoom room) {
			this.room = room;
		}

		public Presence getPresence() {
			return null;
		}

		public void setPresence(Presence presence) {
		}

		public void setRole(MUCRole.Role newRole) {
		}

		public MUCRole.Role getRole() {
			return MUCRole.Role.moderator;
		}

		public void setAffiliation(MUCRole.Affiliation newAffiliation) {
		}

		public MUCRole.Affiliation getAffiliation() {
			return MUCRole.Affiliation.owner;
		}

		public void changeNickname(String nickname) {
		}

		public String getNickname() {
			return null;
		}

		public boolean isVoiceOnly() {
			return false;
		}

		public boolean isLocal() {
			return true;
		}

		public NodeID getNodeID() {
			return XMPPServer.getInstance().getNodeID();
		}

		public MUCRoom getChatRoom() {
			return room;
		}

		private JID crJID = null;

		public JID getRoleAddress() {
			if (crJID == null) {
				crJID = new JID(room.getName(), mucService.getServiceDomain(),
						null, true);
			}
			return crJID;
		}

		public JID getUserAddress() {
			return null;
		}

		public void send(Packet packet) {
			room.send(packet);
		}

		public void destroy() {
		}
	}
}
