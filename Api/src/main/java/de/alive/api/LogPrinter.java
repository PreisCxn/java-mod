package de.alive.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public final class LogPrinter {
    public static final boolean DEBUG_MODE = System.getenv("PCXN_DEBUG_MODE") != null && System.getenv("PCXN_DEBUG_MODE").equals("true");
    public static final boolean TESTER_MODE = System.getenv("PCXN_TESTER_MODE") != null && System.getenv("PCXN_TESTER_MODE").equals("true");
    public static final Style DEBUG_TEXT = Style.EMPTY.withColor(Formatting.RED).withItalic(true);

    public static final Logger LOGGER = LoggerFactory.getLogger("preiscxn");

    private LogPrinter() {
    }

    public static void doDebug(@NotNull Consumer<MinecraftClient> function) {
        if (!DEBUG_MODE) return;
        if (MinecraftClient.getInstance() == null) return;
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();

        function.accept(client);
    }

    public static void printTester(String message) {
        if (!DEBUG_MODE && !TESTER_MODE) return;
        if (MinecraftClient.getInstance() == null) return;
        if (MinecraftClient.getInstance().player == null) return;
        MinecraftClient client = MinecraftClient.getInstance();

        MutableText text = MutableText.of(new PlainTextContent.Literal(message));
        client.player.sendMessage(text, true);
        LOGGER.debug("[PCXN-TESTER] : {}", message);
    }

    public static void printDebug(String message, boolean overlay, boolean sysOut) {
        doDebug(client -> {
            MutableText text = MutableText.of(new PlainTextContent.Literal(message)).setStyle(DEBUG_TEXT);
            if (client.player != null)
                client.player.sendMessage(text, overlay);
            if (sysOut) LOGGER.debug("[PCXN-DEBUG] : {}", message);
        });
    }

    public static void printDebug(String message, boolean overlay) {
        printDebug(message, overlay, true);
    }

    public static void printDebug(String message) {
        printDebug(message, false, true);
        printDebug(message, true, false);
    }
}
