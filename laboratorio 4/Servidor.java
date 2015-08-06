import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.FileReader;


public class Servidor implements Runnable{

    protected int          serverPort   = 2407;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected int          maxThreads   = 5;

    public Servidor(int port,String configFile){
        this.serverPort = port;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(configFile));
            String str;
            while ((str = in2.readLine()) != null)
                if (str.indexOf("MaxThreads=") >= 0) {
                    this.maxThreads = Integer.parseInt(str.substring(str.indexOf("=")+1,str.length()));
                }
            in2.close();
        } catch (IOException e) {
            System.out.println("File Not Found");
        }
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        ExecutorService executor = Executors.newFixedThreadPool(this.maxThreads);
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            Runnable worker = new Trabajo(clientSocket, "Multithreaded Server");
            executor.execute(worker);
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 2407", e);
        }
    }


}