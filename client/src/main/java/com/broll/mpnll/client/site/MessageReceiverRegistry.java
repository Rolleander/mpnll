package com.broll.mpnll.client.site;

import com.google.protobuf.Message;

import java.util.function.Consumer;

public interface MessageReceiverRegistry {

    <T extends Message> void add(Message.Builder builder, Consumer<T> receiver);

}
