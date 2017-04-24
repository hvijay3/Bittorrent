/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Peer_Related;

import static All_Messages.Message.getMessageByByte;
import All_Messages.*;
import Logging.logrecord;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Kunal
 */
public class MsgHandler {

    //This requires the following variables
    private FileManager _fileManager;
    private boolean _isChoked;
    private LogRecord _logger;
    private PeerManager _peerManager;
    private AtomicInteger _remotePeerID;

    //Constructor. Here we just set the class variables
    public MsgHandler(AtomicInteger remotePeerId, FileManager fmObj, PeerManager pmObj)
    {
        //Initially set _isChoked to true for this peer
        _remotePeerID = remotePeerId;
        _isChoked = true;
        _fileManager = fmObj;
        _peerManager = pmObj;
       // _logger = lrObj;
    }

    /*
     * A function that takes in a handshake message and returns bitfield message.
     * @author  Kunal Bajaj
     * @params  Handshake message that was received upon establishing connection
     * @returns Bitfield message
     * @throws  no error
    */
    public Message handleHandshake(HandShake msg)
    {
        //Get receivedParts
        BitSet b = _fileManager.partsPeerHas();
        //Create a new bitfield message
        if(!b.isEmpty())
            return(new Bitfield(b.toByteArray()));

        //If there is some error null must be returned
        return null;
    }

    /*
     * A function that takes in message as a parameter and then depending on the type of message performs appropriate action
     * @author  Kunal Bajaj
     * @params  a byte that represents what type of message we need to handle
     * @returns appropriate message type
     * @throws  No error. Just returns null if something isn't correct.
    */
    public synchronized Message genericMessageHandle( Message msg)
    {
        if(null != msg)
        {
            //Get the type of message using supplied byte
            String msgType = msg.getMessageType();

            //Now Switch on the basis of msgType
            switch(msgType)
            {
                case "Choke":
                {
                    _isChoked = true;
                    logrecord.getLogRecord().choked(_remotePeerID.get());
                    return null;
                }

                case "Unchoke":
                {
                    _isChoked = false;
                    //Now we can request for a new piece
                    logrecord.getLogRecord().unchoked(_remotePeerID.get());
                    System.out.println("receiving opt unchoke");
                    return requestForAPiece();
                }

                case "Interested":
                {
                    //Set the remote peer as interested.
                    _peerManager.setIsInterested(_remotePeerID.get());
                    logrecord.getLogRecord().receivedInterested(_remotePeerID.get());
                    //System.out.println("Interested");
                    return null;
                }

                case "Uninterested":
                {
                    //set not interested
                    _peerManager.setIsNotInterested(_remotePeerID.get());
                    logrecord.getLogRecord().receivedNotInterested(_remotePeerID.get());
                    return null;
                }

                case "Have":
                {
                    Have h = (Have)msg;
                    //Get the piece index from payload
                    final int index = ByteBuffer.wrap(Arrays.copyOfRange(h.payLoad, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt(); //converting an integer to 4 bytes array
                    //This index needs to be updated in the remote peer's _receivedParts bitset. This can be done using peer manager
                    _peerManager.updateHave(index, _remotePeerID.get());
                    logrecord.getLogRecord().receivedHave(_remotePeerID.get(),index);

                    //If the received piece is of interest then create a new interested message and return it back or else send not interested
                    if(!_fileManager.partsPeerHas().get(index)) {
                        return new Interested();
                    }
                    else {
                        return new Uninterested();
                    }
                }

                case "Bitfield":
                {
                    //Here we recieve a bitfield from some other peer. We need to update _receivedParts bitset of the peer with this information
                    Bitfield bitfield = (Bitfield)msg;
                    BitSet bitset = BitSet.valueOf(bitfield.payLoad);
                    _peerManager.updateBitField(_remotePeerID.get(), bitset);

                    //check if there are any interesting parts that this peer has
                    bitset.andNot(_fileManager.partsPeerHas());     // returns the bitset after modification in first bitset ( clears all bits
                    //  whose corresponding bits in the arguement bitset are 1

                    //System.out.println("Bitfield");
                    if(!bitset.isEmpty())
                        return new Interested();
                    else
                        return new Uninterested();
                }

                case "Request":
                {

                    logrecord.getLogRecord().peerLogger.log(Level.INFO, " for for piece ");
                /* A remote peer has sent this peer a request message. We will do the following
                    1. Get the index of the part the peer has requested for.
                    2. Using peerId, determine whether we are allowed to exchange data with this peer. This will happen if the peer is either a preferred
                        neighbor or an optimistically unchoked neighbor.
                    3. If yes, then we will generate a new piece message and send the data to the peer
                */ // System.out.println("request above");
                    Request r = (Request)msg;
                    int pieceRequestedFor = ByteBuffer.wrap(Arrays.copyOfRange(r.payLoad, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
                    //System.out.println( " Piece requested for " + pieceRequestedFor + " : "+_remotePeerID);
                    if(pieceRequestedFor!=-1 && _fileManager.partsPeerHas().get(pieceRequestedFor) && _peerManager.canTransferToPeer(_remotePeerID.get()))
                    {
                        byte[] temp = _fileManager.getPiece(pieceRequestedFor, _fileManager._PartsPath);

                        if(null != temp){
                           // System.out.println("before a piece is made by peer1" +_fileManager.partsPeerHas().toString());
                            return new Piece(temp);
                    }
                    return null;

                }}

                case "Piece":
                {
                    
                    if(msg.getMessageType().equals("Request"))
                    {
                       return null; 
                    }
                    System.out.println(msgType);
                    //System.out.println("Piece");
                
                

                    
              //  
                   Piece piece = (Piece) msg;
               // }
              
                    //System.out.println("0" +_fileManager.partsPeerHas().toString());
                    int sentPieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(piece.payLoad, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
                  ;
                    logrecord.getLogRecord().pieceDownloaded(_remotePeerID.get(), piece.getPieceIndex(), _fileManager.partsPeerHas().cardinality());

                    _fileManager.addPiece(sentPieceIndex, piece.getPieceContent());//Something that needs to be seen further
                    _peerManager.receivedPart(_remotePeerID.get(), piece.getPieceContent().length);

                    //System.out.println("2nd" + _fileManager.partsPeerHas().toString());

                    return requestForAPiece();
                }
            }
        }
        return null;
    }

    /*
     * This function is called once current peer is unchoked or it has completed downloading a piece from a remote peer.
     * We check if this peer has some interesting parts and if it does then ask for this piece by sending a new request message with that piece id.
     * @author  Kunal Bajaj
     * @name    requestForAPiece
     * @params  None
     * @returns request message if everything works fine/ Else return null
    */
    private Message requestForAPiece()
    {
      //  System.out.println("comes in request for piece");
        if(!_isChoked)
        {
            logrecord.getLogRecord().peerLogger.log(Level.INFO, "Asking for piece beore ");
            //System.out.println("before index to request" +_fileManager.partsPeerHas().toString());
            int indexOfPieceToRequest = _fileManager.partsToRequest(_peerManager.getReceivedParts(_remotePeerID.get()));
            //System.out.println("before asking for a piece after unchoke is made by peer1" +_fileManager.partsPeerHas().toString());
            System.out.println("peer2 asking for  index " + indexOfPieceToRequest);
            if(indexOfPieceToRequest >= 0){
                return new Request(indexOfPieceToRequest);}
                else{
                    return new Uninterested();//this converts the pieceIndex to bytes
            }
        }

        return null;
    }

}

