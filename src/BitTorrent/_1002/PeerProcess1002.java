package BitTorrent._1002;
import java.io.*;
import  java.net.*;
public class PeerProcess1002
{
    public static void main(String[] args)
    {
        try
        {
            Socket socket = new Socket("localHost", 51234);
            DataInputStream dis=new DataInputStream(socket.getInputStream());
            DataOutputStream dout=new DataOutputStream(socket.getOutputStream());
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));

            String msg = "";
            String inMsg = "";
            while(true)
            {
                msg = reader.readLine();
                dout.writeUTF(msg);
                dout.flush();

                inMsg=dis.readUTF();
                System.out.println("\tIncoming= "+inMsg);

                if(inMsg.equals("null") || msg.equals("null"))
                {
                    break;
                }
            }

            dout.close();
            socket.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
