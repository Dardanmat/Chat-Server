package it.fi.itismeucci;
import java.io.IOException;


public class ServerMain {
    public static void main(String[] args) {
        
        ServerChat server = new ServerChat(5000);
        try {
            server.start();
        } catch (IOException ex) {System.out.println("Errore nel server main\n" + ex.getMessage()); }
        
    }
}
