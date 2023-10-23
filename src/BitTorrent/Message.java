package BitTorrent;
import java.io.Serializable;
import java.util.List;

public class Message implements Serializable
{
    private int msgLength;
    private byte msgType;

    private List<Integer> msgPayload;
    private String msgBitfield = "";

    public Message()
    {
    }
    public Message(int length, byte type, List<Integer> payload) // regular message
    {
        this.msgLength = length;
        this.msgType = type;
        this.msgPayload = payload;
    }
    public Message(int length, byte type, String bitfield) // bit field message
    {
        this.msgLength = length;
        this.msgType = type;
        this.msgBitfield = bitfield;
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
