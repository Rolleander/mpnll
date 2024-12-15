package com.broll.mpnll.message;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import java.util.HashMap;
import java.util.Map;

public class MessageRegistryImpl implements MessageRegistry {

    private int registerIndex = 0;
    private Map<Descriptors.Descriptor, Integer> types = new HashMap<>();
    private Map<Integer, Parser> parsers = new HashMap<>();

    public MessageRegistryImpl(){}

    public <T extends Message> void  register(Message.Builder builder, Parser<T> parser){
        parsers.put(registerIndex, parser);
        types.put(builder.getDescriptorForType(), registerIndex  );
        registerIndex ++;
    }

    @Override
    public Message parseMessage(byte[] bytes, int type) throws InvalidProtocolBufferException {
        return (Message) parsers.get(type).parseFrom(bytes);
    }

    @Override
    public int getType(Message message) {
        return types.get(message.getDescriptorForType());
    }

}
