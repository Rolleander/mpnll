package com.broll.mpnll.client;

public class ResponseException extends RuntimeException {

    public ResponseException(String reason) {
        super(reason);
    }
}
