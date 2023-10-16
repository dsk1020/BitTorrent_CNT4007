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
    public ObjectOutputStream outputStream = null;
    public ObjectInputStream inputStream = null;


    // Managing All Sockets and Streams
    public List<Socket> connectedFrom = new ArrayList<>();
    public List<Socket> connectedTo = new ArrayList<>();
    public HashMap<Socket, ObjectInputStream> inputStreams = new HashMap<>();
    public HashMap<Socket, ObjectOutputStream> outputStreams = new HashMap<>();
    public HashMap<Socket, Integer> connectedID = new HashMap<>();
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
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(handshakeMessage);

            //System.out.println("Sent handshake message from " + port);
            logMessage("TCP connection");
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void read() {
        while (true) {
            // TODO - Check if we are choked

            List<Socket> allConnections = new ArrayList<>(this.connectedFrom);
            allConnections.addAll(this.connectedTo);

            for(Socket socket:allConnections)
            {
                try{
                    socket.setSoTimeout(500); //TODO - set to unchoke time
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    String mss = (String)inputStream.readObject();
                    System.out.println(mss);
                    // TODO - Message response
                }catch (IOException i)
                {
                    //System.out.println(i);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void logMessage(String msgType) {
        try {
            Date getDate = new Date();
            String time = forTime.format(getDate);

            switch (msgType) {
                case "test":
                    log.println(time + ": This is a test message, id1:  " + port + " , id2: " + "[peer_ID 2]" + ".");
                    break;
                case "sends connection":
                    log.println(time + ": Peer " + port + " makes a connection to Peer " + "[peer_ID 2]" + ".");
                    break;
                case "connection made":
                    log.println(time + ": Peer " + port + " is connected from Peer " + "[peer_ID 2]" + ".");
                    break;
                case "change of preferred neighbors":
                    log.println(time + ": Peer " + port + " has the preferred neighbors " + "[preferred neighbor ID list].");
                    break;
                case "change of optimistically unchoked neighbor":
                    log.println(time + ": Peer " + port + " has the optimistically unchoked neighbor " + "[optimistically unchoked neighbor ID].");
                    break;
                case "unchoking":
                    log.println(time + ": Peer " + port + " is unchoked by " + " [peer_ID 2].");
                    break;
                case "choking":
                    log.println(time + ": Peer " + port + " is choked by " + " [peer_ID 2].");
                    break;
                case "receive HAVE":
                    log.println(time + ": Peer " + port + " received the 'have' message from " + "[peer_ID 2]" + " for the piece " + "[piece index].");
                    break;
                case "receive INTERESTED":
                    log.println(time + ": Peer " + port + " received the 'interested' message from " + "[peer_ID 2]");
                    break;
                case "receive NOT INTERESTED":
                    log.println(time + ": Peer " + port + " received the 'not interested' message from " + "[peer_ID 2]");
                    break;
                case "downloading a piece":
                    log.println(time + ": Peer " + port + " has downloaded the piece " + "[piece index]" + " from " + "[peer_ID 2]" + " . Now the number of pieces it has is " + "[number of pieces]" + " .");
                    break;
                case "completion of download":
                    log.println(time + ": Peer " + port + " has downloaded the complete file.");
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


}
