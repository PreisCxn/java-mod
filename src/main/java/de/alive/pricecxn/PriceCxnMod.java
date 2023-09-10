package de.alive.pricecxn;

import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class PriceCxnMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	public static final Style DEFAULT_TEXT = Style.EMPTY.withColor(Formatting.GRAY);
	public static final Style ERROR_TEXT = Style.EMPTY.withColor(Formatting.RED);
	public static final Style DEBUG_TEXT = Style.EMPTY.withColor(Formatting.RED).withItalic(true);
	public static boolean DEBUG_MODE = true;

	public static final String MOD_NAME = "PriceCxn";

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}

	public static void doDebug(Consumer<MinecraftClient> function){
		if(!PriceCxnMod.DEBUG_MODE) return;
		if(MinecraftClient.getInstance() == null) return;
		if(MinecraftClient.getInstance().player == null) return;
		MinecraftClient client = MinecraftClient.getInstance();

		function.accept(client);
	}

	public static void printDebug(String message, boolean overlay){
		doDebug((client) -> {
			MutableText text = MutableText.of(new LiteralTextContent(message)).setStyle(PriceCxnMod.DEBUG_TEXT);
			client.player.sendMessage(text, overlay);
			System.out.println("[PCXN-DEBUG] : " + message);
		});
	}

	public static void printDebug(String message){
		printDebug(message, false);
	}



}