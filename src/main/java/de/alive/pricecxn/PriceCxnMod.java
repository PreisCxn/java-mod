package de.alive.pricecxn;

import net.fabricmc.api.ModInitializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static de.alive.pricecxn.LogPrinter.LOGGER;

public class PriceCxnMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Style DEFAULT_TEXT = Style.EMPTY.withColor(Formatting.GRAY);
	public static final Style GOLD_TEXT = Style.EMPTY.withColor(Formatting.GOLD);
	public static final Style ERROR_TEXT = Style.EMPTY.withColor(Formatting.RED);
	public static final String MOD_NAME = "PriceCxn";

	public static final String MOD_VERSION = Version.MOD_VERSION;
	public static final MutableText MOD_TEXT = MutableText
			.of(new PlainTextContent.Literal(""))
			.append(MutableText.of(new PlainTextContent.Literal("["))
					.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
			.append(Text.translatable("cxn_listener.mod_text")
					.setStyle(Style.EMPTY.withColor(Formatting.GOLD)))
			.append(MutableText.of(new PlainTextContent.Literal("] "))
					.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));

	@Override
	public void onInitialize() {
		LOGGER.info("PriceCxn initialized");
	}

	public static @NotNull Optional<Integer> getIntVersion(@Nullable String version){
        LOGGER.debug("Version: {}", version);
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