package com.broll.mpnll.message;

import com.broll.mpnll.NetworkException;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageRegistryImpl implements MessageRegistry, MessageRegistrySetup {

    private int registerIndex = 0;
    private Map<Class<?>, Integer> types = new HashMap<>();
    private Map<Integer, Parser> parsers = new HashMap<>();
    private Map<Class<?>, ObjectMapping<?, ?>> mappingsByObjectType = new HashMap<>();
    private Map<String, ObjectMapping<?, ?>> mappingsByMessageType = new HashMap<>();

    public MessageRegistryImpl() {
    }

    @Override
    public void register(Message.Builder builder) {
        Message messageType = builder.getDefaultInstanceForType();
        Parser parser = messageType.getParserForType();
        parsers.put(registerIndex, parser);
        types.put(messageType.getClass(), registerIndex);
        registerIndex++;
    }

    @Override
    public <T, M extends Message> void registerMapping(
        Class<T> objectType,
        String protobufTypeName,
        M messageType,
        Function<T, M> encoder,
        Function<M, T> decoder
    ) {
        ObjectMapping<T, M> mapping =
            new ObjectMapping<>(protobufTypeName, messageType, encoder, decoder);
        mappingsByObjectType.put(objectType, mapping);
        mappingsByMessageType.put(protobufTypeName, mapping);
    }

    @Override
    public Message parseMessage(byte[] bytes, int type) {
        Parser parser = parsers.get(type);
        if (parser == null) {
            throw new NetworkException("No parser registered for this message type");
        }
        try {
            return (Message) parsers.get(type).parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new NetworkException("Failed parsing message:", e);
        }
    }

    @Override
    public int getType(Message message) {
        Integer type = types.get(message.getClass());
        if (type == null) {
            throw new IllegalArgumentException(
                "Message type is not registered: " + message.getClass().getName()
            );
        }
        return type;
    }

    @Override
    public Any pack(Object value) {
        if (value == null) {
            return Any.getDefaultInstance();
        }
        ObjectMapping mapping = mappingsByObjectType.get(value.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(
                "No message mapping registered for " + value.getClass().getName()
            );
        }
        Message encoded = mapping.encode(value);
        return Any.newBuilder()
            .setTypeUrl("type.googleapis.com/" + mapping.protobufTypeName)
            .setValue(ByteString.copyFrom(encoded.toByteArray()))
            .build();
    }

    @Override
    public Object unpack(Any value) {
        if (value.equals(Any.getDefaultInstance())) {
            return null;
        }
        ObjectMapping<?, ?> mapping = mappingsByMessageType.get(messageTypeName(value));
        if (mapping == null) {
            return value;
        }
        return mapping.decode(value);
    }

    @Override
    public <T> T unpack(Any value, Class<T> objectType) {
        if (value.equals(Any.getDefaultInstance())) {
            return null;
        }
        ObjectMapping<?, ?> mapping = mappingsByObjectType.get(objectType);
        if (mapping == null) {
            throw new IllegalArgumentException(
                "No message mapping registered for " + objectType.getName()
            );
        }
        return (T) mapping.decode(value);
    }

    private String messageTypeName(Any value) {
        String typeUrl = value.getTypeUrl();
        int separator = typeUrl.lastIndexOf('/');
        return separator == -1 ? typeUrl : typeUrl.substring(separator + 1);
    }

    private static final class ObjectMapping<T, M extends Message> {

        private final String protobufTypeName;
        private final M messageType;
        private final Function<T, M> encoder;
        private final Function<M, T> decoder;

        private ObjectMapping(
            String protobufTypeName,
            M messageType,
            Function<T, M> encoder,
            Function<M, T> decoder
        ) {
            this.protobufTypeName = protobufTypeName;
            this.messageType = messageType;
            this.encoder = encoder;
            this.decoder = decoder;
        }

        private Message encode(T value) {
            return encoder.apply(value);
        }

        private T decode(Any value) {
            String expectedType = protobufTypeName;
            String typeUrl = value.getTypeUrl();
            if (!typeUrl.equals(expectedType) && !typeUrl.endsWith("/" + expectedType)) {
                throw new NetworkException(
                    "Failed decoding message: expected message " + expectedType + " but received " + typeUrl
                );
            }
            try {
                M message = (M) messageType.getParserForType().parseFrom(value.getValue());
                return decoder.apply(message);
            } catch (InvalidProtocolBufferException e) {
                throw new NetworkException("Failed decoding message", e);
            }
        }
    }

}
