package com.broll.mpnll.server;

public class ServerLauncher {

    public static void main(String[] args) throws InterruptedException {
        int tcpPort = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int websocketPort = args.length > 1 ? Integer.parseInt(args[1]) : 8081;
        new MpnllServer().open(tcpPort, websocketPort);
    }
}
