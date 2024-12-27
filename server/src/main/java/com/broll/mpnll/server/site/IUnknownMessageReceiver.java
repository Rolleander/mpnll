package com.broll.mpnll.server.site;

import com.google.protobuf.Message;

public interface IUnknownMessageReceiver {

    void unknownMessage(Message o);
}
