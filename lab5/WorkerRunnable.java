import java.io.InputStream;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.Driver;

public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    protected String clientSentence;
    protected String capitalizedSentence;
    protected boolean alive = true;
    protected String etapa = null;

    protected String url = "jdbc:mysql://localhost:3306/cc8";
    protected String username = "root";
    protected String password = "12345";
    protected Connection connection = null;

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
            outToClient.writeBytes("220 milaronix.com" + '\n');

            
            

            

            while(alive){
                String cliente = null;
                clientSentence = inFromClient.readLine();
                System.out.println("Received: " + clientSentence);
                capitalizedSentence = clientSentence.toUpperCase();
                
                System.out.println("Request processed: " + time);
                System.out.println(capitalizedSentence + '\n');

                if(capitalizedSentence.equals("QUIT") || capitalizedSentence == null){
                    outToClient.writeBytes("221 milaronix.com le desea feliz dia" + '\n');
                    this.alive = false;   
                }

                String date = null;
                String from = null;
                String to = null;
                String subject = null;

                if(this.etapa.equals("DATA")) {
                    if(capitalizedSentence.indexOf(".") != -1){
                        outToClient.writeBytes("250 mensaje agregado a la cola" + '\n');
                        this.etapa = "FIN DATA";
                    }else{
                        if(capitalizedSentence.indexOf("DATE:") != -1){
                            date = capitalizedSentence.substring(capitalizedSentence.indexOf("DATE:")+1,capitalizedSentence.length()-capitalizedSentence.indexOf("DATE:"));
                            System.out.println("esto es date: "+date);
                        }
                        outToClient.writeBytes("grabando data" + '\n');
                    }                                    
                }

                if(this.etapa.equals("RCPT TO")) {
                    if(capitalizedSentence.indexOf("DATA") != -1){
                        //outToClient.writeBytes("Listo para recibir data" + '\n');
                        this.etapa = "DATA";
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                         
                }

                boolean esmio = false;
                if(this.etapa.equals("MAIL FROM")) {
                    if(capitalizedSentence.indexOf("RCPT TO:") != -1){
                        cliente = clientSentence.substring(clientSentence.indexOf(": ")+2,clientSentence.length());
                        if(cliente.indexOf("@") != -1 && cliente.indexOf(".") != -1) {
                            outToClient.writeBytes("250 OK destinatario: " + cliente + '\n');
                            this.etapa = "RCPT TO";

                            String dominio = cliente.substring(cliente.indexOf("@")+1,cliente.length());
                            System.out.println("dominio: "+ dominio);
                            if(dominio.equals("milaronix.com")){
                                System.out.println("Si es mio");
                                esmio = true;
                                try{
                                    conecta();
                                    Statement stmt=this.connection.createStatement(); 
                                    String query = "select * from usuarios";
                                    ResultSet rs=stmt.executeQuery(query); 
                                    this.connection.close(); 
                                }catch(Exception e){ System.out.println(e);}
                            }                            
                        }else{
                            outToClient.writeBytes("501 error de formato" + '\n');
                        }    
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                         
                }

                if(this.etapa.equals("HELO")) {
                    if(capitalizedSentence.indexOf("MAIL FROM:") != -1){
                        cliente = clientSentence.substring(clientSentence.indexOf("<")+1,clientSentence.length()-1);
                        if(cliente.indexOf("@") != -1 && cliente.indexOf(".") != -1) {
                            boolean existemail = false;
                            try{
                                conecta();
                                Statement stmt=this.connection.createStatement(); 
                                ResultSet rs=stmt.executeQuery("select * from usuarios");                                
                                while(rs.next()){                                
                                    System.out.println("resultado: "+rs.getString(2)+" cliente: "+ cliente);
                                    if(rs.getString(2).equals(cliente)){
                                        existemail = true;
                                    }
                                }  
                                this.connection.close(); 
                            }catch(Exception e){ System.out.println(e);}
                            if(existemail){
                                outToClient.writeBytes("250 OK destinatario: " + cliente + '\n');
                                this.etapa = "MAIL FROM";    
                            }else{
                                outToClient.writeBytes("501 usuario: " + cliente + " no existe." +'\n');
                            }
                        }else{
                            outToClient.writeBytes("501 error de formato" + '\n');
                        }
                    }else{
                        outToClient.writeBytes("503 error en secuencia de comandos" + '\n');
                    }                                        
                }

                if(this.etapa.equals("INICIO")) {
                    if(capitalizedSentence.indexOf("HELO") != -1 || capitalizedSentence.indexOf("EHLO") != -1){
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

    public void conecta (){
        try{
            Class.forName("com.mysql.jdbc.Driver");  
            this.connection=DriverManager.getConnection(url,username,password);
        }catch(Exception e){ System.out.println(e);}

    }
}