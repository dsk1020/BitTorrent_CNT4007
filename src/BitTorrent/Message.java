package BitTorrent;

import java.util.List;

public class Message
{
    private int msgLength;
    private byte msgType;

    private List<Integer> msgPayload;
    private String msgBitfield = "";

    public void Message(int length, byte type, List<Integer> payload)
    {
        this.msgLength = length;
        this.msgType = type;
        this.msgPayload = payload;
    }

    public int getMsgLength()
    {
        return this.msgLength;
    }
    public byte getMsgType()
    {
        return this.msgType;
    }


}
