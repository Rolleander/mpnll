package com.broll.mpnll.gwt.smoke.client;

import com.broll.mpnll.NtLobbyMessagesRegistry;
import com.broll.mpnll.client.MpnllClient;
import com.broll.mpnll.client.impl.LobbyLookup;
import com.broll.mpnll.gwt.MpnllWebsocketClient;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SmokeTestEntryPoint implements EntryPoint {

    private final Label status = new Label("Starting");
    private final TextArea events = new TextArea();
    private final TextBox url = new TextBox();
    private MpnllClient client;

    @Override
    public void onModuleLoad() {
        String configuredPort = Window.Location.getParameter("wsPort");
        String port = configuredPort == null || configuredPort.isEmpty() ? "8081" : configuredPort;
        url.setWidth("360px");
        url.setText("ws://" + Window.Location.getHostName() + ":" + port + "/");

        Button run = new Button("Run lobby-list sanity test");
        Button disconnect = new Button("Disconnect");
        run.addClickHandler(event -> runSanityTest());
        disconnect.addClickHandler(event -> closeClient());

        status.getElement().setId("sanity-status");
        events.getElement().setId("sanity-events");
        events.setReadOnly(true);
        events.setVisibleLines(12);
        events.setCharacterWidth(60);

        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(8);
        panel.add(new Label("MPNLL GWT lobby-list sanity test"));
        panel.add(url);
        panel.add(run);
        panel.add(disconnect);
        panel.add(status);
        panel.add(events);
        RootPanel.get("smoke-test").add(panel);
        client = new MpnllClient(new MpnllWebsocketClient());
        client.registerMessages(NtLobbyMessagesRegistry::register);
        runSanityTest();
    }

    private void runSanityTest() {
        setStatus("LISTING", "Connecting to " + url.getText() + " and requesting lobby list");
        client.listLobbies(url.getText())
            .onSuccess(this::pass)
            .onFailure(error -> fail(error.getMessage()));
    }

    private void pass(LobbyLookup lookup) {
        setStatus(
            "PASS",
            "PASS: " + lookup.getServerName() + " listed " + lookup.getLobbies().size() + " lobbies"
        );
    }

    private void closeClient() {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }

    private void fail(String reason) {
        setStatus("FAIL", "FAIL: " + (reason == null ? "Unknown error" : reason));
    }

    private void setStatus(String state, String event) {
        status.setText(state);
        status.getElement().setAttribute("data-state", state);
        events.setText(events.getText() + event + "\n");
        events.getElement().setScrollTop(events.getElement().getScrollHeight());
    }
}
