package de.alive.pricecxn.utils;

import de.alive.pricecxn.cytooxien.TranslationDataAccess;
import de.alive.pricecxn.networking.DataAccess;
import net.minecraft.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeUtil {
    private static final Logger LOGGER = Logger.getLogger(TimeUtil.class.getName());
    private static final DataAccess MINUTE_SEARCH = TranslationDataAccess.MINUTE_SEARCH;
    private static final DataAccess NOW_SEARCH = TranslationDataAccess.NOW_SEARCH;
    private static final DataAccess HOUR_SEARCH = TranslationDataAccess.HOUR_SEARCH;
    private static final DataAccess SECOND_SEARCH = TranslationDataAccess.SECOND_SEARCH;

    public enum TimeUnit {
        SECONDS(1000, "", ""),
        MINUTES(60 * 1000, "cxn_listener.display_prices.minutes_singular", "cxn_listener.display_prices.minutes_plural"),
        HOURS(60 * 60 * 1000, "cxn_listener.display_prices.hours_singular", "cxn_listener.display_prices.hours_plural"),
        DAYS(24 * 60 * 60 * 1000, "cxn_listener.display_prices.days_singular", "cxn_listener.display_prices.days_plural");

        private final long milliseconds;
        private final String singularTranslatable;
        private final String pluralTranslatable;

        TimeUnit(long milliseconds, String singularTranslatable, String pluralTranslatable) {
            this.milliseconds = milliseconds;
            this.pluralTranslatable = pluralTranslatable;
            this.singularTranslatable = singularTranslatable;
        }

        public long getMilliseconds() {
            return milliseconds;
        }

        public String getPluralTranslatable() {
            return pluralTranslatable;
        }

        public String getSingularTranslatable() {
            return singularTranslatable;
        }

        public String getTranslatable(Long amount) {
            if(amount == 1)
                return getSingularTranslatable();
            else
                return getPluralTranslatable();
        }

    }

    public static Optional<Pair<Long, TimeUnit>> getTimestampDifference(long timestamp) {
        long currentTimestamp = System.currentTimeMillis(); // Aktueller Unix-Timestamp in Millisekunden

        long difference = currentTimestamp - timestamp;

        if (difference <= 0) {
            return Optional.empty(); // Der übergebene Timestamp ist aktueller oder gleich der aktuellen Zeit
        }

        if (difference >= TimeUnit.DAYS.getMilliseconds()) {
            long days = difference / TimeUnit.DAYS.getMilliseconds();
            return Optional.of(new Pair<>(days, TimeUnit.DAYS));
        } else if (difference >= TimeUnit.HOURS.getMilliseconds()) {
            long hours = difference / TimeUnit.HOURS.getMilliseconds();
            return Optional.of(new Pair<>(hours, TimeUnit.HOURS));
        } else if (difference >= TimeUnit.MINUTES.getMilliseconds()) {
            long minutes = difference / TimeUnit.MINUTES.getMilliseconds();
            return Optional.of(new Pair<>(minutes, TimeUnit.MINUTES));
        }

        return Optional.empty();
    }

    public static Optional<Long> getStartTimeStamp(String timerString) {
        if (StringUtil.containsString(timerString, NOW_SEARCH.getData()))
            return Optional.empty();

        Optional<Integer> hours = getTime(timerString, HOUR_SEARCH);
        Optional<Integer> minutes = getTime(timerString, MINUTE_SEARCH);

        LOGGER.log(Level.INFO, "timerString: " + timerString);
        LOGGER.log(Level.INFO, "hours: " + hours.orElse(-1));
        LOGGER.log(Level.INFO, "minutes: " + minutes.orElse(-1));

        if(minutes.isEmpty() && hours.isEmpty())
            return Optional.empty();

        if(hours.isEmpty())
            hours = Optional.of(0);

        if(minutes.isEmpty())
            minutes = Optional.of(0);

        long elapsedSeconds = (hours.get() * 3600L + minutes.get() * 60L) * 1000;
        long day = 86400000;
        elapsedSeconds = day - elapsedSeconds;
        long startTimeStamp = System.currentTimeMillis() - elapsedSeconds;

        //startTimeStamp = (startTimeStamp / (60 * 1000)) * (60 * 1000);

        return Optional.of(startTimeStamp);
    }

    /**
     * Extrahiert die Minuten aus dem Timer-String.
     *
     * @param timerString Der Timer-String.
     * @return Die Minuten als Integer.
     */
    public static Optional<Integer> getMinutes(String timerString) {
        if (StringUtil.containsString(timerString, NOW_SEARCH.getData()))
            return Optional.empty();

        String[] parts = timerString.split(" ");
        if (parts.length > 2) {
            try {
                int minutes = Integer.parseInt(parts[2]);
                return Optional.of(minutes);
            } catch (NumberFormatException e){
                return Optional.empty();
            }
        } else {
            if(!StringUtil.containsString(parts[1], MINUTE_SEARCH.getData())) return Optional.of(0);

            try{
                int minutes = Integer.parseInt(parts[0]);
                return Optional.of(minutes);
            } catch (NumberFormatException e){
                return Optional.empty();
            }
        }
    }

    /**
     * Extrahiert die Stunden aus dem Timer-String.
     *
     * @param timerString Der Timer-String.
     * @return Die Stunden als Integer.
     */
    public static Optional<Integer> getTime(String timerString, DataAccess search) {

        timerString = timerString.toLowerCase();

        List<String> searchList = StringUtil.listToLowerCase(search.getData());

        List<String> partsList = Arrays.asList(timerString.split(" "));

        return partsList
                .stream()
                .filter(s -> StringUtil.containsString(s, searchList))
                .findFirst()
                .flatMap(s -> {
                    //getting index before
                    int index = partsList.indexOf(s);
                    if(index > 0) {
                        try {
                            int hours = Integer.parseInt(partsList.get(index - 1));
                            return Optional.of(hours);
                        } catch (NumberFormatException e) {
                            return Optional.empty();
                        }
                    } else
                        return Optional.empty();
                });
    }

    /**
     * Überprüft, ob zwei Zeitstempel innerhalb eines Zeitfensters (in Minuten) gleich sind.
     *
     * @param timestamp1     Der erste Zeitstempel in Millisekunden.
     * @param timestamp2     Der zweite Zeitstempel in Millisekunden.
     * @param windowMinutes  Das Zeitfenster in Minuten.
     * @return true, wenn die Zeitstempel innerhalb des Zeitfensters gleich sind, andernfalls false.
     */
    public static boolean timestampsEqual(long timestamp1, long timestamp2, int windowMinutes) {
        final long timeWindow = (long) windowMinutes * 60 * 1000; // 1 Minuten in Millisekunden
        long timeDifference = Math.abs(timestamp1 - timestamp2);
        return timeDifference <= timeWindow;
    }

}
