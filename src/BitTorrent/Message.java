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

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public byte getMsgType()
    {
        return this.msgType;
    }

    public void setMsgType(byte msgType) {
        this.msgType = msgType;
    }

    public String getMsgBitfield() {
        return msgBitfield;
    }
    public void setMsgBitfield(String bitfield)
    {
        this.msgBitfield = bitfield;
    }

    public List<Integer> getMsgPayload() {
        return msgPayload;
    }

    public void setMsgPayload(List<Integer> msgPayload) {
        this.msgPayload = msgPayload;
    }
}
