import java.io.InputStream;
import java.io.*;
import java.net.*;

public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    protected String clientSentence;
    protected String capitalizedSentence;
    protected boolean alive = true;
    protected String etapa = null;

    public WorkerRunnable(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
            BufferedReader inFromClient  = 
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
            long time = System.currentTimeMillis();
            this.etapa = "INICIO";
            outToClient.writeBytes("220 milaronix.com" + '\n' + "Etapa:" + this.etapa + '\n');
            

            while(alive){
                String cliente = null;
                clientSentence = inFromClient.readLine();
                System.out.println("Received: " + clientSentence);
                capitalizedSentence = clientSentence.toUpperCase();
                
                System.out.println("Request processed: " + time);
                System.out.println(capitalizedSentence + '\n');

                if(capitalizedSentence.equals("QUIT")){
                    outToClient.writeBytes("221 milaronix.com le desea feliz dia" + '\n');
                    this.alive = false;   
                }

                if(this.etapa.equals("DATA")) {
                    if(capitalizedSentence.indexOf(".") != -1){
                        outToClient.writeBytes("250 mensaje agregado a la cola" + '\n');
                        this.etapa = "FIN DATA";
                    }else{
                        outToClient.writeBytes("grabando data" + '\n');
                    }                                    
                }

                if(this.etapa.equals("RCPT TO")) {
                    if(capitalizedSentence.indexOf("DATA") != -1){
                        outToClient.writeBytes("Listo para recibir data" + '\n');
                        this.etapa = "DATA";
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                         
                }

                if(this.etapa.equals("MAIL FROM")) {
                    if(capitalizedSentence.indexOf("RCPT TO:") != -1){
                        cliente = clientSentence.substring(clientSentence.indexOf(": ")+2,clientSentence.length());
                        if(cliente.indexOf("@") != -1 && cliente.indexOf(".") != -1) {
                            outToClient.writeBytes("250 OK destinatario: " + cliente + '\n');
                            this.etapa = "RCPT TO";
                        }else{
                            outToClient.writeBytes("501 error de formato" + '\n');
                        }    
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                         
                }

                if(this.etapa.equals("HELO")) {
                    if(capitalizedSentence.indexOf("MAIL FROM:") != -1){
                        cliente = clientSentence.substring(clientSentence.indexOf(": ")+2,clientSentence.length());
                        if(cliente.indexOf("@") != -1 && cliente.indexOf(".") != -1) {
                            outToClient.writeBytes("250 OK remitente: " + cliente + '\n');
                            this.etapa = "MAIL FROM";
                        }else{
                            outToClient.writeBytes("501 error de formato" + '\n');
                        }
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                        
                }

                if(this.etapa.equals("INICIO")) {
                    if(capitalizedSentence.indexOf("HELO") != -1){
                        cliente = clientSentence.substring(clientSentence.indexOf(" ")+1,clientSentence.length());
                        outToClient.writeBytes("250 Hola " + cliente + " gusto en conocerte" + '\n');
                        this.etapa = "HELO";    
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                    
                }                
            }
            inFromClient.close();
            outToClient.close();
            
            
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }        
    }
}