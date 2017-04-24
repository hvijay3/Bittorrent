
package All_Messages;
public class Request extends Message {

    public Request (byte[] pieceIdx)
    {

        super ("Request", pieceIdx);
    }
    public Request (int pieceIdx) {
        this (getPieceIndexBytes (pieceIdx));
    }

}
