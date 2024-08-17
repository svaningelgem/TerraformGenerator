package org.terraform.main;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.ChunkCacheLoader;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.heights.HeightMap;
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
import org.terraform.utils.version.Version;
import org.terraform.watchdog.TfgWatchdogSuppressant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TerraformGeneratorPlugin extends JavaPlugin implements Listener {

    public static TLogger logger;
    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final @NotNull PrivateFieldHandler privateFieldHandler;
    public static @Nullable NMSInjectorAbstract injector;
    private static TerraformGeneratorPlugin instance;
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
        instance = this;

        LanguageManager.init(new File(getDataFolder(), getTConfig().LANGUAGE_FILE));

        //Initiate the height map flat radius value
        HeightMap.spawnFlatRadiusSquared = defaultConfig.getInt(TConfig.HEIGHT_MAP_SPAWN_FLAT_RADIUS);
        if(HeightMap.spawnFlatRadiusSquared > 0) HeightMap.spawnFlatRadiusSquared *= HeightMap.spawnFlatRadiusSquared;

        BiomeBank.initSinglesConfig(); //Initiates single biome modes.

        //Initialize chunk cache based on config size
        TerraformGenerator.CHUNK_CACHE =
                CacheBuilder.newBuilder()
                        .maximumSize(defaultConfig.getInt(TConfig.DEVSTUFF_CHUNKCACHE_SIZE)).build(new ChunkCacheLoader());

        //Initialize biome query cache based on config size
        GenUtils.biomeQueryCache = CacheBuilder.newBuilder()
                .maximumSize(defaultConfig.getInt(TConfig.DEVSTUFF_CHUNKBIOMES_SIZE))
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull EnumSet<BiomeBank> load(@NotNull ChunkCache key) {
                        EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);
                        int gridX = key.chunkX * 16;
                        int gridZ = key.chunkZ * 16;
                        for(int x = gridX; x < gridX + 16; x++) {
                            for(int z = gridZ; z < gridZ + 16; z++) {
                                BiomeBank bank = key.tw.getBiomeBank(x, z);
                                if(!banks.contains(bank)) banks.add(bank);
                            }
                        }
                        return banks;
                    }
                });

        logger = new TLogger();
        watchdogSuppressant = new TfgWatchdogSuppressant();
        new TerraformGeneratorMetricsHandler(this); //bStats

        TerraformGenerator.updateSeaLevelFromConfig();
        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.stdout("Detected version: " + version + ", number: " + Version.DOUBLE);
        try {
            injector = Version.SupportedVersion.getInjector();
            if(injector == null) throw new ClassNotFoundException();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            logger.stdout("&cNo support for this version has been made yet!");
        } catch(InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            logger.stdout("&cSomething went wrong initiating the injector!");
        }

        injector.startupTasks();

        if(config.getBoolean(TConfig.MISC_SAPLING_CUSTOM_TREES_ENABLED)) {
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

    private TConfig getTConfig() {
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
