package com.veiljoy.spark.android.net.xmpp;

import android.os.Handler;

import com.veiljoy.spark.android.net.Configs;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
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
    List<Handler> handlerList;

    public static MUC getInstanceFor(String room, boolean create) throws Exception {
        if (mInstance != null) {
            throw new Exception("there is a muc already");
        }
        mInstance = new MUC(room, create);
        return mInstance;
    }

    public static MUC getInstance() throws Exception {
        if (mInstance == null) {
            throw new Exception("no muc instance");
        }
        return mInstance;
    }

    private MUC(String room, boolean create) throws SmackException, XMPPException.XMPPErrorException {
        AbstractXMPPConnection connection = Connection.getInstance().getConnection();
        muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(room + Configs.ROOM_JID);

        // get the real name of user
        // FIXME use the nickname field of vCard to get the real name of user
        VCard vCard = VCardManager.getInstanceFor(connection).loadVCard();
        String name = vCard.getNickName();
        if (create) {
            createRoom(name);
        } else {
            muc.join(name);
        }

        handlerList = new ArrayList<>();
    }

    private void createRoom(String room) throws XMPPException.XMPPErrorException, SmackException {
        muc.create(room);

        // 获得聊天室的配置表单
        Form form = muc.getConfigurationForm();
        // 根据原始表单创建一个要提交的新表单。
        Form submitForm = form.createAnswerForm();
        // 向要提交的表单添加默认答复
        for (Iterator<FormField> fields = form.getFields().iterator(); fields
                .hasNext();) {
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
        // submitForm.setAnswer("muc#roomconfig_whois", Arrays.asList("anyone"));
        submitForm.setAnswer("muc#roomconfig_whois", Arrays.asList("moderators"));
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

    public void registerHandler(Handler handler) {
        handlerList.add(handler);
    }

    public void unregisterHandler(Handler handler) {
        handlerList.remove(handler);
    }
}
