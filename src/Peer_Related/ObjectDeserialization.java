package Peer_Related;
import java.io.*;
import All_Messages.*;

/**
 * Created by Harshit Vijayvargia on 4/5/2017.
 */
public class ObjectDeserialization extends DataInputStream implements ObjectInput  {
    boolean isHandshakeReceived = false;

    public ObjectDeserialization(InputStream in)
    {
        super(in);

    }

    public Object readObject()
    {
        try{

        if(!isHandshakeReceived) {
            System.out.println("in read object handshake not received block");
            HandShake handShake = new HandShake();
            if (handShake.msgIsHandShake(this)) {
                isHandshakeReceived = true;
                return handShake;
            }
            else{
                System.out.println("handshake is not received properly");
            }

            }
            else
            {
             try
             {
                final int length = readInt();
                final int payloadLength = length - 1; // subtract 1 for the message type
                Message message = Message.getMessage(payloadLength,Message.getMessageByByte(readByte()));
                message.read(this);
                return message;
            }
            catch( Exception e)
            {
                e.printStackTrace();
               // System.exit(0);
            }
        }}


    catch (Exception E)
    {
        E.printStackTrace();
    }

        return null;
    }}
