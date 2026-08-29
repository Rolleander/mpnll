package com.broll.mpnll.client;

import com.broll.mpnll.ConnectionDefaults;

import java.net.URI;
import java.net.URISyntaxException;

final class TcpServerAddress {

    final String host;
    final int port;

    private TcpServerAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static TcpServerAddress parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Host must not be empty");
        }
        String address = value.trim();
        String uriValue = address.contains("://") ? address : "tcp://" + address;
        try {
            URI uri = new URI(uriValue);
            validate(uri, value);
            int port = uri.getPort() == -1 ? ConnectionDefaults.DEFAULT_TCP_PORT : uri.getPort();
            return new TcpServerAddress(uri.getHost(), port);
        } catch (URISyntaxException error) {
            throw new IllegalArgumentException("Invalid TCP client address: " + value, error);
        }
    }

    private static void validate(URI uri, String originalValue) {
        if (!"tcp".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(
                "TCP client address must use the tcp:// scheme: " + originalValue
            );
        }
        if (uri.getHost() == null || uri.getHost().isEmpty()) {
            throw new IllegalArgumentException("Invalid TCP client address: " + originalValue);
        }
        if (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) {
            throw new IllegalArgumentException(
                "TCP client address must not contain a path: " + originalValue
            );
        }
    }
}
