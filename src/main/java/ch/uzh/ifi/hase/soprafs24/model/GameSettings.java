    package ch.uzh.ifi.hase.soprafs24.model;

    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;

    public record GameSettings(
            @JsonProperty("initialBalance") long initialBalance,
            @JsonProperty("smallBlind") long smallBlind,
            @JsonProperty("bigBlind") long bigBlind) {

        public GameSettings(ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings) {
            this(gameSettings.getInitialBalance(), gameSettings.getSmallBlind(), gameSettings.getBigBlind());
        }
    }
