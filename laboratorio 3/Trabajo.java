import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;

public class Trabajo implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;

    public Trabajo(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String inputLine = in.readLine();
            String firstLine = inputLine;
            String method = inputLine.substring(0,inputLine.indexOf(" "));
            System.out.println(method);
            if (method.equals("HEAD")) {
                output.write((inputLine+"\n").getBytes());
                while (!(inputLine = in.readLine()).equals(""))
                    output.write((inputLine+"\n").getBytes());
            } else {
                if (method.equals("GET")) {
                    String page = firstLine.substring(firstLine.indexOf(" ")+2, firstLine.lastIndexOf(" "));
                    try {
                        BufferedReader in2 = new BufferedReader(new FileReader(page));
                        String str;
                        while ((str = in2.readLine()) != null)
                            output.write((str+"\n").getBytes());
                        in.close();
                    } catch (IOException e) {
                        System.out.println("Not Found");
                        output.write(("<h1>Not Found</h1>\n").getBytes());
                    }
                }
            }
            in.close();
            output.close();
            input.close();
            System.out.println("Request processed");
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}