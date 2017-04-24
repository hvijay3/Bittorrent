package Peer_Related;
import Logging.logrecord;
import Peer_Related.*;


import java.io.*;
import java.util.*;
import java.util.logging.Level;


/**
 * Created by Harshit Vijayvargia on 3/28/2017.
 */

public class PeerProcess {

    static String CONFIGFILENAME =  "Common.cfg";                        // to read cfg files
    static String PEERINFOFILENAME= "PeerInfo.cfg";
    static Properties common_cfg ;
    final static LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>();




    public static void main(String args[]) throws Exception {

       Scanner inp = new Scanner(System.in);
        int peerId = inp.nextInt();
        inp.nextLine();
//int peerId = Integer.parseInt(args[0]);
        Reader commonReader =null;
        Reader peerReader =null;
        common_cfg = readCommonFile(commonReader);
        readPeerFile(peerReader);
       logrecord.getLogRecord().setLoggerForPeer(peerId);








        PeerInfo peer = PeerInfo.getPeerByPeerId(peerId,peers);
        try {
           // System.out.println(peer.getPeerId());
            PeerSetup peerSetup = new PeerSetup(common_cfg, peers, peer);
            peerSetup.startingThreadsandMethods();
            Thread t = new Thread(peerSetup);
            t.setName("Making peer as server thread");
            t.start();
            peerSetup.connectToOtherPeers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        /*listners registered for both filemanager and peer manager ,
        and file splitted in chunks(pieces) and peer received parts are set if the peers has file
        Also Peer Manager thread is started ,
        One thread for managing optimistic unchoking peer - determines randomly optimistic unchokking peer
        second thread for mainatining preferred neighbours,choked neighbours, neighbours from which optimistic
        unchoked neighbour will be selected . These all are determined using interested neighbours

        At the end of these two threads messages are need to be sent ( choke , unchoke ) .
        this is done using connection handler and listners to peer manager and file manager
         */





// Storing all peers in a LinkedList which this peer has to connect to .
// A peer connects only to previous peers

        // initializing a process associated with a peer , it's file manager , its peer manager ,
        // requires configuration and peerinfo file


    }









/*Method for storing Common.cfg file information in properties*/

    static Properties readCommonFile(Reader commonReader) {
        Properties cProperty = new Properties() {
            public synchronized void load(Reader commonReader) {

                try {
                    commonReader = new FileReader(CONFIGFILENAME);

                    BufferedReader cReader = new BufferedReader(commonReader);

                    int i=0;

                    for (String line; (line = cReader.readLine()) != null; i++) {
                        String arr[] = line.split(" ");
                        setProperty(arr[0], arr[1]);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally
                {

                    try {
                        commonReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        };
        try {
            cProperty.load(commonReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cProperty;
    }





    /*Method for Reading PeerInfo File and storing that information in  a LinkedList*/

    static void readPeerFile(Reader peerReader) {
        try {
            peerReader = new FileReader(PEERINFOFILENAME);
            BufferedReader pReader = new BufferedReader(peerReader);


            int i=0;
            for (String line; (line = pReader.readLine()) != null; i++) {
                String arr[] = line.split(" ");
                peers.add(new PeerInfo(arr[0], arr[1], arr[2], arr[3]));



            }


        } catch (Exception e) {

        }
        finally
        {
            try {
                peerReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }}




