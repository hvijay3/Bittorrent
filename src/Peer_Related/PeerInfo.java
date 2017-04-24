package Peer_Related;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

/**
 * Created by Harshit Vijayvargia on 3/28/2017.
 */
public class PeerInfo {
    public final String peerId;
    public final String hostName;
    public final String listeningPort;
    public final boolean hasFile;
    public AtomicInteger _bytesDownloadedFrom;   //from this peer how many bytes have been downloaded
    public BitSet _receivedParts;                 // how many parts this peer has received
    public AtomicBoolean _interested ;      // is the the interested peer

    public PeerInfo(String pID, String hName, String lPort, String hFile)
    {
        peerId = pID;
        hostName = hName;
        listeningPort = lPort;
        hasFile = hFile.equals("1")?true:false;
        //To understand
        _bytesDownloadedFrom = new AtomicInteger (0);
        _receivedParts = new BitSet();
        _interested = new AtomicBoolean (false);

    }



    // A method to return peerId if i send it a list of peers
    static HashSet<Integer> getPeerIds(LinkedList<PeerInfo> peers)
    {
        HashSet<Integer> peerIds = new HashSet<Integer>();
     if(peers!=null && !peers.isEmpty()){

        for(PeerInfo p : peers)
        {
            peerIds.add(p.getPeerId());
        }


    }
        return peerIds;}


    public static PeerInfo getPeerByPeerId(int peerId, LinkedList<PeerInfo> list)
    {

       for(PeerInfo p : list)
       {
           if(p.getPeerId()==peerId)
           {return p;}
       }
       return null;
    }
    public int getPeerId() {
        return Integer.parseInt(peerId);
    }

    public int getPort() {
        return Integer.parseInt(listeningPort);
    }

    public String getPeerAddress() {
        return listeningPort;
    }

    public boolean hasFile() {
        return hasFile;
    }


    //to Understand
    public boolean isInterested() {
        return _interested.get();
    }

    public void setInterested() {
        _interested.set (true);
    }

    public void setNotIterested() {
        _interested.set (false);
    }

}
