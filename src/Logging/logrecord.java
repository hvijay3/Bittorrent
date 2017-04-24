package Logging;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.*;
import java.util.logging.*;
import java.util.logging.Formatter;


/**
 * Created by Harshit Vijayvargia on 3/28/2017.
 */
public class logrecord {

    public static Logger peerLogger;
    public static logrecord logRecord = new logrecord();
    static String LOGTITLE;
static
{
System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %5$s%6$s%n");
}

    public logrecord() {
        Logger bitTorrent = Logger.getLogger("CNT5106C");
        peerLogger = bitTorrent;
    }

    public static logrecord getLogRecord()
    {
        return logRecord;
    }
    public static void setLoggerForPeer(int peerId)
    {
        LOGTITLE = ": Peer" + " " + Integer.toString(peerId);
                String filename = "log_peer_" + Integer.toString(peerId) + ".log";


        try {
            Handler loggerHandler = new FileHandler(filename);

            Formatter formatter = (Formatter) Class.forName("java.util.logging.SimpleFormatter").newInstance();

            loggerHandler.setFormatter(formatter);
            loggerHandler.setLevel(Level.parse("INFO"));
            peerLogger.addHandler(loggerHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void closeLogger()
    {
        for(Handler h : peerLogger.getHandlers())
        {
            h.close();
        }
    }


// Logging for connection between peers
    public void MakesConnection (int peerId) {
        String msg = LOGTITLE + " makes a connection to "+ Integer.toString(peerId) ;

        peerLogger.log (Level.INFO,msg);
    }
// Logging for geting connected by peerId
    public void getingConnected(int peerId)
    {
        String msg = LOGTITLE + " is connected from Peer " + Integer.toString(peerId) ;
        peerLogger.log (Level.INFO,msg);
    }
    // Logging for geting choked

    public void choked (int peerId) {
         String msg = LOGTITLE + " is choked by " + Integer.toString(peerId);
        peerLogger.log (Level.INFO,msg);
    }


    public void changeOfPrefereedNeighbors (HashSet<Integer> preferredNeighbours) {

        String neighbours = converttoString(preferredNeighbours);
         String msg = LOGTITLE + " has preferred neighbors "+ neighbours;
        peerLogger.log (Level.INFO,msg);
    }

    private String converttoString(HashSet<Integer> preferredNeighbours) {

        StringBuilder s =new StringBuilder();
        int i1=0;

        for(Integer i : preferredNeighbours)
        {
            s.append(Integer.toString(i));
            i++;

            if(i!=preferredNeighbours.size()) {
                s.append(",");
            }
            else{
                break;}
        }
        return s.toString();
    }

    public void changeOfOptimisticallyUnchokedNeighbors (int unchokeNeighbours) {
        //String neighbours = converttoString(unchokeNeighbors);
        final String msg = LOGTITLE + " has the optimistically unchoked neighbor " + Integer.toString(unchokeNeighbours);
        peerLogger.log(Level.INFO, msg);
    }



    public void unchoked (int peerId) {
        String msg = LOGTITLE + " is unchoked by " + Integer.toString(peerId);
        peerLogger.log(Level.INFO,msg);
    }

    public void receivedHave (int peerId, int pieceIdx) {
        String msg = LOGTITLE + " received the 'have' message from " + Integer.toString(peerId)+ "for the piece " + Integer.toString(pieceIdx);
        peerLogger.log (Level.INFO,msg);
    }

    public void receivedInterested (int peerId) {
        String msg = LOGTITLE + " received the 'interested' message from "+ Integer.toString(peerId);
        peerLogger.log (Level.INFO,msg);
    }

    public void receivedNotInterested (int peerId) {
        final String msg = LOGTITLE + " received the 'not interested' message from "+Integer.toString(peerId);
        peerLogger.log (Level.INFO,msg);
    }

    public void pieceDownloaded (int peerId, int pieceIdx, int currNumberOfPieces) {
        final String msg = LOGTITLE + " has downloaded the piece " + Integer.toString(pieceIdx) +" from peer "+ Integer.toString(peerId) +" Now the number of pieces it has is " + Integer.toString(currNumberOfPieces);
        peerLogger.log(Level.INFO,msg);
    }

    public void fileComplete () {
        final String msg = LOGTITLE + " has downloaded the complete file.";
        peerLogger.log (Level.INFO,msg);
    }


}
