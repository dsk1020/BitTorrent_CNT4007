package BitTorrent;

public class Type {
    final static public byte choke = 0;
    final static public byte unchoke = 1;
    final static public byte interested = 2;
    final static public byte not_interested = 3;
    final static public byte have = 4;
    final static public byte bitfield = 5;
    final static public byte request = 6;
    final static public byte piece = 7;
}
