package de.alive.pricecxn;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PriceCxnMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	private static final java.util.logging.Logger LOGGER = Logger.getLogger(PriceCxnMod.class.getName());
	public static final Style DEFAULT_TEXT = Style.EMPTY.withColor(Formatting.GRAY);
	public static final Style GOLD_TEXT = Style.EMPTY.withColor(Formatting.GOLD);
	public static final Style ERROR_TEXT = Style.EMPTY.withColor(Formatting.RED);
	public static final Style DEBUG_TEXT = Style.EMPTY.withColor(Formatting.RED).withItalic(true);
	public static final boolean DEBUG_MODE = true;
	public static final String MOD_NAME = "PriceCxn";
	public static final MutableText MOD_TEXT = MutableText
			.of(new PlainTextContent.Literal(""))
			.append(MutableText.of(new PlainTextContent.Literal("["))
					.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
			.append(Text.translatable("cxn_listener.mod_text")
					.setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
			.append(MutableText.of(new PlainTextContent.Literal("] "))
					.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
	public static final String MOD_VERSION = Version.MOD_VERSION;

	@Override
	public void onInitialize() {

	}

	public static void doDebug(Consumer<MinecraftClient> function){
		if(!PriceCxnMod.DEBUG_MODE) return;
		if(MinecraftClient.getInstance() == null) return;
		if(MinecraftClient.getInstance().player == null) return;
		MinecraftClient client = MinecraftClient.getInstance();

		function.accept(client);
	}

	public static void printDebug(String message, boolean overlay, boolean sysOut){
		doDebug((client) -> {
			MutableText text = MutableText.of(new PlainTextContent.Literal(message)).setStyle(PriceCxnMod.DEBUG_TEXT);
			if(client.player != null)
			    client.player.sendMessage(text, overlay);
			if(sysOut) LOGGER.log(Level.INFO, "[PCXN-DEBUG] : " + message);
		});
	}

	public static void printDebug(String message, boolean overlay){
		printDebug(message, overlay, true);
	}

	public static void printDebug(String message){
		printDebug(message, false, true);
		printDebug(message, true, false);
	}

	public static Optional<Integer> getIntVersion(@Nullable String version){
		if(DEBUG_MODE)
			LOGGER.log(Level.INFO, "Version: " + version);
		if(version == null)
			return Optional.empty();
		version = version.replaceAll("\\.", "");
		try{
			return Optional.of(Integer.parseInt(version));
		} catch (NumberFormatException e){
			return Optional.empty();
		}
	}


}