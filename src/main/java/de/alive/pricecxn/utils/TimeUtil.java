package de.alive.pricecxn.utils;

import de.alive.pricecxn.DataAccess;
import de.alive.pricecxn.cytooxien.SearchDataAccess;

public class TimeUtil {

    private static final DataAccess MINUTE_SEARCH = SearchDataAccess.MINUTE_SEARCH;
    private static final DataAccess NOW_SEARCH = SearchDataAccess.NOW_SEARCH;
    private static final DataAccess HOUR_SEARCH = SearchDataAccess.HOUR_SEARCH;
    private static final DataAccess SECOND_SEARCH = SearchDataAccess.SECOND_SEARCH;

    /**
     * Extrahiert die Minuten aus dem Timer-String.
     *
     * @param timerString Der Timer-String.
     * @return Die Minuten als Integer.
     */
    public static int getMinutes(String timerString) {
        if (StringUtil.containsString(timerString, NOW_SEARCH.getData()))
            return -1;

        String[] parts = timerString.split(" ");
        if (parts.length > 2) {
            return Integer.parseInt(parts[2]);
        } else {
            return StringUtil.containsString(parts[1], MINUTE_SEARCH.getData()) ? Integer.parseInt(parts[0]) : 0;
        }
    }

    /**
     * Extrahiert die Stunden aus dem Timer-String.
     *
     * @param timerString Der Timer-String.
     * @return Die Stunden als Integer.
     */
    public static Integer getHours(String timerString) {
        if (StringUtil.containsString(timerString, NOW_SEARCH.getData()))
            return null;

        String[] parts = timerString.split(" ");

        if (parts.length > 2) {
            return Integer.parseInt(parts[0]);
        } else {
            return StringUtil.containsString(parts[1], HOUR_SEARCH.getData()) ? Integer.parseInt(parts[0]) : 0;
        }
    }




}
