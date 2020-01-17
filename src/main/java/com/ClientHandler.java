package com;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private String name;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    /**
     * Constructs a handler thread, squirreling away the socket. All the interesting
     * work is done in the run method. Remember the constructor is called from the
     * server's main method, so this has to be as short as possible.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            String userNameToTalkTo;

            // Keep requesting till user write a unique name
            while (true) {
                //output.println("Write name:");
                Message mB2 = new Message(EMessageType.SERVER_WAITING, "WAIT");
                output.writeObject(mB2);
                Message m = (Message) input.readObject();

                name = m.getTextMessage();
                if (name == null) {
                    return;
                }
                synchronized (SocketServer.getUserNames()) {
                    if (SocketServer.getUserNames().contains(name) && !name.equals("AllUsers")) {
                        //SocketServer.getUserNames().add(name);
                        //SocketServer.getUserPrintWriters().add(output);
                        SocketServer.getUserNameAndPrintWriterMap().put(name, output);
                        break;
                    } else {
                        Message mB = new Message(EMessageType.SERVER_REJECTED, "Error");
                        output.writeObject(mB);
                    }
                }
            }

            // Tell everyone that this user appeared
            //output.println("USER NAME ACCEPTED " + name);
            Message messageBack = new Message(EMessageType.SERVER_ACCEPTED, "USER NAME ACCEPTED " + name);
            output.writeObject(messageBack);


            // Listening new messages
            while (true) {
                Message m = (Message) input.readObject();
                handleMessage(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally { // closing socket
           /* if(output != null){
                SocketServer.getUserPrintWriters().remove(output);
            }
            if(name != null){
                System.out.println(name + " is leaving!");
                SocketServer.getUserNames().remove(name);
                for (PrintWriter singlePrintWriter:SocketServer.getUserPrintWriters()
                     ) {
                    singlePrintWriter.println("MESSAGE FROM "+name+" has left");
                }
            }*/
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleMessage(Message m) throws IOException {
        // there is handling few kinds of messages
        switch (m.geteMessageType()) {
            case SERVER_USERS:
                String avaliableContactList = getAvaliableContactsList();
                Message newMessage = new Message(EMessageType.SERVER_USERS, avaliableContactList);
                output.writeObject(newMessage);
                break;
            case TEXT:
                if (m.getAdressee() != null) {
                    if (!m.getAdressee().equals("AllUsers")) {
                        SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    } else {
                        broadcastMessageToAll(m);
                    }
                }
                break;
            case JPG:
                if (m.getAdressee() != null) {
                    if (!m.getAdressee().equals("AllUsers")) {
                        SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    } else {
                        broadcastMessageToAll(m);
                    }
                }
                break;
            case PNG:
                if (m.getAdressee() != null) {
                    if (!m.getAdressee().equals("AllUsers")) {
                        SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    } else {
                        broadcastMessageToAll(m);
                    }
                }
            case PDF_FILE:
                if (m.getAdressee() != null) {
                    if (!m.getAdressee().equals("AllUsers")) {
                        SocketServer.getUserNameAndPrintWriterMap().get(m.getAdressee()).writeObject(m);
                    } else {
                        broadcastMessageToAll(m);
                    }
                }

        }
    }

    public void broadcastMessageToAll(Message m) throws IOException {
        for (String userName : SocketServer.getUserNames()
                ) {
            if (!userName.equals("AllUsers") && SocketServer.getUserNameAndPrintWriterMap().get(userName) != null && !userName.equals(m.getSender())) {
                Message broadcastMessage;
                if(m.getTextMessage() != null){
                    broadcastMessage= new Message(m.geteMessageType(),m.getTextMessage(), userName, "AllUsers");
                } else{
                    broadcastMessage = new Message(m.geteMessageType(),m.getFileMessage(), userName, "AllUsers");
                }
                SocketServer.getUserNameAndPrintWriterMap().get(userName).writeObject(broadcastMessage);
            }
        }
    }

    public String getAvaliableContactsList() {
        String messageWithContactList = "";
        for (String userName : SocketServer.getUserNames()
                ) {
            if (!userName.equals(name)) {
                messageWithContactList += userName + ",";
            }
        }
        return messageWithContactList;
    }
}
