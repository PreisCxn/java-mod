package de.alive.pricecxn.cytooxien;

/**
 * This enum is used to store the modes of the server
 */
public enum Modes {
        SKYBLOCK("Skyblock", "cxnprice.mode.skyblock", "\uE202"),
        CITYBUILD("Citybuild", "cxnprice.mode.citybuild", "\uE202"),
        LOBBY("Lobby", "cxnprice.mode.lobby", ""),
        NOTHING("NOTHING", "cxnprice.mode.nothing", "");

        private final String text;
        private final String translationKey;
        private final String currency;

        Modes(final String text, final String translationKey, final String currency) {
            this.text = text;
            this.translationKey = translationKey;
            this.currency = currency;
        }

        @Override
        public String toString() {
            return text;
        }

        public String getTranslationKey(){
            return translationKey;
        }

        public String getCurrency(){
            return currency;
        }

}
