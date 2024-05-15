package de.alive.preiscxn.fabric;

import de.alive.preiscxn.api.PriceCxn;
import de.alive.preiscxn.impl.Version;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;



public class PriceCxnMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Style DEFAULT_TEXT = Style.EMPTY.withColor(Formatting.GRAY);
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
        PriceCxn.getMod().getLogger().info("PriceCxn initialized");
    }

}
