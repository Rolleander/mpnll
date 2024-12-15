package com.broll.mpnll.server.inbound;

import com.broll.mpnll.server.session.ClientSession;
import com.google.protobuf.Message;

public interface MessageListener {

    void received (ClientSession session, Message message);

}
