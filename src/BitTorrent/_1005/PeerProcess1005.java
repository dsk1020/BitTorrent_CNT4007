package BitTorrent._1005;
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
public class PeerProcess1005
{
    public static void main(String[] args) throws IOException {
        // Create server Socket
        ServerSocket ss = new ServerSocket(5056);
//
//        // connect it to client socket
//        Socket s = ss.accept();
//        System.out.println("Connection established");
//
//        // to send data to the client
//        PrintStream ps = new PrintStream(s.getOutputStream());
//
//        // to read data coming from the client
//        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//        // to read data from the keyboard
//        BufferedReader kb = new BufferedReader(new InputStreamReader(System.in));

//        // server executes continuously
//        while (true) {
//            String str, str1;
//
//            // repeat as long as the client does not send a null string
//            // read from client
//            while ((str = br.readLine()) != null) {
//                System.out.println(str);
//                str1 = kb.readLine();
//
//                // send to client
//                ps.println(str1);
//            }
//
//            // close connection
//            ps.close();
//            br.close();
//            kb.close();
//            ss.close();
//            s.close();
//
//            System.exit(0);
//        }
        while (true)
        {
            Socket s = null;

            try
            {
                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos);

                // Invoking the start() method
                t.start();

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}

// ClientHandler class
class ClientHandler extends Thread
{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        String received;
        String date;
        String time;

        while (true)
        {
            try {
                dos.writeUTF("Type Exit to terminate connection. Otherwise, type your message.");
                Date getDate = new Date();
                date = fordate.format(getDate);
                time = fortime.format(getDate);

                // Write out message
                received = dis.readUTF();
                System.out.println(date + " at " + time + ", Client: " + received);
                if(received.equals("Exit"))
                {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}