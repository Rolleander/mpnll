package com.broll.mpnll.gwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;


public class GwtWebSocketTest {

    @Test
    public void makesBareHostsAbsolute() {
        assertEquals("ws://gainea.de", GwtWebSocket.normalizeUrl("gainea.de", false));
        assertEquals("ws://localhost:8081", GwtWebSocket.normalizeUrl("localhost:8081", false));
        assertEquals("wss://gainea.de", GwtWebSocket.normalizeUrl("gainea.de", true));
    }

    @Test
    public void preservesExplicitWebSocketUrls() {
        assertEquals("ws://localhost:8081/game", GwtWebSocket.normalizeUrl("ws://localhost:8081/game", false));
        assertEquals("wss://gainea.de/game", GwtWebSocket.normalizeUrl("wss://gainea.de/game", false));
    }

    @Test
    public void convertsHttpUrls() {
        assertEquals("ws://gainea.de/game", GwtWebSocket.normalizeUrl("http://gainea.de/game", true));
        assertEquals("wss://gainea.de/game", GwtWebSocket.normalizeUrl("https://gainea.de/game", false));
    }

    @Test
    public void rejectsEmptyHosts() {
        try {
            GwtWebSocket.normalizeUrl("  ", false);
            fail("exception not thrown");
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Test
    public void rejectsUnsupportedSchemes() {
        try {
            GwtWebSocket.normalizeUrl("tcp://gainea.de:8080", false);
            fail("exception not thrown");
        } catch (IllegalArgumentException ignored) {

        }
    }
}
