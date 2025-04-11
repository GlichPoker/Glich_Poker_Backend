    package ch.uzh.ifi.hase.soprafs24.model;

    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;

    public record GameSettings(
            @JsonProperty("initialBalance") long initialBalance,
            @JsonProperty("smallBlind") long smallBlind,
            @JsonProperty("bigBlind") long bigBlind) {

        @JsonCreator
        public GameSettings(
                @JsonProperty("initialBalance") long initialBalance,
                @JsonProperty("smallBlind") long smallBlind,
                @JsonProperty("bigBlind") long bigBlind) {
            this.initialBalance = initialBalance;
            this.smallBlind = smallBlind;
            this.bigBlind = bigBlind;
        }

        public GameSettings(ch.uzh.ifi.hase.soprafs24.entity.GameSettings gameSettings) {
            this(gameSettings.getInitialBalance(), gameSettings.getSmallBlind(), gameSettings.getBigBlind());
        }
    }
