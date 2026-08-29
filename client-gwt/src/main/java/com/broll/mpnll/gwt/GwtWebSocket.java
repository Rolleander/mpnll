package com.broll.mpnll.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A WebSocket abstraction to handle the connection and message passing.
 * This class simulates a basic WebSocket interface in GWT.
 */
public class GwtWebSocket {

    private String url;
    private com.google.gwt.core.client.JavaScriptObject jsWebSocket;
    private Listener listener;

    public void open(String url) {
        this.url = normalizeUrl(url, isSecurePage());
        jsWebSocket = createWebSocket(this.url);
        if (listener != null) {
            installListener();
        }
    }

    public void send(byte[] message) {
        sendMessage(jsWebSocket, message);
    }

    public void close() {
        closeWebSocket(jsWebSocket);
    }

    public boolean isOpen() {
        return isWebSocketOpen(jsWebSocket);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (jsWebSocket != null) {
            installListener();
        }
    }

    private void installListener() {
        setOnOpenHandler(jsWebSocket, listener);
        setOnMessageHandler(jsWebSocket, listener);
        setOnCloseHandler(jsWebSocket, listener);
        setOnErrorHandler(jsWebSocket, listener);
    }

    private native JavaScriptObject createWebSocket(String url) /*-{
            var socket = new $wnd.WebSocket(url);
            socket.binaryType = "arraybuffer";
            return socket;
        }-*/;

    private native boolean isSecurePage() /*-{
            return $wnd.location.protocol === "https:";
        }-*/;

    static String normalizeUrl(String value, boolean securePage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WebSocket host must not be empty");
        }

        String url = value.trim();
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("ws://") || lowerUrl.startsWith("wss://")) {
            return url;
        }
        if (lowerUrl.startsWith("http://")) {
            return "ws://" + url.substring("http://".length());
        }
        if (lowerUrl.startsWith("https://")) {
            return "wss://" + url.substring("https://".length());
        }

        String scheme = securePage ? "wss:" : "ws:";
        if (url.startsWith("//")) {
            return scheme + url;
        }
        if (url.contains("://")) {
            throw new IllegalArgumentException("Unsupported WebSocket URL scheme: " + value);
        }
        return scheme + "//" + url;
    }

    private native void sendMessage(JavaScriptObject socket, byte[] message) /*-{
            var arrayBuffer = new ArrayBuffer(message.length);
            var uint8Array = new Uint8Array(arrayBuffer);
            for (var i = 0; i < message.length; i++) {
                uint8Array[i] = message[i];
            }
            socket.send(arrayBuffer);  // Send the byte array via WebSocket
        }-*/;

    private native boolean isWebSocketOpen(JavaScriptObject socket) /*-{
            return !!socket && socket.readyState === $wnd.WebSocket.OPEN;
        }-*/;

    private native void closeWebSocket(JavaScriptObject socket) /*-{
            if (socket) {
                socket.close();
            }
        }-*/;

    private native void setOnOpenHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onopen = $entry(function() {
                listener.@com.broll.mpnll.gwt.GwtWebSocket.Listener::onOpen()();
            });
        }-*/;

    private native void setOnMessageHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onmessage = $entry(function(event) {
                var view = new Uint8Array(event.data);
                var bytes = @com.broll.mpnll.gwt.GwtWebSocket::newByteArray(I)(view.length);
                for (var i = 0; i < view.length; i++) {
                    bytes[i] = view[i] > 127 ? view[i] - 256 : view[i];
                }
                listener.@com.broll.mpnll.gwt.GwtWebSocket.Listener::onMessage([B)(bytes);
            });
        }-*/;

    private native void setOnCloseHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onclose = $entry(function() {
                listener.@com.broll.mpnll.gwt.GwtWebSocket.Listener::onClose()();
            });
        }-*/;

    private native void setOnErrorHandler(JavaScriptObject socket, Listener listener) /*-{
            var self = this;
            socket.onerror = $entry(function(event) {
                var message = event && event.message ? event.message : "WebSocket error";
                self.@com.broll.mpnll.gwt.GwtWebSocket::notifyError(Ljava/lang/String;)(message);
            });
        }-*/;

    private static byte[] newByteArray(int length) {
        return new byte[length];
    }

    private void notifyError(String message) {
        listener.onError(new RuntimeException(message));
    }

    public interface Listener {
        void onOpen();

        void onMessage(byte[] message);

        void onClose();

        void onError(Throwable error);
    }
}
