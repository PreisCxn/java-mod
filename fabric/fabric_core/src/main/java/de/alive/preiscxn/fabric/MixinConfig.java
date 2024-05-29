package de.alive.preiscxn.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import org.objectweb.asm.tree.ClassNode;

import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfig implements IMixinConfigPlugin {

    private static final String VERSION;

    static {
        String tempVersion;
        try{
            SharedConstants.createGameVersion();
            tempVersion = SharedConstants.getGameVersion().getName().replace(".", "_");
            LoggerFactory.getLogger(MixinConfig.class).info("Loaded Minecraft version for pricecxn mixins: {}", tempVersion);
        }catch(Exception e){
            throw new RuntimeException("Failed to get Minecraft version", e);
        }
        VERSION = tempVersion;
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "de.alive.preiscxn.mixins.fabric.v" + VERSION + ".refmap.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return mixinClassName.contains(VERSION);
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

}
