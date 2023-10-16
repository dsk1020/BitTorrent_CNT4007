package BitTorrent;
import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class PeerProcess {
    public Thread serverThread, readThread, t3, t4;
    // Config parameters
    public static int port;
    public static int numberOfPreferredNeighbors;
    public static int unchokingInterval;
    public static int optimisticUnchokingInterval;
    public static String fileName;
    public static int fileSize;
    public static int pieceSize;
    public int hasFile; //boolean for checking if current peer process contains entire file (0 = false, 1 = true)
    public ArrayList<String> peerInfo; //raw info for each peer, in case it's needed


    // Managing All Sockets and Streams
    public List<Socket> connectedFrom = new ArrayList<>();
    public List<Socket> connectedTo = new ArrayList<>();
    public HashMap<Socket, ObjectInputStream> inputStreams = new HashMap<>();
    public HashMap<Socket, ObjectOutputStream> outputStreams = new HashMap<>();
    public ServerSocket serverSocket = null;

    // Logging functionality
    private final DateFormat forTime = new SimpleDateFormat("hh:mm:ss");
    private final PrintWriter log;

    // Constructor
    public PeerProcess(int port, int numberOfPreferredNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String fileName, int fileSize, int pieceSize) throws Exception {
        PeerProcess.port = port;
        serverSocket = new ServerSocket(port);
        PeerProcess.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        PeerProcess.unchokingInterval = unchokingInterval;
        PeerProcess.optimisticUnchokingInterval = optimisticUnchokingInterval;
        PeerProcess.fileName = fileName;
        PeerProcess.fileSize = fileSize;
        PeerProcess.pieceSize = pieceSize;
        log = new PrintWriter("src/BitTorrent/log_peer_" + port + ".log");
    }

    public void connect (int port) {
        try {
            System.out.println("Peer " + PeerProcess.port + " made connection to port " + port);
            Socket socket = new Socket("localhost", port);
            connectedTo.add(socket);
            outputStreams.put(socket, new ObjectOutputStream(socket.getOutputStream()));
            inputStreams.put(socket, new ObjectInputStream(socket.getInputStream()));

            sendHandshake(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            while (true) {
                System.out.println("Listening from... " + port);
                Socket socket = serverSocket.accept();
                inputStreams.put(socket, new ObjectInputStream(socket.getInputStream()));
                outputStreams.put(socket, new ObjectOutputStream(socket.getOutputStream()));
                connectedFrom.add(socket);
                sendHandshake(socket);
            }
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void sendHandshake(Socket socket) {
        try {
            String handshakeMessage = "P2PFILESHARINGPROJ0000000000" + String.valueOf(port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(handshakeMessage);

            System.out.println("Sent handshake message from " + port);
            logMessage("TCP connection");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read() {
        while (true) {
            // Check if we are choked
            if (readThread.isInterrupted()) {
                // Handle choking
            }
            // Create list of all sockets
            // for each socket...
                // try catch block
                // Set timeout for 1 second
                // Set up input streams
                // Read message
                // Switch statement for each message type
        }
    }

    public void logMessage(String msgType) {
        try {
            Date getDate = new Date();
            String time = forTime.format(getDate);

            switch (msgType) {
                case "test":
                    log.println(time + ": This is a test message, id1:  " + port + " , id2: " + "INSERT ID FROM connectdID MAP" + ".");
                    break;
                case "TCP connection":
                    log.println(time + ": Peer " + port + " makes a connection to Peer " + "INSERT ID FROM connectdID MAP" + ".");
                    break;
                case "change of preferred neighbors":
                    log.println(time + ": Change of preferred neighbors log, etc...");
                    break;
                default:
                    log.println("Invalid log type");
            }
            log.flush();
        } catch (Error e) {
            e.printStackTrace();
        }
    }


    public void parsePeerInfo() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("BitTorrent_CNT4007/src/BitTorrent/peerInfo.cfg")); //My intellij is weird about relative file paths so this may need to be changed
        ArrayList<String> peers = new ArrayList<String>();
        while (true) {
            String peer = reader.readLine();
            if (peer != null) {
                peers.add(peer);
            }
            else
                break;
        }
        this.peerInfo = peers;
        for (String peer : peers) {
            String[] info = peer.split(" ");
            if (Integer.parseInt(info[2]) != this.port) {
                this.connect(Integer.parseInt(info[2]));
            }
            else {
                this.hasFile = Integer.parseInt(info[3]);
            }
        }
    }

    public static ArrayList<Handler> processList = new ArrayList<>();
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private String message;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private final Socket connection;
        private ObjectInputStream in;    //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private PrintWriter log; //stream write to log
        private final int no;        //The index number of the client
        private final DateFormat forTime = new SimpleDateFormat("hh:mm:ss");

        private final int pos; // stores position in array

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
            pos = processList.size();
            processList.add(this);
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                log = new PrintWriter("src/BitTorrent/log_peer_100" + no + ".log");

                System.out.println("CLIENT AMOUNT =" + processList.size());

                try {
                    while (true) {
                        //receive the message sent from the client
                        message = (String) in.readObject();
                        //show the message to the user

                        System.out.println("Receive message: " + message + " from client " + no);
                        //Capitalize all letters in the message
                        MESSAGE = message.toUpperCase();
                        //message = (String)in.readObject();

                        //send MESSAGE back to the client
                        for(int i = processList.size(); i > 0; i--)
                        {
                            if(i == (no))
                            {
                                continue;
                            }
                            sendMessage(MESSAGE, i - 1);
                        }

                        // Every time a messsage is received from a peer,
                        // a test log message is written to the corresponding peer log file
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
        public void sendMessage(String msg, int target) {
            try {
                processList.get(target).out.writeObject(msg);
                processList.get(target).out.flush();
                System.out.println("Send message: " + msg + " to Client " + target);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void logMessage(String msgType, int id1, int id2) {
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
