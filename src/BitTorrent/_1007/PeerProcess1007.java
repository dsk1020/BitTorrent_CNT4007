package BitTorrent._1007;
import BitTorrent._1008.PeerProcess1008;

import java.io.*;
import java.net.*;

//Client() and Server() code from https://www.youtube.com/watch?v=6ClEzAYJ1MY&list=PLoW9ZoLJX39Xcdaa4Dn5WLREHblolbji4&index=4
public class PeerProcess1007 extends Thread
{
    private int method;

    public PeerProcess1007(int _method)
    {
        method = _method;
    }

    public void Client() throws IOException
    {
        System.out.println("Waiting");
        ServerSocket ss = new ServerSocket(49165);
        Socket sock = ss.accept();
        System.out.println("Connection established");
        while (true)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String str = in.readLine();
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            out.println("Server says: " + str);
        }
    }

    public void Server() throws IOException
    {
        System.out.println("Client started");
        Socket soc = new Socket("localhost", 49164);
        while (true)
        {
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String str = userInput.readLine();
        PrintWriter out = new PrintWriter(soc.getOutputStream(), true);
        out.println(str);
        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        System.out.println(in.readLine());
        }
    }

    public void run() {
        if (this.method == 1) {
            try {
                Client();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.method == 2) {
            try {
                Server();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        PeerProcess1007 p7Server = new PeerProcess1007(2);
        PeerProcess1007 p7Client = new PeerProcess1007(1);
        p7Client.start();
        sleep(5000);
        p7Server.start();

    }
}
