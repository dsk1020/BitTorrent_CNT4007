package BitTorrent;

import java.net.*;
import java.io.*;
import java.nio.file.*;
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
    public Set<Socket> neighbors; // Preferred neighbors
    public Socket optimisticNeighbor = null; // Optimistic neighbor
    public int hasFile; //boolean for checking if current peer process contains entire file (0 = false, 1 = true)
    public String bitfield;
    public HashMap<Integer, List<Integer>> filePieces;
    public ArrayList<String> peerInfo; //raw info for each peer, in case it's needed
    public ObjectOutputStream outputStream = null;
    public ObjectInputStream inputStream = null;


    // Managing All Sockets and Streams
    public List<Socket> connectedFrom = new ArrayList<>();
    public List<Socket> connectedTo = new ArrayList<>();
    public HashMap<Socket, ObjectInputStream> inputStreams = new HashMap<>();
    public HashMap<Socket, ObjectOutputStream> outputStreams = new HashMap<>();
    public HashMap<Socket, Integer> connectedID = new HashMap<>();
    public HashMap<Socket,String> peerBitfields = new HashMap<>();
    public List<Socket> interestedNeighbors = new ArrayList<>();
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
        filePieces = new HashMap<Integer, List<Integer>>();
        log = new PrintWriter("src/BitTorrent/log_peer_" + port + ".log");
        neighbors = new HashSet<>();
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
            logMessage("sends connection", connectedID.get(socket), 0);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void send(Socket socket, Message sndMsg)
    {
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            outputStream.writeObject(sndMsg);
            outputStream.flush();

        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void read() {
        while (true) {
            // Handle choke
            if (readThread.isInterrupted()) {
                // Send choking message to all neighbors
                for (Socket socket : neighbors) {
                    Message chokeMessage = new Message();
                    chokeMessage.setMsgType(MessageType.choke);
                    send(socket, chokeMessage);
                }
                if (isUnchoked) {
                    setNeighbors();
                    isUnchoked = false;
                }
                if (isOptimisticallyUnchoked) {
                    setOptimisticNeighbor();
                    isOptimisticallyUnchoked = false;
                }
                Thread.interrupted();
                continue;
            }

            List<Socket> allConnections = new ArrayList<>(this.connectedFrom);
            allConnections.addAll(this.connectedTo);

            for(Socket socket:allConnections)
            {
                try{
                    socket.setSoTimeout(1000); //TODO - set to unchoke time

                    inputStream = new ObjectInputStream(socket.getInputStream());
                    Object inMsg = inputStream.readObject();

                    if(inMsg instanceof Handshake)
                    {
                        Handshake msg = (Handshake) inMsg;
                        connectedID.put(socket,msg.getID());
                        System.out.println("-----Handshake: " + msg.getID());

                        //send bitfield message
                        Message bitfieldMsg = new Message(this.bitfield.length(), MessageType.bitfield, this.bitfield);
                        send(socket,bitfieldMsg);
                    }
                    // TODO - Message response
                    else if(inMsg instanceof Message)
                    {
                        Message msg = (Message) inMsg;

                        if(msg.getMsgType() == MessageType.choke)
                        {
                            // We already handle chokes in setNeighbors() and setOptimisticNeighbor() so we just need to log this
                            logMessage("choking", connectedID.get(socket), 0);
                        }
                        else if(msg.getMsgType() == MessageType.unchoke)
                        {
                            // We already handle unchokes in setNeighbors() and setOptimisticNeighbor() so we just need to log this
                            logMessage("unchoking", connectedID.get(socket), 0);
                        }
                        else if(msg.getMsgType() == MessageType.interested)
                        {
                            interestedNeighbors.add(socket); // save interested neighbors
                            logMessage("receive INTERESTED", connectedID.get(socket), 0);
                        }
                        else if(msg.getMsgType() == MessageType.not_interested)
                        {
                            int index = interestedNeighbors.indexOf(socket);
                            if(index != -1)
                            {
                                interestedNeighbors.remove(index); // remove neighbor from interested list
                            }
                            logMessage("receive NOT INTERESTED", connectedID.get(socket), 0);
                        }
                        else if(msg.getMsgType() == MessageType.have)
                        {
                            // TODO - Have
                            // get the index of the piece peer has
                            // u[date peers bitfield
                        }
                        else if(msg.getMsgType() == MessageType.bitfield)
                        {
                            System.out.println("---Bitfield: " + msg.getMsgBitfield());
                            this.peerBitfields.put(socket,msg.getMsgBitfield()); // store all peer bitfields

                            List<Integer> missingPieces = findMissingPieces(msg.getMsgBitfield());

                            if(missingPieces.isEmpty()) // if connected peer has no pieces this peer needs
                            {
                                Message outMsg = new Message(0, MessageType.not_interested);
                                send(socket, outMsg);
                            }
                            else
                            {
                                Message outMsg = new Message(0, MessageType.interested);
                                send(socket, outMsg);
                                // TODO - rest of bitfield
                            }


                        }
                        else if(msg.getMsgType() == MessageType.request)
                        {
                            int requestedPieceIndex = msg.getHaveIndex();
                            List<Integer> pieceContent = filePieces.get(requestedPieceIndex);
                            Message outMsg = new Message(4+pieceSize, MessageType.piece, requestedPieceIndex, pieceContent); //4 = size of pieceIndex field
                            send(socket, outMsg);
                            // TODO - rest of request
                        }
                        else if(msg.getMsgType() == MessageType.piece)
                        {
                            int pieceIndex = msg.getHaveIndex();
                            List<Integer> acquiredPiece = msg.getMsgPayload();
                            filePieces.put(pieceIndex, acquiredPiece);
                            updateBitfield(pieceIndex);
                            logMessage("downloading a piece", connectedID.get(socket), 0); //specify pieces downloaded
                            // Check if we have all the pieces
                            List<Integer> missingPieces = findMissingPieces(bitfield);
                            if (missingPieces.isEmpty()) {
                                // Export filePieces into actual file
                                hasFile = 1;
                                exportFilePieces();
                                logMessage("completion of download", 0, 0);
                            }
                            //Send out have messages?
                            //Message outMsg = new Message(4+pieceSize, MessageType.request, function to find interesting pieceIndex)
                            //send(socket, outMsg);
                            // TODO - rest of piece
                        }
                        else
                        {
                            throw new Exception("Message type not supported");
                        }
                    }
                }catch (IOException i)
                {
                    //System.out.println(i);
                } catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void setOptimisticNeighbor() {
        List<Socket> allConnections = new ArrayList<>(this.connectedFrom);
        allConnections.addAll(this.connectedTo);
        // If all connected sockets are neighbors, there aren't any choked sockets to optimistically choose.
        if (allConnections.size() <= interestedNeighbors.size()) {
            return;
        }

        // Get random index of socket which is not in the unchoked neighbors
        Random random = new Random();
        int randomIndex = random.nextInt(allConnections.size());
        if (interestedNeighbors.contains(allConnections.get(randomIndex))) {
            randomIndex = random.nextInt(allConnections.size());
        }
        optimisticNeighbor = allConnections.get(randomIndex);
        Message unchokeMessage = new Message();
        unchokeMessage.setMsgType(MessageType.unchoke);
        send(optimisticNeighbor, unchokeMessage);

        logMessage("change of optimistically unchoked neighbor", connectedID.get(optimisticNeighbor), 0);
    }

    private void setNeighbors() {
        neighbors = new HashSet<>();
        List<Socket> allConnections = new ArrayList<>(this.connectedFrom);
        allConnections.addAll(this.connectedTo);
        while (neighbors.size() < Math.min(allConnections.size(), numberOfPreferredNeighbors)) {
            Socket neighbor = allConnections.get(new Random().nextInt(allConnections.size()));
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        for (Socket socket : neighbors) {
            Message unchokeMessage = new Message();
            unchokeMessage.setMsgType(MessageType.unchoke);
            send(socket, unchokeMessage);
        }

        logMessage("change of preferred neighbors", 0, 0);
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

    public void logMessage(String msgType, int id2, int pieceIndex) {
        try {
            Date getDate = new Date();
            String time = forTime.format(getDate);

            switch (msgType) {
                case "test":
                    log.println(time + ": This is a test message, id1:  " + port + " , id2: " + id2 + ".");
                    break;
                case "sends connection":
                    log.println(time + ": Peer " + port + " makes a connection to Peer " + id2 + ".");
                    break;
                case "connection made":
                    log.println(time + ": Peer " + port + " is connected from Peer " + id2 + ".");
                    break;
                case "change of preferred neighbors":
                    if (neighbors.isEmpty()) return;
                    String idList = "";
                    for (Socket socket : neighbors) {
                        idList += connectedID.get(socket) + ",";
                    }
                    idList = idList.substring(0, idList.length() - 1);
                    log.println(time + ": Peer " + port + " has the preferred neighbors " + idList + ".");
                    break;
                case "change of optimistically unchoked neighbor":
                    log.println(time + ": Peer " + port + " has the optimistically unchoked neighbor " + id2 + ".");
                    break;
                case "unchoking":
                    log.println(time + ": Peer " + port + " is unchoked by " + id2);
                    break;
                case "choking":
                    log.println(time + ": Peer " + port + " is choked by " + id2);
                    break;
                case "receive HAVE":
                    log.println(time + ": Peer " + port + " received the 'have' message from " + id2 + " for the piece " + pieceIndex + " .");
                    break;
                case "receive INTERESTED":
                    log.println(time + ": Peer " + port + " received the 'interested' message from " + id2);
                    break;
                case "receive NOT INTERESTED":
                    log.println(time + ": Peer " + port + " received the 'not interested' message from " + id2);
                    break;
                case "downloading a piece":
                    log.println(time + ": Peer " + port + " has downloaded the piece " + pieceIndex + " from " + id2 + " . Now the number of pieces it has is " + filePieces.size() + " .");
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

    private void initializeFilePieces() {
        if (this.hasFile != 1) {
            return;
        }

        byte[] fileData;
        try {
            String path = "src/BitTorrent/_" + port + "/" + fileName;
            fileData = Files.readAllBytes(Path.of(path));
        }
        catch (IOException e) {
            System.out.println("Wrong file path");
            e.printStackTrace();
            return;
        }

        // Rounding up integer division
        int numPieces = (fileSize + pieceSize - 1) / pieceSize;
        int dataIndex = 0;
        for (int pieceIndex = 0; pieceIndex < numPieces; pieceIndex++) {
            List<Integer> pieceData = new ArrayList<>();

            for (int i = 0; i < pieceSize && dataIndex < fileSize; i++) {
                pieceData.add((int) fileData[dataIndex]);
                dataIndex++;
            }
            filePieces.put(pieceIndex, pieceData);
        }
        exportFilePieces();
    }

    private void exportFilePieces() {
        String path = "src/BitTorrent/_" + port + "/downloadedfile.jpg";
        try (FileOutputStream fos = new FileOutputStream(path)) {
            for (int pieceIndex = 0; pieceIndex < filePieces.size(); pieceIndex++) {
                List<Integer> pieceData = filePieces.get(pieceIndex);
                for (Integer byteValue : pieceData) {
                    fos.write(byteValue);
                }
            }
            fos.flush();
        } catch (IOException e) {
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
                //String repeat code: https://stackoverflow.com/questions/1235179/simple-way-to-repeat-a-string (String.repeat() was not working on my version of Java)
                this.bitfield = String.join("", Collections.nCopies((int)Math.ceil(fileSize / pieceSize), info[3])); //Technically each *bit* of the bitfield should specify the piece index, but in this case each byte (char) of the string = 1 bit.
                initializeFilePieces();
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

    public void updateBitfield(int index){
        if (index <= (fileSize / pieceSize)) {
            char[] bitfieldArray = this.bitfield.toCharArray();
            bitfieldArray[index] = '1';
            this.bitfield = String.valueOf(bitfieldArray);
        }
    }

    public List<Integer> findMissingPieces(String incomingBitfield) {
        List <Integer> missingPieces = new ArrayList<>();
        for (int i = 0; i < incomingBitfield.length(); i++) {
            if (incomingBitfield.charAt(i) == '1' && this.bitfield.charAt(i) == '0')
                missingPieces.add(i);
        }
        return missingPieces;
    }


}
