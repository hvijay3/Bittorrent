package Peer_Related;
import java.io.*;
import All_Messages.*;

/**
 * Created by Harshit Vijayvargia on 4/6/2017.
 */
public class ObjectSerialization extends DataOutputStream implements ObjectOutput{



public ObjectSerialization(OutputStream out)
{
    super(out);
}

public void writeObject(Object obj) throws IOException {
    if (obj instanceof HandShake) {
        ((HandShake) obj).write(this);
    }
    else {
        ((Message) obj).write(this);
    }

}
}
