import java.io.IOException;

public class Lab2 {
    public static void main(String[] args) throws IOException {
        
        Servidor server = new Servidor(2407);
        new Thread(server).start();

        try {
            Thread.sleep(10000 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop(); 
    }
}
