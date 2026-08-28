package com.broll.mpnll.message;

import com.google.protobuf.Message;

import java.util.function.Function;

public interface MessageRegistrySetup {

    void register(Message.Builder builder);

    default <T, M extends Message> void registerMapping(
        Class<T> objectType,
        M messageType,
        Function<T, M> encoder,
        Function<M, T> decoder
    ) {
        throw new UnsupportedOperationException("This registry does not support object mappings");
    }
}
