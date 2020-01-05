package com;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SocketServer socketServer = SocketServer.getSocketServerInstance();
        socketServer.runSocketServer();
    }
}
