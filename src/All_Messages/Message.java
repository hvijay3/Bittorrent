package All_Messages;
import java.io.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
/**
 * Created by Harshit Vijayvargia on 2/21/2017.
 */
public class Message {
    public int messageLength;
    public byte messageType;
    public byte[] payLoad;
    final static Byte typeToValue[] = {0,1,2,3,4,5,6,7};
    final static HashMap<String, Byte> hash = new HashMap<String, Byte>();




    static  {


        hash.put("Choke", typeToValue[0]);
        hash.put("Unchoke", typeToValue[1]);
        hash.put("Interested", typeToValue[2]);
        hash.put("Uninterested", typeToValue[3]);
        hash.put("Have", typeToValue[4]);
        hash.put("Bitfield", typeToValue[5]);
        hash.put("Request", typeToValue[6]);
        hash.put("Piece", typeToValue[7]);

    }
    public String getMessageType()
    {
        return getMessageByByte(this.messageType);
    }
    protected Message(String type) {

        this (type, null);
    }
    Message(String type, byte[] payload)
    {
        if(payload==null)
        {
            messageLength = 1;
        }
        else if(payload.length==0)
        {
            messageLength = 1;
        }
        else {
            messageLength = payload.length +1;
        }
        messageType = hash.get(type);
        this.payLoad = payload;

    }
    Byte getTypeOfMessage(String type)
    {
        return hash.get(type);
    }

    public static String getMessageByByte(byte b)
    {
        String key ="";
      for ( Map.Entry<String,Byte> entry : hash.entrySet())
      {
          if(entry.getValue()==b)
          {
              key= entry.getKey();
              break;

          }
      }
        return key;
    }

    public static Message getMessage (int length, String type)  {
        switch (type) {
            case "Choke":
                return new Choke();

            case "Unchoke":
                return new Unchoke();

            case "Interested":
                return new Interested();

            case "Uninterested":
                return new Uninterested();

            case "Have":
                return new Have (new byte[length]);

            case "Bitfield":
            {
                if(length > 0)
                    return new Bitfield (new byte[length]);
                else
                    return new Bitfield(new byte[0]);
            }

            case "Request":
                return new Request (new byte[length]);

            case "Piece":
                return new Piece (new byte[length]);

            default:
                return  new Interested();    // EXception to be handled ( Class Not found and IO Exception).
        }
    }
    public void read (DataInputStream in) throws IOException {
        if ((payLoad != null) && (payLoad.length) > 0) {
            in.readFully(payLoad, 0, payLoad.length);
        }
    }


    public void write (DataOutputStream out) throws IOException {
        out.writeInt (messageLength);
        out.writeByte (messageType);
        if ((payLoad != null)) {
            out.write (payLoad, 0, payLoad.length);
        }
    }

    public static byte[] getPieceIndexBytes (int pieceIdx) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceIdx).array();
    }
    public  int getPieceIndex() {
        return ByteBuffer.wrap(Arrays.copyOfRange(payLoad, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }


}



