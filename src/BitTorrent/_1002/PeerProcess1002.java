package BitTorrent._1002;

import BitTorrent.PeerProcess;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class PeerProcess1002 {
    private static int numberOfPreferredNeighbors;
    private static int unchokingInterval;
    private static int optimisticUnchokingInterval;
    private static String fileName;
    private static int fileSize;
    private static int pieceSize;
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
    String MESSAGE;                //capitalized message read from the server

    public void Client() {}

    void run()
    {
        try{
            String handshakeMSG = "P2PFILESHARINGPROJ00000000001001";

            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            sendMessage(handshakeMSG);

            while(true)
            {
                //System.out.print("Hello, please input a sentence: ");
                //read a sentence from the standard input
                //message = bufferedReader.readLine();
                //Send the sentence to the server
                //sendMessage(message);
                //Receive the upperCase sentence from the server
                MESSAGE = (String)in.readObject();
                //show the message to the user

                //

                System.out.println("Receive message: " + MESSAGE);
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
    //send a message to the output stream
    void sendMessage(String msg)
    {
        try{
            //stream write the message
            out.writeObject(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    //main method
    public static void main(String args[]) throws Exception
    {
        // Read common.cfg file
        FileInputStream common = new FileInputStream("src/BitTorrent/Common.cfg");
        Properties properties = new Properties();
        properties.load(common);
        numberOfPreferredNeighbors = Integer.parseInt(properties.getProperty("NumberOfPreferredNeighbors"));
        unchokingInterval = Integer.parseInt(properties.getProperty("UnchokingInterval"));
        optimisticUnchokingInterval = Integer.parseInt(properties.getProperty("OptimisticUnchokingInterval"));
        fileName = properties.getProperty("FileName", null);
        fileSize = Integer.parseInt(properties.getProperty("FileSize"));
        pieceSize = Integer.parseInt(properties.getProperty("PieceSize"));
        PeerProcess peerProcess = new PeerProcess(1002, numberOfPreferredNeighbors, unchokingInterval, optimisticUnchokingInterval, fileName, fileSize, pieceSize);
        peerProcess.connect(1001);
        peerProcess.connect(1003);

        peerProcess.serverThread = new Thread(peerProcess::startServer);
        peerProcess.serverThread.start();
        //BitTorrent._1001.PeerProcess1001 client = new BitTorrent._1001.PeerProcess1001();
        //client.run();
    }

}