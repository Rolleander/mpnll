package com.broll.mpnll.server;

public class ServerLauncher {

    public static void main(String[] args) throws InterruptedException {
        new NettyServer().start();
    }
}