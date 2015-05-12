package com.veiljoy.spark.android.net;

import android.os.Handler;
import android.os.Message;

import com.veiljoy.spark.core.UserInfo;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.Map;

/**
 * Created by Administrator on 2015/5/7.
 */
public class Carriers {
    public static final int CARRIER_CONNECT = 0;
    public static final int CARRIER_REGISTER = 1;
    public static final int CARRIER_LOGIN = 2;
    public static final int CARRIER_UPLOAD_VCARD = 3;
    public static final int CARRIER_RUB = 4;
    public static final int CARRIER_ENTER_ROOM = 5;
    public static final int CARRIER_RECEIVE_MESSAGE = 7;
    public static final int CARRIER_SEND_MESSAGE = 8;
    public static final int CARRIER_UPDATE_OCCUPANTS = 9;
    public static final int CARRIER_KICK = 10;

    public enum Error {
        no_error,
        // xmpp error exception
        conflict,
        forbidden,
        not_allowed,
        item_not_found,
        not_authorized,
        // smack exception
        already_connected,
        already_logged_in,
        unknown,
    }

    public static class Constants {
        public static final String GenderField = "GENDER";
        public static final String GenderMale = "male";
        public static final String GenderFemale = "female";
    }

    public static class ConnectCarrier {
        public Handler handler;
        public Error error;

        public ConnectCarrier(Handler handler) {
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "ConnectCarrier "
                    + "handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class RegisterCarrier {
        public String username;
        public String nickname;
        public String password;
        public Handler handler;
        public Error error;

        public RegisterCarrier(String username, String nickname, String password, Handler handler) {
            this.username = username;
            this.nickname = nickname;
            this.password = password;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "RegisterCarrier "
                    + "username=" + username
                    + "nickname=" + nickname
                    + ", password=" + password
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class LoginCarrier {
        public String username;
        public String password;
        public Handler handler;
        public Error error;

        public LoginCarrier(String username, String password, Handler handler) {
            this.username = username;
            this.password = password;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "LoginCarrier "
                    + "username=" + username
                    + ", password=" + password
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class UploadVCardCarrier {
        public VCard vCard;
        public Handler handler;
        public Error error;

        public UploadVCardCarrier(VCard vCard, Handler handler) {
            this.vCard = vCard;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "UploadVCardCarrier "
                    + "vCard=" + vCard
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class RubCarrier {
        public String room;
        public boolean create;
        public Handler handler;
        public Error error;

        public RubCarrier(Handler handler) {
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "RubCarrier "
                    + "room=" + room
                    + ", create=" + create
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class EnterRoomCarrier {
        public String room;
        public boolean create;
        public Handler handler;
        public Error error;

        public EnterRoomCarrier(String room, boolean create, Handler handler) {
            this.room = room;
            this.create = create;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "EnterRoomCarrier "
                    + "room=" + room
                    + ", create=" + create
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class ReceiveMessageCarrier {
        public String from;
        public String body;
        public String subject;
        public Handler handler;
        public Error error;

        public ReceiveMessageCarrier(String from, String body, String subject, Handler handler) {
            this.from = from;
            this.body = body;
            this.subject = subject;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "EnterRoomCarrier "
                    + "from=" + from
                    + ", body=" + body
                    + ", subject=" + subject
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class SendMessageCarrier {
        public int id;
        public String body;
        public Handler handler;
        public Error error;

        public SendMessageCarrier(int id, String body, Handler handler) {
            this.id = id;
            this.body = body;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "SendMessageCarrier "
                    + "id=" + id
                    + ", body=" + body
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class UpdateOccupantsCarrier {
        public enum Event {
            join,
            left
        }

        public Event event;
        public int index;
        public UserInfo[] users;
        public Handler handler;
        public Error error;

        public UpdateOccupantsCarrier(Event event, int index, UserInfo[] users, Handler handler) {
            this.event = event;
            this.index = index;
            this.users = users;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "UpdateOccupantsCarrier "
                    + "event=" + event
                    + ", index=" + index
                    + ", users=" + users
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static class KickCarrier {
        public String nickname;
        public Handler handler;
        public Error error;

        public KickCarrier(String nickname, Handler handler) {
            this.nickname = nickname;
            this.handler = handler;
            this.error = Error.no_error;
        }

        @Override
        public String toString() {
            return "KickCarrier "
                    + "nickname=" + nickname
                    + ", handler=" + handler
                    + ", error=" + error;
        }
    }

    public static void connect(Message msg, ConnectCarrier carrier) {
        msg.what = CARRIER_CONNECT;
        msg.obj = carrier;
    }

    public static void register(Message msg, RegisterCarrier registerCarrier) {
        msg.what = CARRIER_REGISTER;
        msg.obj = registerCarrier;
    }

    public static void login(Message msg, LoginCarrier loginCarrier) {
        msg.what = CARRIER_LOGIN;
        msg.obj = loginCarrier;
    }

    public static void uploadVCard(Message msg, UploadVCardCarrier carrier) {
        msg.what = CARRIER_UPLOAD_VCARD;
        msg.obj = carrier;
    }

    public static void rub(Message msg, RubCarrier carrier) {
        msg.what = CARRIER_RUB;
        msg.obj = carrier;
    }

    public static void enterRoom(Message msg, EnterRoomCarrier carrier) {
        msg.what = CARRIER_ENTER_ROOM;
        msg.obj = carrier;
    }

    public static void receiveMessage(Message msg, ReceiveMessageCarrier carrier) {
        msg.what = CARRIER_RECEIVE_MESSAGE;
        msg.obj = carrier;
    }

    public static void sendMessage(Message msg, SendMessageCarrier carrier) {
        msg.what = CARRIER_SEND_MESSAGE;
        msg.obj = carrier;
    }

    public static void updateOccupants(Message msg, UpdateOccupantsCarrier carrier) {
        msg.what = CARRIER_UPDATE_OCCUPANTS;
        msg.obj = carrier;
    }

    public static void kick(Message msg, KickCarrier carrier) {
        msg.what = CARRIER_KICK;
        msg.obj = carrier;
    }

    public static Error convertError(Exception e) {
        Error error = Error.unknown;
        if (e == null) {
            error = Error.no_error;
        } else if (e instanceof SmackException) {
            if (e instanceof SmackException.AlreadyConnectedException) {
                error = Error.already_connected;
            } else if (e instanceof SmackException.AlreadyLoggedInException) {
                error = Error.already_logged_in;
            }
        } else if (e instanceof XMPPException.XMPPErrorException) {
            XMPPException.XMPPErrorException XMPPException = (XMPPException.XMPPErrorException) e;
            org.jivesoftware.smack.packet.XMPPError.Condition condition = XMPPException.getXMPPError().getCondition();
            if (condition == org.jivesoftware.smack.packet.XMPPError.Condition.conflict) {
                error = Error.conflict;
            } else if (condition == org.jivesoftware.smack.packet.XMPPError.Condition.item_not_found) {
                error = Error.item_not_found;
            } else if (condition == org.jivesoftware.smack.packet.XMPPError.Condition.forbidden) {
                error = Error.forbidden;
            } else if (condition == org.jivesoftware.smack.packet.XMPPError.Condition.not_allowed) {
                error = Error.not_allowed;
            }
        }
        return error;
    }
}
