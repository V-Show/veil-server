package com.veiljoy.spark.core;

import com.veiljoy.spark.android.net.Carriers;

/**
 * Created by Administrator on 2015/5/11.
 */
public class SparkError {
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

    Error mError;

    public SparkError(Error error) {
        this.mError = error;
    }

    public SparkError(Carriers.Error error) {
        this.mError = Error.unknown;
    }

    public Error getError() {
        return mError;
    }
}
