package de.alive.preiscxn.impl.cytooxien.util;

import de.alive.preiscxn.api.cytooxien.ICxnListener;
import de.alive.preiscxn.api.cytooxien.ModUser;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DisplayNameUtil {
    public static boolean shouldDisplayCoinInTabList(ICxnListener listener, String originalDisplayName, UUID id) {
        List<ModUser> modUsers = listener.getModUsers();

        if (modUsers == null) return false;

        String[] strings = originalDisplayName.split(" ");
        List<String> displayList = Arrays.asList(strings);

        if (displayList.size() != 2) return false;

        String playerName = displayList.get(1).replace(" ", "");

        for (ModUser modUser : modUsers) {
            if (modUser.isCorrect(playerName, id)) {
                return true;
            }
        }

        return false;
    }
}
