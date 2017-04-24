package Peer_Related;
import Logging.logrecord;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Harshit Vijayvargia on 3/31/2017.
 */
public class PeerManager implements Runnable {

    LinkedList<PeerInfo> preferredNeighbours;
    OptimisticUnchoke unchoker = null;
    LinkedList<PeerInfo> peersExceptLocal = new LinkedList<PeerInfo>();
    PeerSetup obj = null;
    AtomicBoolean peerFileCompleted ;


    PeerManager(LinkedList<PeerInfo> peersExceptLocal, PeerSetup obj)
    {
        this.peersExceptLocal = peersExceptLocal;
        unchoker = new OptimisticUnchoke(obj);
        this.obj = obj;
        peerFileCompleted = PeerSetup.peer.hasFile()?new AtomicBoolean(true):new AtomicBoolean(false);

    }



    @Override
    public void run() {
        //System.out.println("first thread working");
        Thread t = new Thread(unchoker);         //thread for optimisticall unchoking
       // t.setName("Optimistic Unchoke Thread");
        t.start();

        /*second thread for maintaining preferred neighbours,choked neighbours, neighbours from which optimistic
        unchoked neighbour will be selected . These all are determined using interested neighbours

        Steps :
        1)Get peers in which the peer is interested

        2) Determine Preferred Neighbours : Two cases : a) In the beginning randomly 3 Preferred neighbours will be choosen.
                          shuffle the interested peers and assign the first 3
                       b) After each unchoking interval , sort the interested neighbours acco
                           rding to bytes downloaded from them and take the first two.
        3)

        */
        // Get Id's of all type of peers
// sleeps for unchoking interval
        while(true)
        {
        try {
            Thread.sleep(Integer.parseInt(PeerProcess.common_cfg.getProperty("UnchokingInterval"))*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LinkedList<PeerInfo> interestedPeers = getInterestedPeers(peersExceptLocal);
           // System.out.println(interestedPeers.size());
        HashSet<Integer> interestedPeersIds = new HashSet<Integer>();
        interestedPeersIds = PeerInfo.getPeerIds(interestedPeers);

        LinkedList<PeerInfo> preferredPeers  = getPreferredPeers(interestedPeers,peerFileCompleted.get());
        HashSet<Integer> preferredPeersIds = new HashSet<Integer>();
        preferredPeersIds = PeerInfo.getPeerIds(preferredPeers);
            if(preferredPeersIds!=null && preferredPeersIds.size()>0){
                logrecord.getLogRecord().changeOfPrefereedNeighbors(preferredPeersIds);
            }

            this.preferredNeighbours = preferredPeers;
           // System.out.println(preferredPeers.size());

        LinkedList<PeerInfo> peersToChoke = getPeersToChoke(preferredPeers);
        HashSet<Integer> peersToChokeIds= new HashSet<Integer>();
        peersToChokeIds = PeerInfo.getPeerIds(peersToChoke);
           // System.out.println(peersToChoke.size());
            obj.chokePeers(peersToChokeIds);                //choking peers
            obj.unchokePeers(preferredPeersIds);

        LinkedList<PeerInfo> optimisticallyUcPeers = getOptimisticallyUcPeers(preferredPeers,interestedPeers);
        HashSet<Integer> optimisticallyUnchokePeerIds = new HashSet<Integer>();
            optimisticallyUnchokePeerIds = PeerInfo.getPeerIds(optimisticallyUcPeers);



// transfer optimistic unchoked ids for the process or class which manages unchoking mechanism
        //message handling  : choke msgs

            //unchoking peers
           if(optimisticallyUcPeers!=null){
        unchoker.setUnchokable(optimisticallyUcPeers);}

    
    }}



    public void setPeerFileCompleted()
    {
        peerFileCompleted.set(true);
    }


    synchronized void receivedPart(int peerId, int size) {
        PeerInfo peer  = searchForPeer(peerId);
        if (peer != null) {
            peer._bytesDownloadedFrom.addAndGet(size);
        }
    }

    //return Optimistically unchokablepeers if present else returns null

    synchronized LinkedList<PeerInfo> getOptimisticallyUcPeers(LinkedList<PeerInfo> preferredPeers, LinkedList<PeerInfo> interestedPeers)
    {
        if(interestedPeers.size()>2){
            LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>(interestedPeers);
            peers.removeAll(preferredPeers);
            return peers;}
        else
            return null;
    }


    synchronized LinkedList<PeerInfo> getPreferredPeers(LinkedList<PeerInfo> interestedPeers,boolean peerFileCompleted)
    {
        LinkedList<PeerInfo> preferredPeers = new LinkedList<PeerInfo>();
        if( interestedPeers!=null && !interestedPeers.isEmpty()) {
            if (peerFileCompleted) {
                Collections.shuffle(interestedPeers);

            } else {
                Collections.sort(interestedPeers, new Comparator<PeerInfo>() {
                    @Override
                    public int compare(PeerInfo o1, PeerInfo o2) {
                        if (o1._bytesDownloadedFrom.get() > o2._bytesDownloadedFrom.get()) {
                            return 1;
                        } else {
                            return -1;
                        }

                    }
                });
            }
//take the top 2 from this sorted PeerInfo list

            int i = 1;
            for (PeerInfo p : interestedPeers) {
                preferredPeers.add(p);
                if (i == 2 || i == interestedPeers.size()) {
                    break;
                }
                i++;
            }
        }
        return preferredPeers;

    }



//return peers to choke

    synchronized LinkedList<PeerInfo> getPeersToChoke(LinkedList<PeerInfo> preferredPeers)
    {
        LinkedList<PeerInfo> peersToChoke = new LinkedList<PeerInfo>(peersExceptLocal);
        peersToChoke.removeAll(preferredPeers);
        return peersToChoke;
    }




    /*
        * This searches for the obj related to a particular peerID in RemotePeerInfo and sets its isInterested to true
        * @name    setIsInterested
        * @author  Kunal Bajaj
        * @params  id of remote peer that needs to be updated
        * @returns void
       */
    public synchronized void setIsInterested(int idOfRemotePeer)
    {
        //Search of this peer
        PeerInfo interestedPeer = searchForPeer(idOfRemotePeer);

        if(interestedPeer!=null)
            interestedPeer._interested.set(true);
           // interestedPeer.setInterested();
    }

    /*
     * This searches for the obj related to a particular peerID in RemotePeerInfo and sets its isInterested to true
     * @name    setIsNotInterested
     * @author  Kunal Bajaj
     * @params  id of remote peer that needs to be updated
     * @returns void
    */
    public synchronized void setIsNotInterested(int idOfRemotePeer)
    {
        //Search of this peer
        PeerInfo notInterestedPeer = searchForPeer(idOfRemotePeer);
        if(null != notInterestedPeer)
           notInterestedPeer. _interested.set (false);
            //notInterestedPeer.setNotIterested();
    }

    /* To get Interested peers */


    /*
        * Search for a particular Peer using in the _peersToConnectTo hashset using peerID.
        * If the peer is found - return the corresponding PeerInfo object.
        * If the peer is not found then just return null;
        * @name    searchForPeer
        * @author  Kunal Bajaj
        * @params  peerID of the peer to search
        * @returns PeerInfo object or null
       */
    public PeerInfo searchForPeer(int peerID)
    {
        if(peersExceptLocal!=null &&!peersExceptLocal.isEmpty())
        {
            for (PeerInfo temp : peersExceptLocal)
            {
                if(peerID == temp.getPeerId())//When ID was found just return it
                    return temp;
            }
        }
        return null;
    }

    /*
     * This function updates _receivedParts field of the remote peer.
     * @name    updateHave
     * @author  Kunal Bajaj
     * @params  pieceIndex of the piece that needs to update for a particular peerID
     * @params  remotePeerID that needs to be updated
     * @return  void
    */
    public synchronized void updateHave(int pieceIndex, int remotePeerID)
    {
        PeerInfo peer = searchForPeer(remotePeerID);
        if(null != peer)
            peer._receivedParts.set(pieceIndex);
        haveNeighboursCompleted();
    }

    /*Method to compare the parts of the local peer with that of a remote peer. If it remote peer no
         * longer has any interesting parts the method returns true*/
    synchronized boolean stillInterested(int peerId, BitSet bitset) {
        PeerInfo peer  = searchForPeer(peerId);
        if (peer != null) {
            BitSet pBitset = (BitSet) peer._receivedParts.clone();
            pBitset.andNot(bitset);
            return ! pBitset.isEmpty();
        }
        return false;
    }
    /*
     * This function updates _receivedParts field of the remote peer.
     * @name    updateHave
     * @author  Kunal Bajaj
     * @params  bitfield that contains information about parts peer has
     * @params  remotePeerID that needs to be updated
     * @return  void
    */
    /*After 'have' message has been received, it is checked if all the neighbors have completed,
     * if so then the PeerSet object will invoke a method to terminate*/

    private synchronized void haveNeighboursCompleted() {
        for(PeerInfo peer : peersExceptLocal ){
            if (peer._receivedParts.cardinality() < FileManager._bitsetSize) {
                return;
            }
        }
        obj.NeighboursHaveCompleted();
    }
    public synchronized void updateBitField(int remotePeerID, BitSet bitfield)
    {
        PeerInfo peer =  searchForPeer(remotePeerID);
        if(null != peer)
            peer._receivedParts = bitfield;
        haveNeighboursCompleted();
    }

    /*
     * Function tells us whether we can transfer to a peer or not.
     * This will happen only when the neighbor is either a preferred neighbor or is optimistically unchoked.
     * @name    canTransferToPeer
     * @author  Kunal Bajaj
     * @params  remote peer id
     * @returns boolean
    */
    public synchronized boolean canTransferToPeer(int remotePeerID)
    {
        PeerInfo peer = searchForPeer(remotePeerID);
        System.out.println(preferredNeighbours + " " + unchoker.unchokable.size() + " " + remotePeerID + " " + PeerInfo.getPeerByPeerId(remotePeerID,peersExceptLocal)._interested );
        if(preferredNeighbours!=null && unchoker.unchokable!=null){
            System.out.println();
        return (preferredNeighbours.contains(peer) || unchoker.unchokable.contains(peer));}
        else{return false;}
    }

    /*
     * Returns bitset containing parts that a remote peer contains.
     * @name    getReceivedParts
     * @author  Kunal Bajaj
     * @params  id of remote peer
     * @returns BitSet
    */
    public synchronized BitSet getReceivedParts(int remotePeerID)
    {
        PeerInfo peer  = searchForPeer(remotePeerID);
        if (peer != null)
            return (BitSet) peer._receivedParts.clone();                //peer info received parts tells the parts which other peers have
        return new BitSet();  // empry bit set
        //System.out.println("before a piece is made by peer1" +_fileManager.partsPeerHas().toString());
    }




    /* To get Interested peers */

    synchronized LinkedList<PeerInfo> getInterestedPeers(LinkedList<PeerInfo> peers)
    {
        LinkedList<PeerInfo> interestedPeers= new LinkedList<PeerInfo>();
        for(PeerInfo p : peers)
        {
            if(p.isInterested()) { interestedPeers.add(p);}

        }
        return interestedPeers;
    }
}