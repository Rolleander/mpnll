package com.broll.mpnll.gwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.broll.mpnll.ConnectionDefaults;

import org.junit.Test;


public class UrlNormalizeTest {

    @Test
    public void makesBareHostsAbsolute() {
        assertEquals("ws://gainea.de:" + ConnectionDefaults.DEFAULT_WS_PORT, WebsocketUrl.normalizeUrl("gainea.de", false));
        assertEquals("ws://localhost:8088", WebsocketUrl.normalizeUrl("localhost:8088", false));
        assertEquals("wss://gainea.de:" + ConnectionDefaults.DEFAULT_WS_PORT, WebsocketUrl.normalizeUrl("gainea.de", true));
    }

    @Test
    public void preservesExplicitWebSocketUrls() {
        assertEquals("ws://localhost:8088/game", WebsocketUrl.normalizeUrl("ws://localhost:8088/game", false));
        assertEquals("wss://gainea.de:" + ConnectionDefaults.DEFAULT_WS_PORT + "/game", WebsocketUrl.normalizeUrl("wss://gainea.de/game", false));
    }

    @Test
    public void convertsHttpUrls() {
        assertEquals("ws://gainea.de:" + ConnectionDefaults.DEFAULT_WS_PORT + "/game", WebsocketUrl.normalizeUrl("http://gainea.de/game", true));
        assertEquals("wss://gainea.de:" + ConnectionDefaults.DEFAULT_WS_PORT + "/game", WebsocketUrl.normalizeUrl("https://gainea.de/game", false));
    }

    @Test
    public void rejectsEmptyHosts() {
        try {
            WebsocketUrl.normalizeUrl("  ", false);
            fail("exception not thrown");
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Test
    public void rejectsUnsupportedSchemes() {
        try {
            WebsocketUrl.normalizeUrl("tcp://gainea.de:8080", false);
            fail("exception not thrown");
        } catch (IllegalArgumentException ignored) {

        }
    }
}
