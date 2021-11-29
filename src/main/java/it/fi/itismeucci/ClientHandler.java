package it.fi.itismeucci;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread{
    
    private boolean logged;
    private String message;
    //the name of the client is the name of the thread, to call it use super.getName()
    
    private Socket client;
    private ServerChat server;
    
    private BufferedReader receive;
    private DataOutputStream send;
    
    private boolean open;
    
    public ClientHandler(Socket client, ServerChat server) throws IOException{
        super();
        this.client = client;
        this.server = server;
        
        logged = false;
        receive = new BufferedReader(new InputStreamReader(client.getInputStream()));
        send = new DataOutputStream(client.getOutputStream());
    }

    @Override
    public void run() {
        open = true;
        while(open){
            //controllo nome
            login();
            
            System.out.println(super.getName() + " - Login completato");
            
            server.notifyUserConnection(super.getName());
            sendConnectedUsersList();
            
            while(open){
                
                System.out.println(super.getName() + " - Aspetto un messaggio");
                message = receiveMessage();
                
                if(message == null){
                    System.out.println(getName() + " disconnected");
                    server.disconnectClient(this);
                    break;
                }
                
                System.out.println(super.getName() + " - Ricevuto il messaggio " + message);
                server.executeCommand(message, this);
                
            }

            if(message == null){
                break;
            }
        }
        close();
    }
    
    public void login(){
        
        while(!logged){
            message = receiveMessage();
            
            if(server.findClient(server.findUsername(message)) != null){
                sendToClient("/name_validity false");
                
            }else{
                sendToClient("/name_validity true");
                super.setName(server.findUsername(message));
                logged = true;
                
            }
        }
    }
    
    public void close(){
        open = false;
        logged = false;
        try {
            receive.close();
            send.close();
            client.close();
        } catch (IOException ex) { System.out.println("Errore nella chiusura del client handler" + ex.getMessage());  }
    }
    
    public String receiveMessage(){
        try {
            return receive.readLine();
        } catch (IOException ex) { System.out.println("Errore nella ricezione, chiusura..." /*+ ex.getMessage()*/); close();}
        return null;
    }
    
    public void sendConnectedUsersList(){
        String list = "/list ";
        
        for (int i = 0; i < server.getClientList().size(); i++) {
            if(server.getClientList().get(i).isLogged()){
                list += server.getClientList().get(i).getName() + ",";
            }
        }
        
        System.out.println("Invio la lista: " + list);
        sendToClient(list);
    }
    
    public void notifyUserConnection(String username){
        sendToClient("/usr_con @" + username);
    }
    
    public void notifyUserDisconnection(String username){
        sendToClient("/usr_dsc @" + username);
    }
    
    public void sendToClient(String msg){
        try {
            send.writeBytes(msg + '\n');
        } catch (IOException ex) { System.out.println("Errore nel'invio:\n" + ex.getMessage()); }
    }
    
    public boolean isLogged(){
        return logged;
    }
    
    public void setOpen(boolean open){
        this.open = open;
    }
    
}