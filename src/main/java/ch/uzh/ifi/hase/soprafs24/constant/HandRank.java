package ch.uzh.ifi.hase.soprafs24.constant;

public enum HandRank {
    HIGHCARD(0),
    ONEPAIR(1),
    TWOPAIR(2),
    THREEOFKIND(3),
    STRAIGHT(4),
    FLUSH(5),
    FULLHOUSE(6),
    FOUROFKIND(7),
    STRAIGHTFLUSH(8),
    ROYALFLUSH(9);

    public final int value;
    HandRank(int value) {
        this.value = value;
    }
}
