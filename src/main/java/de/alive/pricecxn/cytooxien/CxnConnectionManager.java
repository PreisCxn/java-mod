package de.alive.pricecxn.cytooxien;

import de.alive.pricecxn.PriceCxnMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CxnConnectionManager {

    /**
     * Sends a connection information message to the Minecraft player.
     * This method is used to send messages to the player about the status of the connection.
     * The message is only sent if the force parameter is true or the shouldSend parameter is true.
     *
     * @param shouldSend A boolean indicating whether the message should be sent.
     * @param message An ActionNotification object containing the message to be sent.
     * @param force A boolean that, if true, forces the message to be sent regardless of the shouldSend parameter.
     */
    public static void sendConnectionInformation(boolean shouldSend, ActionNotification message, boolean force) {

        if (force || shouldSend) {
            if (MinecraftClient.getInstance().player != null) {

                MutableText msg;
                if (message.hasTextVariables()) {

                    msg = PriceCxnMod.MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey(), (Object[]) message.getTextVariables()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                } else {

                    msg = PriceCxnMod.MOD_TEXT.copy()
                            .append(Text.translatable(message.getTranslationKey()))
                            .setStyle(PriceCxnMod.DEFAULT_TEXT);

                }
                MinecraftClient.getInstance().player.sendMessage(msg);
            }
        }

    }

    /**
     * Sends a connection information message to the Minecraft player.
     * This method is used to send messages to the player about the status of the connection.
     * The message is only sent if the shouldSend parameter is true.
     *
     * @param shouldSend A boolean indicating whether the message should be sent.
     * @param message An ActionNotification object containing the message to be sent.
     */
    public static void sendConnectionInformation(boolean shouldSend, ActionNotification message) {
        sendConnectionInformation(shouldSend, message, false);
    }
}
