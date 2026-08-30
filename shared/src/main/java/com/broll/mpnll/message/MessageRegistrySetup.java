package com.broll.mpnll.message;

import com.google.protobuf.Message;

import java.util.function.Function;

public interface MessageRegistrySetup {

    void register(Message.Builder builder);

    /**
     * Registers a wire message and allows values of the same protobuf type to be
     * transported through {@code google.protobuf.Any}. The explicit protobuf
     * type name keeps this API compatible with GWT, where descriptor reflection
     * is unavailable.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void register(Message.Builder builder, String protobufTypeName) {
        register(builder);
        Message messageType = builder.getDefaultInstanceForType();
        registerMapping(
            (Class) messageType.getClass(),
            protobufTypeName,
            messageType,
            value -> (Message) value,
            value -> value
        );
    }

    default <T, M extends Message> void registerMapping(
        Class<T> objectType,
        String protobufTypeName,
        M messageType,
        Function<T, M> encoder,
        Function<M, T> decoder
    ) {
        throw new UnsupportedOperationException("This registry does not support object mappings");
    }
}
