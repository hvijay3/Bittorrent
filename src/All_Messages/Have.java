package All_Messages;
public class Have extends Message {

    Have (byte[] pieceIndex) {

        super ("Have", pieceIndex);
    }
    public Have (int pieceIdx) {
        this (getPieceIndexBytes (pieceIdx));
    }

      }

