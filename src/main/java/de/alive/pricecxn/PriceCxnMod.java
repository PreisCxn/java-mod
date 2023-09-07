package de.alive.pricecxn;

import net.fabricmc.api.ModInitializer;

import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceCxnMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	public static final Style DEFAULT_TEXT = Style.EMPTY.withColor(Formatting.GRAY);
	public static final Style ERROR_TEXT = Style.EMPTY.withColor(Formatting.RED);

	public static final String MOD_NAME = "PriceCxn";

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}
}