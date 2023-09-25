package BitTorrent;
import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

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
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
		private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
		private ObjectInputStream in;    //stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private PrintWriter log; //stream write to log
		private int no;        //The index number of the client
		private DateFormat forTime = new SimpleDateFormat("hh:mm:ss");

		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		public void run() {
			try {
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				log = new PrintWriter("src/BitTorrent/log_peer_100" + no);
				try {
					while (true) {
						//receive the message sent from the client
						message = (String) in.readObject();
						//show the message to the user
						System.out.println("Receive message: " + message + " from client " + no);
						//Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						//send MESSAGE back to the client
						sendMessage(MESSAGE);
						logMessage( "test", 1001, 1002);
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + no);
			} finally {
				//Close connections
				try {
					in.close();
					out.close();
					log.close();
					connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg + " to Client " + no);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		public void logMessage(String msgType, int id1, int id2) throws FileNotFoundException {
			try {
				Date getDate = new Date();
				String time = forTime.format(getDate);

				switch (msgType) {
					case "test":
						log.println(time + ": This is a test message, id1:  " + id1 + " , id2: " + id2 + ".");
						break;
					case "TCP connection":
						log.println(time + ": Peer " + id1 + " makes a connection to Peer " + id2 + ".");
						break;
					case "change of preferred neighbors":
						log.println(time + ": Change of preferred neighbors log, etc...");
					default:
						log.println("Invalid log type");
				}
			} catch (Error e) {
				e.printStackTrace();
			}
		}

	}

}
