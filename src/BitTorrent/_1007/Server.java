package BitTorrent._1007;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Server {

	private static final int sPort = 8000;   //The server will be listening on this port number
	private static int numberOfPreferredNeighbors;
	private static int unchokingInterval;
	private static int optimisticUnchokingInterval;
	private static String fileName;
	private static int fileSize;
	private static int pieceSize;

	public static void main(String[] args) throws Exception {
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

		System.out.println("The server is running.");
		ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
		Handler[] handArr = new Handler[2];
        	try {
				while (true) {
					handArr[clientNum - 1] = new Handler(listener.accept(), clientNum);
					handArr[clientNum - 1].start();
					System.out.println("Client " + clientNum + " is connected!");
					clientNum++;
					//max clients + 1
					while (clientNum == 3) {
						try {
							for (Handler x : handArr) {
								try {
									// TODO: Change from checking string values to the instance of some object = message type
									if (x.message.substring(0,28).equals("P@PFILESHARINGPROJ0000000000")) {
										handArr[Integer.parseInt(x.message.substring(31,32))-1].sendMessage(x.message);
										x.message = "0";
										// TODO: Change some array that keeps status to indicate that the two are now connected
									}
								}
								catch (StringIndexOutOfBoundsException e)
								{

								}
							}
						}
						catch (NullPointerException e) {
						}
									}
				}
			}
				finally {
            		listener.close();
        	}
	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        	public String message;    //message received from the client
		public String MESSAGE;    //uppercase message send to the client
		public Socket connection;
        	public ObjectInputStream in;	//stream read from the socket
        	public ObjectOutputStream out;    //stream write to the socket
		public int no;		//The index number of the client

        	public Handler(Socket connection, int no) {
            		this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
//					//Capitalize all letters in the message
//					MESSAGE = message.toUpperCase();
//					//send MESSAGE back to the client
//					sendMessage(MESSAGE);
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg + " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }

}
