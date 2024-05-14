package de.alive.preiscxn.core;

import de.alive.api.PriceCxn;
import de.alive.api.cytooxien.PriceCxnItemStack;
import de.alive.api.keybinds.KeybindExecutor;
import de.alive.api.module.Module;
import de.alive.api.module.ModuleLoader;
import de.alive.api.module.PriceCxnModule;
import de.alive.api.networking.Http;
import de.alive.api.networking.cdn.CdnFileHandler;
import de.alive.preiscxn.impl.Version;
import de.alive.preiscxn.impl.cytooxien.CxnListener;
import de.alive.preiscxn.impl.modules.ClasspathModule;
import de.alive.preiscxn.impl.modules.MainModule;
import de.alive.preiscxn.impl.modules.ModuleLoaderImpl;
import de.alive.preiscxn.impl.modules.RemoteModule;
import de.alive.preiscxn.impl.networking.HttpImpl;
import de.alive.preiscxn.impl.networking.cdn.CdnFileHandlerImpl;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.models.addon.annotation.AddonMain;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static de.alive.api.LogPrinter.LOGGER;

@AddonMain
public class PriceCxnAddon extends LabyAddon<PriceCxnConfiguration> {
  public static final Style DEFAULT_TEXT = Style.EMPTY.color(NamedTextColor.GRAY);
  public static final String MOD_NAME = "PriceCxn";

  public static final String MOD_VERSION = Version.MOD_VERSION;

  private final Map<Class<? extends KeybindExecutor>, ?> classKeyBindingMap = new HashMap<>();
  private final Map<?, KeybindExecutor> keyBindingKeybindExecutorMap = new HashMap<>();
  private final ModuleLoader projectLoader;

  private final CxnListener cxnListener;
  private final CdnFileHandler cdnFileHandler;
  private final Http http;

  private PriceCxnItemStack.ViewMode viewMode = PriceCxnItemStack.ViewMode.CURRENT_STACK;

  public PriceCxnAddon(){
    this.http = new HttpImpl();
    this.cdnFileHandler = new CdnFileHandlerImpl(http);
    try {
      Field mod = PriceCxn.class.getDeclaredField("mod");
      mod.setAccessible(true);
      mod.set(null, this);
      mod.setAccessible(false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    LOGGER.info("PriceCxn client created");

    this.projectLoader = new ModuleLoaderImpl();

    this.projectLoader.addModule(new MainModule());

    try {
      cxnListener = new CxnListener();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    this.projectLoader.addModule(new ClasspathModule("de.alive.api"));
    this.projectLoader.addModule(new ClasspathModule("de.alive.scanner.inventory"));

    registerRemoteModule(
            "de.alive.inventory.listener.AuctionHouseListener",
            "Listener.jar",
            Path.of("./downloads/" + "MOD_NAME" + "_modules/cxn.listener.jar"),
            "de.alive.inventory")
            .doOnNext(module1 -> {
              LOGGER.info("Adding module: {}", module1);
              this.projectLoader.addModule(module1);
              this.cxnListener.loadModules(this.projectLoader);

              Set<Class<? extends PriceCxnModule>> classes1 = this.projectLoader.loadInterfaces(PriceCxnModule.class);
              classes1.forEach(aClass -> {
                LOGGER.info("Loading module: {}", aClass);
                try {
                  aClass.getConstructor().newInstance().loadModule();
                  LOGGER.info("Loaded module: {}", aClass);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                         | NoSuchMethodException e) {
                  LOGGER.error("Failed to load module: {}", aClass, e);
                }
              });
            }).subscribe();

  }

  private Mono<Module> registerRemoteModule(String classPath, String remotePath, Path localPath, String primaryPackage) {
    boolean useRemote;
    try {
      Thread.currentThread().getContextClassLoader().loadClass(classPath);
      useRemote = false;
    } catch (Exception e) {
      useRemote = true;
    }

    LOGGER.info("Registering remote module: {} ({}), local path: {}, primary package: {}, use remote: {}",
            classPath, remotePath, localPath, primaryPackage, useRemote);
    return RemoteModule.create(remotePath,
            localPath,
            primaryPackage,
            useRemote);
  }

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.logger().info("Enabled the Addon");
  }

  @Override
  protected Class<PriceCxnConfiguration> configurationClass() {
    return PriceCxnConfiguration.class;
  }
}
