package com.broll.mpnll.gwt.smoke.client;

import com.broll.mpnll.gwt.GwtWebSocket;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SmokeTestEntryPoint implements EntryPoint {

    private final GwtWebSocket socket = new GwtWebSocket();
    private final Label status = new Label("Disconnected");
    private final TextArea events = new TextArea();

    @Override
    public void onModuleLoad() {
        TextBox url = new TextBox();
        url.setWidth("360px");
        url.setText("ws://" + Window.Location.getHostName() + ":8081/");

        Button connect = new Button("Connect");
        Button disconnect = new Button("Disconnect");

        socket.setListener(new GwtWebSocket.Listener() {
            @Override
            public void onOpen() {
                status.setText("Connected");
                append("OPEN");
            }

            @Override
            public void onMessage(byte[] message) {
                append("MESSAGE: " + message.length + " bytes");
            }

            @Override
            public void onClose() {
                status.setText("Disconnected");
                append("CLOSE");
            }

            @Override
            public void onError(Throwable error) {
                status.setText("Error");
                append("ERROR: " + error.getMessage());
            }
        });

        connect.addClickHandler(event -> {
            append("Connecting to " + url.getText());
            socket.open(url.getText());
        });
        disconnect.addClickHandler(event -> socket.close());

        events.setReadOnly(true);
        events.setVisibleLines(12);
        events.setCharacterWidth(60);

        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(8);
        panel.add(new Label("MPNLL GWT WebSocket smoke test"));
        panel.add(url);
        panel.add(connect);
        panel.add(disconnect);
        panel.add(status);
        panel.add(events);
        RootPanel.get("smoke-test").add(panel);
    }

    private void append(String event) {
        events.setText(events.getText() + event + "\n");
        events.getElement().setScrollTop(events.getElement().getScrollHeight());
    }
}
