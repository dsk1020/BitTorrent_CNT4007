package BitTorrent;
import java.io.Serializable;

public class Handshake implements Serializable
{
    private String _header;
    private String _0bits;
    private int _id;

    public void Handshake(int id)
    {
        this._header = "P2PFILESHARINGPROJ";
        this._0bits = "0000000000";
        this._id = id;
    }

    public int getID()
    {
        return this._id;
    }
    public void setID(int id)
    {
        this._id = id;
    }

    public void setHeader(String header) // set custom header if you want
    {
        this._header = header;
    }

    public String getHandshakeMessage() // get a string of full message for testing
    {
        return _header + _0bits + _id;
    }
}
