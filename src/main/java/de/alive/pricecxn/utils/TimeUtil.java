package de.alive.pricecxn.utils;

import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.SearchDataAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TimeUtil {

    private static final DataAccess MINUTE_SEARCH = SearchDataAccess.MINUTE_SEARCH;
    private static final DataAccess NOW_SEARCH = SearchDataAccess.NOW_SEARCH;
    private static final DataAccess HOUR_SEARCH = SearchDataAccess.HOUR_SEARCH;
    private static final DataAccess SECOND_SEARCH = SearchDataAccess.SECOND_SEARCH;

    public static Optional<Long> getStartTimeStamp(String timerString) {
        if (StringUtil.containsString(timerString, NOW_SEARCH.getData()))
            return Optional.empty();

        Optional<Integer> hours = getTime(timerString, HOUR_SEARCH);
        Optional<Integer> minutes = getTime(timerString, MINUTE_SEARCH);

        if(minutes.isEmpty() && hours.isEmpty())
            return Optional.empty();

        if(hours.isEmpty())
            hours = Optional.of(0);
        else
            minutes = Optional.of(0);

        long elapsedSeconds = (hours.get() * 3600L + minutes.get() * 60L) * 1000;
        long day = 86400000;
        elapsedSeconds = day - elapsedSeconds;
        long startTimeStamp = System.currentTimeMillis() - elapsedSeconds;

        startTimeStamp = (startTimeStamp / (60 * 1000)) * (60 * 1000);

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

        List<String> partsList = Arrays.asList(timerString.split(" "));

        return partsList
                .stream()
                .filter(s -> StringUtil.containsString(s, search.getData()))
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
