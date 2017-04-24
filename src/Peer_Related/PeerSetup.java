package Peer_Related;


import All_Messages.Choke;
import All_Messages.Unchoke;
import All_Messages.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import Logging.logrecord;
import Peer_Related.*;
/**
 * Created by Harshit Vijayvargia on 3/31/2017.
 */
public class PeerSetup implements Runnable {
    static FileManager fileManager;
    static PeerManager peerManager;
    static PeerInfo peer;
    Vector<Handler> connectionsList = new Vector<>();
    static LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>();

    public  AtomicBoolean isFileCompleted = new AtomicBoolean(false);
    public AtomicBoolean areNeighboursCompleted = new AtomicBoolean(false);
    public AtomicBoolean EndPeerProcess = new AtomicBoolean(false);

    // to Store Connection handlers for a peer and make it thread safe.
    //  Set<Handler> handlerSet = Collections.newSetFromMap(new ConcurrentHashMap<Handler,Boolean>());

    static Properties common_cfg = new Properties();

    PeerSetup(Properties common_cfg, LinkedList<PeerInfo> peers, PeerInfo peer) throws Exception {
        this.peers = peers;
        this.common_cfg = common_cfg;
        this.peer = peer;

        fileManager = new FileManager(this.peer,common_cfg,this);

        //making peers to connect to list
        LinkedList<PeerInfo> peersExceptLocal = new LinkedList<>(removePeer(peers, peer.getPeerId()));
        peerManager = new PeerManager(peersExceptLocal, this);
    }

    void startingThreadsandMethods()
    {
        Thread t = new Thread(peerManager);
        t.setName("PeerManager Thread");
        t.start();
    }

// this thread for making peer as a server


    public void run() {
        try
        {
            ServerSocket serversok = new ServerSocket(Integer.parseInt(peer.listeningPort));
            while(!EndPeerProcess.get()){
                    //System.out.println("here2");
                    Socket s = serversok.accept();
                    //  if(s==null)
                    // continue;
                    // else

                    //System.out.println("here2");
                    Handler conn = new Handler(s,Integer.parseInt(peer.peerId),-1,fileManager,peerManager);
                    //System.out.println("here" + conn.peerId);
                    addConnection(conn);


            }
        }
        catch(Exception e)
        {
           // System.exit(0);
            e.printStackTrace();
            System.out.println(peer.getPeerId());
        }

            }

            public static void closeSockets(Vector<Handler> connectionsList)
            {
                for(Handler h : connectionsList)
                {
                    try {
                        h.sok.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


    public void connectToOtherPeers(){
        Queue<PeerInfo> _listOfPeers = getConnectList(peers,peer.getPeerId());
        while(_listOfPeers!=null && !_listOfPeers.isEmpty())
        {
            Socket sok = null;
            PeerInfo r = _listOfPeers.poll();
            try {
                sok = new Socket(r.hostName, r.getPort());
                Handler conn = new Handler(sok,peer.getPeerId(),r.getPeerId(),fileManager,peerManager);
                  addConnection(conn);
               // Thread.sleep(10);


                }
            catch(Exception e) {
                e.printStackTrace();
            }

            }

        }


    public synchronized void addConnection(Handler conn) {
        if (!connectionsList.contains(conn)) {
            connectionsList.add(conn);
            new Thread(conn).start();
           try {
                wait(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }






// this method removes the peer from the peer list and sends the new peer list

    LinkedList<PeerInfo> removePeer(LinkedList<PeerInfo> peers, int peerId)
    {
        LinkedList<PeerInfo> newPeers = new LinkedList<PeerInfo>();

        for(PeerInfo p : peers)
        {
            if (peerId == p.getPeerId()) { continue;}
            else { newPeers.add(p);}
        }
        return newPeers;
    }

    Queue<PeerInfo> getConnectList(LinkedList<PeerInfo> peers, int peerId)
    {
        Queue<PeerInfo> newPeers = new LinkedList<PeerInfo>();

        for(PeerInfo p : peers)
        {
            if (peerId == p.getPeerId()) {break;}
            else { newPeers.add(p);}
        }
        return newPeers;
    }


    public static PeerInfo getPeer()
    {
        return peer;
    }

    /*
     * A function that takes in a collection of PeerIDs and uses connectionhandler vector to unchoke certain neigbors
     * @name    unchokePeers
     * @author  Kunal Bajaj
     * @params  Collection of peerIDs that need to be unchoked
     * @returns nothing
    */
    public synchronized void unchokePeers(Collection<Integer> peerIDsToUnchoke)
    {
        if(peerIDsToUnchoke!=null && !peerIDsToUnchoke.isEmpty())
        {
            for (int currentPeer : peerIDsToUnchoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Unchoke());

                    }
                }
            }
        }
    }
    public synchronized void notInterestedPeers(Collection<Integer> peerIDsToUnchoke)
    {
        if(peerIDsToUnchoke!=null && !peerIDsToUnchoke.isEmpty())
        {
            for (int currentPeer : peerIDsToUnchoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Uninterested());

                    }
                }
            }
        }
    }



    /*
     * A function that takes in a collection of PeerIDs and uses connectionhandler vector to unchoke certain neigbors
     * @name    unchokePeers
     * @author  Kunal Bajaj
     * @params  Collection of peerIDs that need to be unchoked
     * @returns nothing
    */
    public synchronized void chokePeers(Collection<Integer> peerIDsToChoke)
    {
        if(peerIDsToChoke!=null && !peerIDsToChoke.isEmpty())
        {
            for (int currentPeer : peerIDsToChoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Choke());

                    }
                }
            }
        }
    }

 /*the process should close when all have finished downloading the whole file*/

    public void FileHasCompleted() {
        peerManager.setPeerFileCompleted();
        isFileCompleted.set(true);
        if (isFileCompleted.get() && areNeighboursCompleted.get()) {
            logrecord.getLogRecord().fileComplete();
            EndPeerProcess.set(true);
            //logrecord.getLogRecord().fileComplete();
            logrecord.getLogRecord().closeLogger();


           System.exit(0);
        }
    }
    /*the process should close when all have finished downloading the whole file*/
    public void NeighboursHaveCompleted() {
        areNeighboursCompleted.set(true);
        if (isFileCompleted.get() && areNeighboursCompleted.get()) {
            EndPeerProcess.set(true);
            logrecord.getLogRecord().closeLogger();

          System.exit(0);
        }
    }
    /*method for Sending have messages to all peers after receiving new parts.
     * If peers no longer have interesting parts method sends not interested message
     * */
    public synchronized void gotPart(int partindex){
        for (Handler conn : connectionsList) {
            conn.pushInQueue(new Have(partindex));
            if (!peerManager.stillInterested(conn.getRemotePeerId(), fileManager.partsPeerHas()))
            {

                conn.pushInQueue(new Uninterested());
            }
        }
    }


}