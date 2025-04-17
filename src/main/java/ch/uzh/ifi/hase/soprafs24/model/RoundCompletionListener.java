package ch.uzh.ifi.hase.soprafs24.model;

import java.util.List;

/**
 * Interface for receiving notifications when a poker round is completed
 */
public interface RoundCompletionListener {
    /**
     * Called when a round is completed and winners are determined
     * @param winners The list of winning players
     */
    void onRoundComplete(List<Player> winners);
}