package com.broll.mpnll.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.nt.NT_LobbyInformation;
import com.google.protobuf.Any;

import org.junit.Test;

public class MessageRegistryTest {

    @Test
    public void registeredProtobufIsUnpackedFromAny() {
        MessageRegistryImpl registry = new MessageRegistryImpl();
        registry.register(NT_LobbyInformation.newBuilder(), "NT_LobbyInformation");
        NT_LobbyInformation settings = NT_LobbyInformation.newBuilder()
            .setLobbyName("test")
            .setPlayerLimit(4)
            .build();

        Object unpacked = registry.unpack(Any.pack(settings));

        assertSame(NT_LobbyInformation.class, unpacked.getClass());
        assertEquals(settings, unpacked);
    }
}
