package BitTorrent._1001;

import BitTorrent.PeerProcess;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class PeerProcess1001 {
    private static int numberOfPreferredNeighbors;
    private static int unchokingInterval;
    private static int optimisticUnchokingInterval;
    private static String fileName;
    private static int fileSize;
    private static int pieceSize;

    //main method
    public static void main(String args[]) throws Exception
    {
        String processId = args[0];
        int port = PeerProcess.findPortFromPeerInfo(processId);
        // Read common.cfg file
        FileInputStream common = new FileInputStream("BitTorrent_CNT4007/src/BitTorrent/Common.cfg");
        Properties properties = new Properties();
        properties.load(common);
        numberOfPreferredNeighbors = Integer.parseInt(properties.getProperty("NumberOfPreferredNeighbors"));
        unchokingInterval = Integer.parseInt(properties.getProperty("UnchokingInterval"));
        optimisticUnchokingInterval = Integer.parseInt(properties.getProperty("OptimisticUnchokingInterval"));
        fileName = properties.getProperty("FileName", null);
        fileSize = Integer.parseInt(properties.getProperty("FileSize"));
        pieceSize = Integer.parseInt(properties.getProperty("PieceSize"));
        PeerProcess peerProcess = new PeerProcess(port, numberOfPreferredNeighbors, unchokingInterval, optimisticUnchokingInterval, fileName, fileSize, pieceSize);
        peerProcess.parsePeerInfo();
        peerProcess.connectToAllBefore();

        peerProcess.serverThread = new Thread(peerProcess::startServer);
        peerProcess.readThread = new Thread(peerProcess::read);

        peerProcess.serverThread.start();
        peerProcess.readThread.start();
    }

}