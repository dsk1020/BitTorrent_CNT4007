package BitTorrent._1001;

import  java.net.*;
import java.io.*;

public class PeerProcess1001
{

    public static void main(String[] args)
    {
        try
        {
            ServerSocket servSocket = new ServerSocket(51234);
            Socket _socket = servSocket.accept();
            DataInputStream dis=new DataInputStream(_socket.getInputStream());
            DataOutputStream dout=new DataOutputStream(_socket.getOutputStream());
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));

            String msg = "";
            String inMsg = "";
            while(true)
            {
                msg = reader.readLine();
                inMsg=dis.readUTF();

                dout.writeUTF(msg);
                System.out.println("\tIncoming= "+inMsg);

                dout.flush();

                if(inMsg.equals("null") || msg.equals("null"))
                {
                    break;
                }
            }
            dout.close();
            servSocket.close();

        }
        catch (IOException e)
        {
            System.out.println("Error: " + e);
        }
    }
}
