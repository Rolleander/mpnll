package com.broll.mpnll.server.user;

import com.google.protobuf.Message;

import java.util.function.Consumer;

/** A lobby member controlled by server-side game logic rather than a connection. */
public class BotUser extends User {

    private Consumer<Message> messageReceiver = message -> { };

    public BotUser(int id, String name) {
        super(id, "bot:" + id, null);
        setName(name);
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public void send(Message message) {
        messageReceiver.accept(message);
    }

    @Override
    public void send(byte[] data) {
        // Serialized messages cannot be dispatched without the registry type.
    }

    public void setMessageReceiver(Consumer<Message> messageReceiver) {
        this.messageReceiver = messageReceiver != null ? messageReceiver : message -> { };
    }
}
