


package Peer_Related;

      import Logging.logrecord;

      import java.io.*;
      import java.nio.ByteBuffer;
      import java.nio.ByteOrder;
      import java.util.*;

public class FileManager
{
    private final int _peerId;   //peer
    private String _fileName;
    private final boolean _hasFile;  //peer
    private final BitSet _partsPeerHas;
    private final int _partSize;    // common_cfg
    public static  int _bitsetSize;  // common_cfg
    private int _fileSize;          // common_cfg - same as above
    public static String _PartsPath;
    public static String _actualFilePath;
    private BitSet _partsRequested;   // used for request message
    public static String _fileCreationPath;

    PeerSetup peersetupobj = null;

    public FileManager(PeerInfo peer, Properties obj,PeerSetup peersetupobj) throws Exception
    {
        _peerId = peer.getPeerId();
        _hasFile = peer.hasFile();
        _partSize = Integer.parseInt(obj.getProperty("PieceSize"));
        _fileSize = Integer.parseInt(obj.getProperty("FileSize"));
        _bitsetSize = (int) Math.ceil ((float)_fileSize/(float)_partSize);   //number of parts in the file
        //System.out.println("number of pieces " +_bitsetSize);
        this.peersetupobj = peersetupobj;
        _fileName = obj.getProperty("FileName");
        _fileCreationPath = "./peer_"+_peerId+"/files/" + _fileName;
        _PartsPath = "./peer_"+_peerId+"/files/parts/";
       // _actualFilePath = "C:/Users/shriya/allCountries.txt";


        //Create a new bitset array that will help us determine which pieces a file has and also the total number of pieces in the file
        _partsPeerHas = new BitSet(_bitsetSize);
        _partsRequested = new BitSet(_bitsetSize);




                                                            //REQUESTED PARTS CONSTRUCTOR CALLED HERE
                 makeDirectoryAndFile(_fileCreationPath,_PartsPath);

        //System.out.println("stage 2 checking peer has file");                                                 // Destination directory and some methods to be seen

        //If the peer has the complete file then call split file function to split the file
        if(_hasFile)
        {
            //Set all bits in the bitset array
            setAllParts();

            //System.out.println("stage 3 peer has file now splitting");

            //Call splitfile to split the file
            try
            {
                //File path string needs to change
               // File file =  makeDirectoryAndFile(_peerId,obj.getProperty("FileName"));
                splitFile(_partSize, _fileCreationPath, _PartsPath);

                                                                            //  here path of the file to be given
            }
            catch(Exception e)
            {
                System.err.println(e);
            }
        }

        //else
           // throw new Exception("FileManager could not be initialized");
    }

    public BitSet partsPeerHas()
    {
        return (BitSet)_partsPeerHas.clone();
    }

    /*
     * A function that does the following - update piece index by calling piece arrived and sends have message to all connected
     * peers, save file to directory, merge file if all pices have arrived.
     * @author  Kunal Bajaj
     * @params  int i - index of the arrived piece
     * @params  byte[] p - file piece that needs to be added in terms of a byte array
     * @params  ID of the peer from whom we are recieving the part
     * @returns void
    */
    public synchronized void addPiece(int i, byte[] p)
    {

        if(!hasPart(i))
        {
            //System.out.println("Here we are");
            _partsPeerHas.set(i);
            //System.out.println("it does nt have part");

            //call piece arrived and update piece information

            saveToDirectory(i, p);

            //System.out.println("piece saved in directory");

            peersetupobj.gotPart(i);

            //Write file to directory


            //check if file is complete
            if(isFileCompleted())
            {

                //Then call merge file to get the complete file
                mergeFile(_partsPeerHas.cardinality());
                peersetupobj.FileHasCompleted();

            }
        }
    }

