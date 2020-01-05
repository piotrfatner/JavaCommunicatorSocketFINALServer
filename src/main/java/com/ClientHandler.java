package com;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private String name;
    private Socket socket;
    private Scanner input;
    private PrintWriter output;

    /**
     * Constructs a handler thread, squirreling away the socket. All the interesting
     * work is done in the run method. Remember the constructor is called from the
     * server's main method, so this has to be as short as possible.
     */
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            String userNameToTalkTo;

            // Keep requesting till user write a unique name
            while(true){
                output.println("Write name:");
                name = input.nextLine();
                if(name == null){
                    return;
                }
                synchronized (SocketServer.getUserNames()){
                    if(!SocketServer.getUserNames().contains(name)){
                        SocketServer.getUserNames().add(name);
                        SocketServer.getUserPrintWriters().add(output);
                        SocketServer.getUserNameAndPrintWriterMap().put(name, output);
                        break;
                    }
                }
            }

            // Tell everyone that this user appeared
            output.println("USER NAME ACCEPTED " + name);
            /*for (PrintWriter singlePrintWriter:SocketServer.getUserPrintWriters()
                    ) {
                singlePrintWriter.println("MESSAGE FROM SERVER: User " + name + "has joined!");
            }*/
            for(Map.Entry<String, PrintWriter> entry : SocketServer.getUserNameAndPrintWriterMap().entrySet()) {
                if(entry.getKey() != name){
                    entry.getValue().println("MESSAGE FROM SERVER: User " + name + " has joined!");
                }
            }

            // Choose user you want to write to
            String otherUser;
            while(true){
                output.println("Write user you want to talk to, if you want to talk with all guys write 'all':");
                for (Map.Entry<String, PrintWriter> entry : SocketServer.getUserNameAndPrintWriterMap().entrySet()
                        ) {
                    if(entry.getKey() != name){
                        output.println(entry.getKey()+",");
                    }
                }
                otherUser = input.nextLine();
                if(otherUser.equals("all")){
                    userNameToTalkTo = "all";
                    break;
                }
                if(SocketServer.getUserNameAndPrintWriterMap().containsKey(otherUser)){
                    userNameToTalkTo = otherUser;
                    break;
                }

            }

            // Keep listening a messages from this user / client socket to some user
            while(true){
                String singleInput = input.nextLine();
                if(singleInput.toLowerCase().startsWith("/quit")){
                    return;
                }
                /*for (PrintWriter singlePrintWriter:SocketServer.getUserPrintWriters()
                     ) {
                    singlePrintWriter.println("MESSAGE FROM "+name+": "+ singleInput);
                }*/
                if("all".equals(userNameToTalkTo)){
                    for(Map.Entry<String, PrintWriter> entry : SocketServer.getUserNameAndPrintWriterMap().entrySet()) {
                        if(entry.getKey() != name){
                            entry.getValue().println("MESSAGE FROM "+name+": "+ singleInput);
                        }
                    }
                } else{
                    SocketServer.getUserNameAndPrintWriterMap().get(userNameToTalkTo).println("MESSAGE FROM "+name+": "+ singleInput);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally { // closing socket
            if(output != null){
                SocketServer.getUserPrintWriters().remove(output);
            }
            if(name != null){
                System.out.println(name + " is leaving!");
                SocketServer.getUserNames().remove(name);
                for (PrintWriter singlePrintWriter:SocketServer.getUserPrintWriters()
                     ) {
                    singlePrintWriter.println("MESSAGE FROM "+name+" has left");
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
