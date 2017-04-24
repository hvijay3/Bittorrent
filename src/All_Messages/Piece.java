package All_Messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Piece extends Message {

   public Piece (byte[] payload) {
        super ("Piece", payload);
    }
    // return piece index in the piece ( first 4 bhytes of payLoad
    public int getPieceIndex() {
        return ByteBuffer.wrap(Arrays.copyOfRange(payLoad, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }


    // To reframe
    public Piece (int pieceIdx, byte[] content) {
        super ("Piece", join (pieceIdx, content));
    }

    public byte[] getPieceContent() {
        if ((payLoad == null) || (payLoad.length <= 4)) {
            return null;
        }
        return Arrays.copyOfRange(payLoad, 4, payLoad.length);
    }

    private static byte[] join (int pieceIdx, byte[] second) {
        byte[] concat = new byte[4 + (second == null ? 0 : second.length)];
        System.arraycopy(getPieceIndexBytes (pieceIdx), 0, concat, 0, 4);
        System.arraycopy(second, 0, concat, 4, second.length);
        return concat;
    }






    }

