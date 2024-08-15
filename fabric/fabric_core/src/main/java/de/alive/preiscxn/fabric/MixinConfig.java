package de.alive.preiscxn.fabric;

import com.google.gson.JsonParseException;
import net.minecraft.MinecraftVersion;
import net.minecraft.util.JsonHelper;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class MixinConfig implements IMixinConfigPlugin {

    private static String VERSION;
    private static final Logger LOGGER = LoggerFactory.getLogger(MixinConfig.class);

    @Override
    public void onLoad(String mixinPackage) {
        VERSION = create().replace(".", "_");;
    }

    @Override
    public String getRefMapperConfig() {
        return "de.alive.preiscxn.mixins.fabric.v" + VERSION + ".refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean contains = mixinClassName.contains(".v" + VERSION + ".");
        LOGGER.info("Mixin {} for {} is {} with {}", mixinClassName, targetClassName, contains ? "enabled" : "disabled", VERSION);
        return contains;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    public static String create() {
        try {
            InputStream inputStream = MinecraftVersion.class.getResourceAsStream("/version.json");

            String var10;
            label63: {
                String var2;
                try {
                    if (inputStream == null) {
                        LOGGER.warn("Missing version information!");
                        var10 = "1_21_1";
                        break label63;
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                    try {
                        var2 = JsonHelper.deserialize(inputStreamReader).get("name").getAsString();
                    } catch (Throwable var6) {
                        try {
                            inputStreamReader.close();
                        } catch (Throwable var5) {
                            var6.addSuppressed(var5);
                        }

                        throw var6;
                    }

                    inputStreamReader.close();
                } catch (Throwable var7) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var4) {
                            var7.addSuppressed(var4);
                        }
                    }

                    throw var7;
                }

                inputStream.close();

                return var2;
            }

            return var10;
        } catch (JsonParseException | IOException var8) {
            throw new IllegalStateException("Game version information is corrupt", var8);
        }
    }
}