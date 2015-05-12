package com.veiljoy.spark.android.net.xmpp;

import android.os.Handler;
import android.util.Log;

import com.veiljoy.spark.android.net.Carriers;
import com.veiljoy.spark.android.net.Configs;
import com.veiljoy.spark.core.UserInfo;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.packet.MUCItem;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2015/5/8.
 */
public class MUC {
    private static MUC mInstance;

    MultiUserChat muc;
    Handler callbackHandler;
    UserInfo[] users = new UserInfo[4];

    public static MUC getInstanceFor(String room, boolean create, Handler handler) throws Exception {
        if (mInstance != null) {
            throw new Exception("there is a muc already");
        }
        mInstance = new MUC(room, create, handler);
        return mInstance;
    }

    public static MUC getInstance() throws Exception {
        if (mInstance == null) {
            throw new Exception("no muc instance");
        }
        return mInstance;
    }

    private MUC(String room, boolean create, Handler handler) throws SmackException, XMPPException.XMPPErrorException {
        AbstractXMPPConnection connection = Connection.getInstance().getConnection();
        muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(room + Configs.ROOM_JID);

        muc.addMessageListener(new ChatMessageListener());
        muc.addParticipantListener(new ChatPresenceListener());
        muc.addParticipantStatusListener(new ChatParticipantStatusListener());

        callbackHandler = handler;

        // get the real name of user
        // FIXME use the nickname field of vCard to get the real name of user
        VCard vCard = VCardManager.getInstanceFor(connection).loadVCard();
        String name = vCard.getNickName();
        if (create) {
            createRoom(name);
        } else {
            muc.join(name);
        }
    }

    private void createRoom(String room) throws XMPPException.XMPPErrorException, SmackException {
        muc.create(room);

        // 获得聊天室的配置表单
        Form form = muc.getConfigurationForm();
        // 根据原始表单创建一个要提交的新表单。
        Form submitForm = form.createAnswerForm();
        // 向要提交的表单添加默认答复
        for (Iterator<FormField> fields = form.getFields().iterator(); fields
                .hasNext(); ) {
            FormField field = (FormField) fields.next();
            if (!FormField.Type.hidden.equals(field.getType())
                    && field.getVariable() != null) {
                // 设置默认值作为答复
                submitForm.setDefaultAnswer(field.getVariable());
            }
        }
        // 设置聊天室的新拥有者
        // List owners = new ArrayList();
        // owners.add("liaonaibo2\\40slook.cc");
        // owners.add("liaonaibo1\\40slook.cc");
        // submitForm.setAnswer("muc#roomconfig_roomowners", owners);
        // 设置聊天室是持久聊天室，即将要被保存下来
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);
        // 房间仅对成员开放
        submitForm.setAnswer("muc#roomconfig_membersonly", false);
        // 允许占有者邀请其他人
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);
        // 能够发现占有者真实 JID 的角色
        submitForm.setAnswer("muc#roomconfig_whois", Arrays.asList("anyone"));
//        submitForm.setAnswer("muc#roomconfig_whois", Arrays.asList("moderators"));
        // 房间最大人数
        submitForm.setAnswer("muc#roomconfig_maxusers", Arrays.asList("4"));
        // 登录房间对话
        submitForm.setAnswer("muc#roomconfig_enablelogging", false);
        // 仅允许注册的昵称登录
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
        // 允许使用者修改昵称
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
        // 允许用户注册房间
        submitForm.setAnswer("x-muc#roomconfig_registration", false);
        // 发送已完成的表单（有默认值）到服务器来配置聊天室
        muc.sendConfigurationForm(submitForm);
    }

    public void sendMessage(int id, String body) throws SmackException.NotConnectedException {
        Message msg = new Message();
        msg.setSubject(id + "");
        msg.setBody(body);
        muc.sendMessage(msg);
    }

    public String getNickname() throws Exception {
        return muc.getNickname();
    }

    public String getRoom() throws Exception {
        return muc.getRoom();
    }

    public void kick(String nickname, String reason) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        muc.kickParticipant(nickname, reason);
    }

    class ChatMessageListener implements MessageListener {
        @Override
        public void processMessage(Message message) {
            String from = message.getFrom();
            String body = message.getBody();
            String subject = message.getSubject();

            notifyReceiveMessage(from, body, subject);
        }
    }

    class ChatPresenceListener implements PresenceListener {
        @Override
        public void processPresence(Presence presence) {
            ExtensionElement pe = presence.getExtension("http://jabber.org/protocol/muc#user");
            if (pe != null) {
                MUCUser user = (MUCUser) pe;
                MUCItem item = user.getItem();
                String userJid = item.getJid();
                String username = JID.getNode(userJid);
                String nickname = JID.getNickname(presence.getFrom());
                MUCRole role = item.getRole();

                // get avatar
                VCard vCard = null;
                try {
                    vCard = VCardManager.getInstanceFor(Connection.getInstance().getConnection()).loadVCard(
                            JID.getBaredID(userJid));
                } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(username);
                userInfo.setNickname(nickname);
                if (vCard != null) {
                    userInfo.setAvatar(vCard.getAvatar());
                }

                Carriers.UpdateOccupantsCarrier.Event event = null;
                int index = -1;
                if (presence.getType() == Presence.Type.available) {
                    event = Carriers.UpdateOccupantsCarrier.Event.join;
                    if (role == MUCRole.moderator) {
                        users[0] = userInfo;
                        index = 0;
                    } else {
                        // find a empty seat
                        for (int i = 1; i < 4; i++) {
                            if (users[i] == null) {
                                users[i] = userInfo;
                                index = i;
                                break;
                            }
                        }
                    }
                } else if (presence.getType() == Presence.Type.unavailable) {
                    event = Carriers.UpdateOccupantsCarrier.Event.left;
                    // find the occupant
                    for (int i = 0; i < 4; i++) {
                        if (users[i] != null
                                && users[i].getUsername().equals(username)) {
                            users[i] = null;
                            index = i;
                            break;
                        }
                    }
                }

                if (event != null && index != -1) {
                    notifyUpdateUserInfo(event, index);
                }
            }
//            pe = presence.getExtension("vcard-temp:x:update");
//            pe = presence.getExtension("jabber:x:avatar");
        }
    }

    class ChatParticipantStatusListener extends SimpleParticipantStatusListener {
        @Override
        public void joined(String s) {
        }

        @Override
        public void left(String s) {
        }

        @Override
        public void kicked(String s, String s2, String s3) {
        }

        @Override
        public void banned(String s, String s2, String s3) {
        }
    }

    private void notifyReceiveMessage(String from, String body, String subject) {
        int delayMillis = 0;
        if (mInstance == null) {
            delayMillis = 200;
        }
        android.os.Message msg = callbackHandler.obtainMessage();
        Carriers.receiveMessage(msg, new Carriers.ReceiveMessageCarrier(from, body, subject, callbackHandler));
        callbackHandler.sendMessageDelayed(msg, delayMillis);
    }

    private void notifyUpdateUserInfo(Carriers.UpdateOccupantsCarrier.Event event, int index) {
        // FIXME 要在进入到聊天界面后才发送这个onJoin消息
        int delayMillis = 0;
        if (mInstance == null) {
            delayMillis = 200;
        }
        android.os.Message msg = callbackHandler.obtainMessage();
        Carriers.updateOccupants(msg, new Carriers.UpdateOccupantsCarrier(event, index, users, callbackHandler));
        callbackHandler.sendMessageDelayed(msg, delayMillis);
    }
}
