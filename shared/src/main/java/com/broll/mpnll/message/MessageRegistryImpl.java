package com.broll.mpnll.message;

import com.broll.mpnll.NetworkException;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageRegistryImpl implements MessageRegistry, MessageRegistrySetup {

    private int registerIndex = 0;
    private Map<Descriptors.Descriptor, Integer> types = new HashMap<>();
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
        types.put(builder.getDescriptorForType(), registerIndex);
        registerIndex++;
        registerIdentityMapping(messageType);
    }

    @Override
    public <T, M extends Message> void registerMapping(
        Class<T> objectType,
        M messageType,
        Function<T, M> encoder,
        Function<M, T> decoder
    ) {
        ObjectMapping<T, M> mapping = new ObjectMapping<>(messageType, encoder, decoder);
        mappingsByObjectType.put(objectType, mapping);
        mappingsByMessageType.put(messageType.getDescriptorForType().getFullName(), mapping);
    }

    @Override
    public Message parseMessage(byte[] bytes, int type) {
        try {
            return (Message) parsers.get(type).parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new NetworkException("Failed parsing message", e);
        }
    }

    @Override
    public int getType(Message message) {
        return types.get(message.getDescriptorForType());
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
        return Any.pack(mapping.encode(value));
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
        return objectType.cast(mapping.decode(value));
    }

    private void registerIdentityMapping(Message messageType) {
        Class<Message> objectType = (Class<Message>) messageType.getClass();
        registerMapping(objectType, messageType, Function.identity(), Function.identity());
    }

    private String messageTypeName(Any value) {
        String typeUrl = value.getTypeUrl();
        int separator = typeUrl.lastIndexOf('/');
        return separator == -1 ? typeUrl : typeUrl.substring(separator + 1);
    }

    private static final class ObjectMapping<T, M extends Message> {

        private final M messageType;
        private final Function<T, M> encoder;
        private final Function<M, T> decoder;

        private ObjectMapping(M messageType, Function<T, M> encoder, Function<M, T> decoder) {
            this.messageType = messageType;
            this.encoder = encoder;
            this.decoder = decoder;
        }

        private Message encode(T value) {
            return encoder.apply(value);
        }

        private T decode(Any value) {
            String expectedType = messageType.getDescriptorForType().getFullName();
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
