package com.veiljoy.spark.core;

/**
 * Created by Administrator on 2015/5/11.
 */
public class SparkAction {
    public enum Action {
        none,
        connect,
        register,
        login,
        upload_vcard,
        rub,
        enter_room,
        send_message,
        kick
    }

    Action action;
    Object args[];

    public SparkAction(Action action, Object... args) {
        this.action = action;
        this.args = args.clone();
    }

    public Action getAction() {
        return action;
    }

    public Object[] getArgs() {
        return args;
    }
}
