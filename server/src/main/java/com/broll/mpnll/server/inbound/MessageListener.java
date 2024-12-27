package com.broll.mpnll.server.inbound;

import com.broll.mpnll.server.connection.ClientConnection;
import com.google.protobuf.Message;

public interface MessageListener {

    void received(ClientConnection connection, Message message);

}
