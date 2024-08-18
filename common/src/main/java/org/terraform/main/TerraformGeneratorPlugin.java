package org.terraform.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.reflection.Post14PrivateFieldHandler;
import org.terraform.reflection.Pre14PrivateFieldHandler;
import org.terraform.reflection.PrivateFieldHandler;
import org.terraform.schematic.SchematicListener;
import org.terraform.structure.StructureRegistry;
import org.terraform.tree.SaplingOverrider;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.bstats.TerraformGeneratorMetricsHandler;
import org.terraform.utils.injection.InjectableObject;
import org.terraform.utils.version.Version;
import org.terraform.watchdog.TfgWatchdogSuppressant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TerraformGeneratorPlugin extends JavaPlugin implements Listener {

    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final @NotNull PrivateFieldHandler privateFieldHandler;
    public static @Nullable NMSInjectorAbstract injector;
    public static TfgWatchdogSuppressant watchdogSuppressant;
    private final HashMap<String, TerraformGenerator> generators = new HashMap<>();

    static {
        PrivateFieldHandler handler;
        try {
            Field.class.getDeclaredField("modifiers");
            handler = new Pre14PrivateFieldHandler();
        } catch(NoSuchFieldException | SecurityException ex) {
            handler = new Post14PrivateFieldHandler();
        }
        privateFieldHandler = handler;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        GenUtils.initGenUtils();
        BlockUtils.initBlockUtils();

        LanguageManager.init(new File(getDataFolder(), getTConfig().LANGUAGE_FILE));
        new TerraformGeneratorMetricsHandler(this); // bStats

        BiomeBank.initSinglesConfig(); //Initiates single biome modes.
        InjectableObject.registerGlobal(this);

        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.stdout("Detected version: " + version + ", number: " + Version.DOUBLE);
        try {
            injector = Version.SupportedVersion.getInjector();
            if(injector == null) throw new ClassNotFoundException();
        } catch(ClassNotFoundException e) {
            logger.stackTrace(e);
            logger.stdout("&cNo support for this version has been made yet!");
        } catch(InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.stackTrace(e);
            logger.stdout("&cSomething went wrong initiating the injector!");
        }

        injector.startupTasks();

        if(config.MISC_SAPLING_CUSTOM_TREES_ENABLED) {
            Bukkit.getPluginManager().registerEvents(new SaplingOverrider(), this);
        }

        StructureRegistry.init();
    }


    @SuppressWarnings("unused")
    @EventHandler
    public void onWorldInit(@NotNull WorldInitEvent event) {
        if(event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.stdout("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
            TerraformWorld tw = TerraformWorld.forceOverrideSeed(event.getWorld());
            if(injector.attemptInject(event.getWorld())) {
                INJECTED_WORLDS.add(event.getWorld().getName());
                tw.minY = injector.getMinY();
                tw.maxY = injector.getMaxY();

                logger.stdout("&aInjection success! Proceeding with generation.");

            } else {
                logger.stdout("&cInjection failed.");
            }
        }
    }

    public TConfig getTConfig() {
        TConfig config = new TConfig();
        try {
            config = config.load(new File(getDataFolder(), "config.yml"));
        }
        catch(IOException e) {
            getLogger().severe("Couldn't load config.yml!: " + e.getMessage() + " -- Using defaults");
        }

        return config;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return generators.computeIfAbsent(
                worldName + '#' + id,
                k -> new TerraformGenerator(this, getTConfig())
        );
    }
}
