package com.veiljoy.spark.android.net.xmpp.rub;

import org.jivesoftware.smack.packet.IQ;

public class RubReq extends IQ {

    public static final String ELEMENT = QUERY_ELEMENT;
    public static final String NAMESPACE = "com.veil.rub";

    public RubReq() {
        super(ELEMENT, NAMESPACE);

        this.setType(IQ.Type.get);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
            IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        return xml;
    }
}