    /*
     * A simple function that takes in piece index and byte array and writes it to parts directory of the peer
     * @author  Kunal Bajaj
     * @params  index of the piece
     * @params  byte array that needs to be written to file
     * @returns void
     * @throws  IOException, FileNotFoundException
    */
    public  void saveToDirectory(int i, byte[] p)
    {
        FileOutputStream op = null;
        try
        {
            //System.out.println("saving in directory");
            //create a file handle
            File f = new File(_PartsPath+"/"+i);
            //create an output stream handle
            op = new FileOutputStream(f);
            //write the file
            peersetupobj.gotPart(i);
            op.write(p);
            op.flush();
            op.close();
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }

    public void makeDirectoryAndFile( String _fileCreationPath,String _PartsPath){
        File _partsDir = new File(_PartsPath);   //this is making a file or directory hierarchy
        _partsDir.mkdirs();
        File file = new File(_fileCreationPath);

    }

    /*
     * Function that handles operations to do when a piece arrives.
     * These operations include updating _partsPeerHas bitset and creating a new Have message to be sent to all connected peers.
     * @author  Kunal Bajaj
     * @params  int index - the index of the piece that has arrived.
     * @returns void
     */


    /*
     * Function that checks if the peer has a certain part and returns a boolean value as answer.
     * This is done using _partsPeerHas bitset.
     * @author      Kunal Bajaj
     * @parameters  index of the part that needs to be set
     * @returns     void
     */
    public synchronized boolean hasPart(int index)
    {
        return index >=0 && index<_partsPeerHas.size() && _partsPeerHas.get(index) == true;
    }
    public static byte[] getPieceIndexBytes (int pieceIdx) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceIdx).array();
    }

    public byte[] getPiece(int index, String path)
    {
        //Only proceed if this peer has the file
        if(hasPart(index) && path.trim().compareTo("") != 0)
        {
            byte[] b = new byte[4];
            b = getPieceIndexBytes(index);
            try
            {
                File f = new File(path+ "/" + Integer.toString(index));
                FileInputStream in = null;
                try
                {
                    in = new FileInputStream(f);
                    //create a new byte array

                    //System.out.println("geting piece");

                    byte[] barray = new byte[(int)f.length()];
                    byte[] barrayfinal = new byte[(int)f.length() + b.length];
                    in.read(barray);
                    for(int i=0;i<4;i++)
                    {
                        barrayfinal[i] =b[i];
                    }
                    for(int i=4;i<barrayfinal.length;i++)
                    {

                        barrayfinal[i]=barray[i-4];
                    }

                    //read file contents;


                    //return barray
                    return barrayfinal;
                }
                catch(FileNotFoundException e)
                {
                    System.err.println(e);
                }
            }
            catch(Exception e)
            {
                System.err.println(e);
            }
        }
        return null;
    }

    /*
     * A functiont that simply returns the parts that have been currently requested.
     * @author  Kunal Bajaj
     * @params  None
     * @returns  void
    */
    public synchronized BitSet partsBeingRequested()
    {
        return _partsRequested;
    }

    /*
     * A function that returns a part index that needs to be requested.
     * First we check whether the remote peer has an  interesting part. If it does then we randomly select a part from them and request for that part
     * @author  Kunal Bajaj
     * @params  BitSet partsRemotePeerHas -  a bitset containing information about the parts a remotePeerHas
     * @returns index of the part to request
    */
    synchronized int partsToRequest(BitSet partsRemotePeerHas)
    {
        //check if the remote peer has some interesting part. This function clears all bits in partsRemotePeerHas that are set in _partsPeerHas
        partsRemotePeerHas.andNot(partsPeerHas());

        //Now check whether there are any parts that there are any interesting parts that have not yet been requested.
        partsRemotePeerHas.andNot(_partsRequested);

        //proceed only if the partsRemotePeerHas is not empty
        if(!partsRemotePeerHas.isEmpty())
        {
            //Get all set values in the bitset and add them to a list
            List<Integer> listOfSetIndices = new ArrayList();
            for(int i = partsRemotePeerHas.nextSetBit(0); i != -1 ; i = partsRemotePeerHas.nextSetBit(i+1))
                listOfSetIndices.add(i);
            if(listOfSetIndices.size()>0){
            int index = listOfSetIndices.get(new Random().nextInt(listOfSetIndices.size()));
            _partsRequested.set(index);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                synchronized (_partsRequested) {
                                    _partsRequested.clear(index);
                                  //  LogHelper.getLogger().debug("clearing requested parts for pert " + partId);
                                }
                            }
                        },
                        3000
                );






            return index; }


            //Request for part again after some time out code goes here

        }

        return -1;
    }

    /*
     * A simple function that sets all indices to 1 in _partsPeerHas
     * This function will be called when a particular peer has the complete file.
     * @author  Kunal Bajaj
     * @params  None
     */
    public void setAllParts()
    {
        for (int i = 0; i < _bitsetSize; i++)
            _partsPeerHas.set(i, true);
    }

    /*
     * A function that determines when all parts have been received.
     * This function will help to determine when we need to merge the files.
     * If any value in _partsPeerHas is 0 then it returns false else it returns true
     * @author  Kunal Bajaj
     * @params  None
     * @returns void
    */
    private boolean isFileCompleted()
    {
        for (int i = 0; i < _bitsetSize; i++)
            if (!_partsPeerHas.get(i)){
                return false;}
        logrecord.getLogRecord().fileComplete();

        return true;
    }

    /*
     * A function that splits file into pieces. Each piece is of size = _partSize.
     * This is done only for peers that have the complete file.
     * @author  Kunal Bajaj
     * @params  partSize - the size of each part in bytes
     * @params  path - The directory where the file is present.
     * @returns void
     * @throws  IOException/Invalid parameters Exception
     */
    private void splitFile(int partSize, String _fileCreationPath, String _PartsPath) throws Exception
    {
        //Proceed only when part size is greater than zero
        if(partSize <= 0 || _fileName.trim().compareTo("") == 0)
        {
            throw new Exception ("Invalid file name or part size too small to split");
        }
        else
        {
            //Open a file input and output Stream
            FileInputStream ip = null;
            FileOutputStream op = null;

            //Start reading bytes from the original file
            try
            {
                //System.out.println("accessing file for splitting it");
                ip = new FileInputStream(_fileCreationPath);                        //here file path will come

                //create a byte array for storing contents of the file that have been read
                byte[] chunkOfFile;
                int bytesToRead = _partSize;
                int read = 0;
                int chunkNumber = 0;
                int offset = 0;
                while (offset < _fileSize)
                {
                    //System.out.println("offset" + offset + " _partSize" + _partSize + " bytestoread" +bytesToRead );
                    //Need to check this part eventually. This is important. This should be if(fileSize < readLength)
                    if (_fileSize - offset < bytesToRead)//Needs to be seen why. This should be part size I guess
                    {
                        bytesToRead = _fileSize - offset;
                    }

                    //These are the number of bytes we need to read
                    chunkOfFile = new byte[bytesToRead];
                    read = ip.read(chunkOfFile);

                    //Increase offset
                    offset += read;

                    //Get ready to write the file
                    //System.out.println("stage 4 accessing file to split");
                    op = new FileOutputStream(new File(_PartsPath + "/" + Integer.toString(chunkNumber++)));      //path to be seen
                    op.write(chunkOfFile);
                    op.flush();
                    op.close();
                    chunkOfFile = null;
                    op = null;
                }
                ip.close();
            }
            catch(IOException e)
            {
                System.err.println(e);
            }
        }
    }

    private void mergeFile(int numberOfParts)
    {
        System.out.println("Inside merge file" + numberOfParts);
        if(numberOfParts > 0)
        {
            try
            {
                //Open output and input streams
                FileOutputStream os  = new FileOutputStream(new File(_fileCreationPath));
                FileInputStream is = null;
                byte[] temp = null;
                //merge all parts
                for(int i=0; i<numberOfParts; i++)
                {
                    //Create a file handler for this file type
                    File f = new File(_PartsPath+"/"+i);
                    is = new FileInputStream(f);

                    //A new byte array of length of the file
                    temp = new byte[(int)f.length()];
                     is.read(temp);
                    //Write the piecce
                    os.write(temp);
                    temp = null;
                    is.close();
                    is = null;
                }
                os.close();
            }

            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }
}
