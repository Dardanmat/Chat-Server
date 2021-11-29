package it.fi.itismeucci;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerChat {
    
    private ServerSocket server;
    private int port;
    
    private ArrayList<ClientHandler> clientList;

    public ServerChat(int port) {
        this.port = port;
        clientList = new ArrayList<>();
    }
    
    public void start() throws IOException{
        
        System.out.println("Avvio server...");
        server = new ServerSocket(port);
        
        while (true) {        
            System.out.println("In attesa di client...");
            clientList.add(new ClientHandler(server.accept(), this));
            System.out.println("Client arrivato, attivo servizio...");
            clientList.get(clientList.size() - 1).start();
        }
    }
    
    public void executeCommand(String message, ClientHandler sender){
        
        switch(findCommand(message)){
            case "/g":
                sendToAll(message);
                break;
                
            case "/msg":
                sendToOne(message, sender);
                break;
                
            case "/quit":
                disconnectClient(sender);
                sender.close();
                break;
                
            case "/list":
                sender.sendConnectedUsersList();
                break;
                
            default:
                break;
        }
    }
    
    public void sendToOne(String message, ClientHandler sender){
        ClientHandler receiver = findClient(findUsername(message));
        
        if(receiver != null){
            receiver.sendToClient("/msg @" + sender.getName() + " " + findMessageBody(message));
        }else{
            sender.sendToClient("/msg_offline @" + findUsername(message));
        }
    }
    
    public ClientHandler findClient(String username){
        for (ClientHandler client : clientList) {
            if(client.getName().equals(username)) return client;
        }
        return null;
    }
    
    public boolean disconnectClient(ClientHandler client){
        notifyUserDisconnection(client.getName());
        return removeClient(client);   
    }
    
    public boolean removeClient(ClientHandler client){
        return clientList.remove(client);
    }
    
    public void notifyUserConnection(String username){
        for (ClientHandler client : clientList) {
            if(client.isLogged()){
                client.notifyUserConnection(username);
            }
        }
    }
    
    public void notifyUserDisconnection(String username){
        for (ClientHandler client : clientList) {
            if(client.isLogged()){
                client.notifyUserDisconnection(username);
            }
        }
    }
    
    public void sendToAll(String message){
        
        System.out.println("Invio a tutti " + message);
        
        for (ClientHandler client : clientList) {
            //se l'utente è loggato
            if(client.isLogged()){
                //se l'utente non è il mittente
                if(!client.getName().equals(findUsername(message))){
                     client.sendToClient(message);
                }
            }
        }
    }
    
    public ArrayList<ClientHandler> getClientList(){
        return clientList;
    }
    
    public String findCommand(String msg){
        return msg.split(" ")[0];
    }
    
    public String findUsername(String msg){
        return msg.split(" ")[1].substring(1);
    }
    
    public String findMessageBody(String msg){
        String body = "";
        String[] list = msg.split(" ");
        
        for (int i = 2; i < list.length; i++) {
            body += " " + list[i];
        }
        
        return body.substring(1);
    }
    
}