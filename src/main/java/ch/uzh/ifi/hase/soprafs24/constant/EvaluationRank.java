package ch.uzh.ifi.hase.soprafs24.constant;

public enum EvaluationRank {
    TENTH(0),
    NINETH(1),
    EIGHTH(2),
    SEVENTH(3),
    SIXTH(4),
    FIFTH(5),
    FOURTH(6),
    THIRD(7),
    SECOND(8),
    FIRST(9);

    public final int value;
    EvaluationRank(int value) {
        this.value = value;
    }
    public static EvaluationRank fromValue(int value) {
        for (EvaluationRank rank : EvaluationRank.values()) {
            if (rank.value == value) {
                return rank;
            }
        }
        return null;
    }

}
