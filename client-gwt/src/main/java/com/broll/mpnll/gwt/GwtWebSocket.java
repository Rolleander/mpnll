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
        this.url = url;
        jsWebSocket = createWebSocket(url);
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
        setOnOpenHandler(jsWebSocket, listener);
        setOnMessageHandler(jsWebSocket, listener);
        setOnCloseHandler(jsWebSocket, listener);
        setOnErrorHandler(jsWebSocket, listener);
    }

    private native JavaScriptObject createWebSocket(String url) /*-{
            var socket = new $wnd.WebSocket(url);
            return socket;
        }-*/;

    private native void sendMessage(JavaScriptObject socket, byte[] message) /*-{
            var arrayBuffer = new ArrayBuffer(message.length);
            var uint8Array = new Uint8Array(arrayBuffer);
            for (var i = 0; i < message.length; i++) {
                uint8Array[i] = message[i];
            }
            socket.send(arrayBuffer);  // Send the byte array via WebSocket
        }-*/;

    private native boolean isWebSocketOpen(JavaScriptObject socket) /*-{
            return socket.readyState === WebSocket.OPEN;
        }-*/;

    private native void closeWebSocket(JavaScriptObject socket) /*-{
            socket.close();
        }-*/;

    private native void setOnOpenHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onopen = function() {
                listener.@WebSocketClient.WebSocket.Listener::onOpen()();
            };
        }-*/;

    private native void setOnMessageHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onmessage = function(event) {
                var byteArray = new Uint8Array(event.data);
                listener.@WebSocketClient.WebSocket.Listener::onMessage([B)(byteArray);
            };
        }-*/;

    private native void setOnCloseHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onclose = function() {
                listener.@WebSocketClient.WebSocket.Listener::onClose()();
            };
        }-*/;

    private native void setOnErrorHandler(JavaScriptObject socket, Listener listener) /*-{
            socket.onerror = function(event) {
                listener.@WebSocketClient.WebSocket.Listener::onError(Ljava/lang/Throwable;)(new Error(event.message));
            };
        }-*/;

    public interface Listener {
        void onOpen();

        void onMessage(byte[] message);

        void onClose();

        void onError(Throwable error);
    }
}

