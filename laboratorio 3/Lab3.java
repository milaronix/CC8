import java.io.IOException;

public class Lab3 {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Uso: java Lab3 <config.txt>");
            System.exit(1);
        }
        
        String configFile = args[0];
        
        Servidor server = new Servidor(2407, configFile);
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
