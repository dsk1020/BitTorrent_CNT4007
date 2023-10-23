package BitTorrent;

import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class PeerProcess {
    public Thread serverThread, readThread, unchokingThread, optimisticUnchokingThread;
    // Config parameters
    public static int port;
    public static int numberOfPreferredNeighbors;
    public static int unchokingInterval;
    public static int optimisticUnchokingInterval;
    public static String fileName;
    public static int fileSize;
    public static int pieceSize;
    public boolean isUnchoked, isOptimisticallyUnchoked = false; // Flag for choking/unchoking intervals
    public ArrayList<Socket> neighbors;
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
            //String handshakeMessage = "P2PFILESHARINGPROJ0000000000" + String.valueOf(port);
            Handshake msg = new Handshake(port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(msg);

            //System.out.println("Sent handshake message from " + port);
            logMessage("TCP connection");
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void send(Socket socket, Message sndMsg)
    {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(sndMsg);

            //TODO - add Log Message
        } catch(Exception e)
        {
            //e.printStackTrace();
        }
    }

    public void read() {
        while (true) {
            // Handle choke
            if (readThread.isInterrupted()) {
                // Send choking message to all neighbors
                for (Socket socket : neighbors) {
                    Message message = new Message();
                    message.setMsgType(MessageType.choke);
                    // TODO: Send choke message, i.e. send(socket, message)
                }
                if (isUnchoked) {
                    // TODO: changeNeighbors();
                }
                if (isOptimisticallyUnchoked) {
                    // TODO: changeOptimisticNeighbors();
                }
                // TODO: Handle interrupt message/ Do choking stuff
            }

            List<Socket> allConnections = new ArrayList<>(this.connectedFrom);
            allConnections.addAll(this.connectedTo);

            for(Socket socket:allConnections)
            {
                try{
                    socket.setSoTimeout(500); //TODO - set to unchoke time

                    inputStream = new ObjectInputStream(socket.getInputStream());
                    Object inMsg = inputStream.readObject();

                    if(inMsg instanceof Handshake)
                    {
                        Handshake msg = (Handshake) inMsg;
                        connectedID.put(socket,msg.getID());
                        //TODO - Send Bitfield Message
                        //Message bitfieldMsg = new Message(length, type, bitfield)
                        //send(socket,
                    }
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

    public void unchokingInterval() {
        while (true) {
            try {
                // Sleep for unchoking interval seconds
                Thread.sleep(unchokingInterval * 1000L);
                System.out.println("Unchoking interval is " + unchokingInterval + " seconds.");
                System.out.println("Unchoke alert sent from peer process " + port);
                isUnchoked = true;
                readThread.interrupt();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void optimisticUnchokingInterval() {
        while (true) {
            try {
                // Sleep for optimistic unchoking interval seconds
                Thread.sleep(optimisticUnchokingInterval * 1000L);
                System.out.println("Optimistc unchoking interval is " + optimisticUnchokingInterval + " seconds.");
                System.out.println("Optimistic unchoke alert sent from peer process " + port);
                isOptimisticallyUnchoked = true;
                readThread.interrupt();
            }
            catch (Exception e) {
                e.printStackTrace();
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
        BufferedReader reader = new BufferedReader(new FileReader("src/BitTorrent/PeerInfo.cfg")); //My intellij is weird about relative file paths so this may need to be changed
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
            if (Integer.parseInt(info[2]) == this.port) {
                this.hasFile = Integer.parseInt(info[3]);
            }
        }
    }

    public static int findPortFromPeerInfo(String id) throws IOException { //This function is a bit redundant if process id = port number, but necessary in current implementation to have the port of a process be grabbed from the PeerInfo.cfg file
        BufferedReader reader = new BufferedReader(new FileReader("src/BitTorrent/PeerInfo.cfg"));
        int foundPort = -1; //Will return -1 if process id not in PeerInfo.cfg
        while (true) {
            String process = reader.readLine();
            if (process != null) {
                if (process.contains(id)) {
                    String[] info = process.split(" ");
                    foundPort = Integer.parseInt(info[2]);
                    return foundPort;
                }
            }
            else
                break;
        }
        return foundPort;
    }

    public void connectToAllBefore(){
        for (String peer: this.peerInfo) {
            String[] info = peer.split(" ");
            if (Integer.parseInt(info[2]) < this.port) {
                this.connect(Integer.parseInt(info[2]));
            }
        }
    }


}
