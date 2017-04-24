package Peer_Related;

import All_Messages.Message;
import All_Messages.Request;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static Peer_Related.PeerSetup.fileManager;

/**
 * Created by Harshit Vijayvargia on 4/8/2017.
 */
public class RequestTimeOut extends TimerTask {
    Request msg;
    FileManager f1 = null;
    ObjectSerialization out = null;
    AtomicInteger remotePeerId = new AtomicInteger(-1);
    Message msg1 ;

    public RequestTimeOut(Request message, FileManager f1, ObjectSerialization out, Message message1, AtomicInteger remotePeerId)
    {
        super();
        this.msg = message;
        this.f1 = f1;
        this.out = out;
        this.msg1 = message1;


    }


    public void run()
    {
        if (fileManager.hasPart(msg.getPieceIndex()))
        {
return;
        }
        else {

            try {
                out.writeObject(msg1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
