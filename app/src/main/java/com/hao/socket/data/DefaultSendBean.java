package com.hao.socket.data;


import com.android.socket.client.core.iocore.interfaces.ISendable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class DefaultSendBean implements ISendable {
    protected String content = "";

    @Override
    public final byte[] parse() {
        byte[] body = content.getBytes(Charset.defaultCharset());
        ByteBuffer bb = ByteBuffer.allocate(4 + body.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(body.length);
        bb.put(body);
        return bb.array();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
