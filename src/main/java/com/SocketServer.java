package com;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private static SocketServer socketServerInstance = null;
    // All user names, so we can check for duplicates upon registration.
    private static List<String> userNames = Arrays.asList("Piotr1", "Piotr2", "Piotr3","AllUsers");

    // List og all print writers for all users / clients, used for broadcast
    private static List<ObjectOutputStream> userPrintWriters = new ArrayList<ObjectOutputStream>();

    private static HashMap<String, ObjectOutputStream> userNameAndPrintWriterMap = new HashMap<String, ObjectOutputStream>();

    private SocketServer(){}

    public static SocketServer getSocketServerInstance(){
        if(socketServerInstance == null){
            socketServerInstance = new SocketServer();
        }
        return socketServerInstance;
    }

    public void runSocketServer() throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(100);
        ServerSocket socketListener = new ServerSocket(59001);
        System.out.println("The chat server is running...");
        while(true){
            threadPool.execute(new ClientHandler(socketListener.accept()));
        }
    }

    public static List<String> getUserNames() {
        return userNames;
    }

    public static List<ObjectOutputStream> getUserPrintWriters() {
        return userPrintWriters;
    }

    public static HashMap<String, ObjectOutputStream> getUserNameAndPrintWriterMap() {
        return userNameAndPrintWriterMap;
    }
}
