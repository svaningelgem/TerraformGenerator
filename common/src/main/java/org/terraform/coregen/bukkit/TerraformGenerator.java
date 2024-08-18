package org.terraform.coregen.bukkit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.ChunkCacheLoader;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.data.DudChunkData;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TLogger;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.injection.InjectableObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class TerraformGenerator extends ChunkGenerator implements InjectableObject {
    private final LoadingCache<ChunkCache, ChunkCache> CHUNK_CACHE;
    private final LoadingCache<ChunkCache, EnumSet<BiomeBank>> biomeQueryCache;

    private final @NotNull TConfig config;
    private final @NotNull TerraformGeneratorPlugin plugin;
    private final @NotNull TLogger logger;
    private final @NotNull HeightMap heightMap;

    // Explode if a read is attempted. Transform Handlers are not supposed to read.
    private final DudChunkData DUD = new DudChunkData();

    public TerraformGenerator(@NotNull TerraformGeneratorPlugin plugin) {
        this(plugin, plugin.getTConfig());
    }

    public TerraformGenerator(@NotNull TerraformGeneratorPlugin plugin, @NotNull TConfig config) {
        this.plugin = register(plugin);
        this.config = register(config);
        register(this);
        this.logger = create(TLogger.class);
        this.heightMap = create(HeightMap.class);

        //Initialize chunk cache based on config size
        CHUNK_CACHE = CacheBuilder.newBuilder()
                .maximumSize(config.DEVSTUFF_CHUNKCACHE_SIZE).build(new ChunkCacheLoader());

        //Initialize biome query cache based on config size
        biomeQueryCache = CacheBuilder.newBuilder()
                .maximumSize(config.DEVSTUFF_CHUNKBIOMES_SIZE)
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
    }

    /**
     * Refers to raw X and raw Z (block coords). NOT chunk coords.
     */
    public @NotNull ChunkCache getCache(TerraformWorld tw, int x, int z) {
        ChunkCache cache = new ChunkCache(tw, x, 0, z);
        //Note how it DOES NOT initInternalCache here
        //Cos this is the damn key
        //Don't fucking run calculations here
        try {
            return CHUNK_CACHE.get(cache);
        } catch(ExecutionException e) {
            logger.stackTrace(e);
            e.getCause().printStackTrace();
            cache.initInternalCache();
            return cache;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isParallelCapable() {
        return true;
    }

    /**
     * EVERY STAGE IS DONE INSIDE HERE FOR A REASON.
     * The cache MAY invalidate between stages, making it infeasible to even
     * bother splitting it up.
     * <br>
     * It was originally split into 4 phases for readability's sake,
     * but there's really no point - it's faster to iterate x/z ONCE here,
     * and avoid all the cache and other nonsense issues.
     * <br>
     * It's that or throw ChunkCaches into ConcurrentHashMaps and
     * then flush it into the CHUNK_CACHE after generation, which is
     * dumb. That's dumb.
     */
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random dontCareRandom, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();

        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(), worldInfo.getSeed());
        ChunkCache cache = getCache(tw, chunkX * 16, chunkZ * 16);

        //For transformation ONLY
        Random transformRandom = tw.getHashedRand(chunkX, chunkZ, 31278);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double height = heightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x, z, (short) height);

                //Fill stone up to the world height. Differentiate between deepslate or not.
                for(int y = (int) height; y >= TerraformGeneratorPlugin.injector.getMinY(); y--) {
                    Material stoneType = Material.STONE;
                    if(y < 0)
                        stoneType = Material.DEEPSLATE;
                    else if(y <= 2)
                        stoneType = GenUtils.randMaterial(Material.DEEPSLATE, Material.STONE);

                    //Set stone if a cave CANNOT be carved here
                    if(!tw.noiseCaveRegistry.canNoiseCarve(rawX, y, rawZ, height))
                        chunkData.setBlock(x, y, z, stoneType);

                }

                //PERFORM SURFACE AND CAVE CARVING
                BiomeBank bank = tw.getBiomeBank(rawX, (int) height, rawZ);
                int index = 0;
                Material[] crust = bank.getHandler().getSurfaceCrust(dontCareRandom);
                while(index < crust.length) {
                    chunkData.setBlock(x, (int) (height - index), z, crust[index]);
                    index++;
                }
                //Water for below certain heights
                for(int y = (int) (height + 1); y <= getSeaLevel(); y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }

                //Carve caves HERE.
                boolean mustUpdateHeight = true;
                for(int y = (int) height; y > TerraformGeneratorPlugin.injector.getMinY(); y--) {
                    if(tw.noiseCaveRegistry.canGenerateCarve(rawX, y, rawZ, height)
                            || !chunkData.getType(x, y, z).isSolid()) {
                        chunkData.setBlock(x, y, z, Material.CAVE_AIR);
                        if(mustUpdateHeight)
                            cache.writeTransformedHeight(x, z, (short) (y - 1));
                    } else mustUpdateHeight = false;
                }

                //Transform height AFTER sea level is written.
                //Transformed below-sea areas are not supposed to be water.
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();
                if(transformHandler != null)
                    transformHandler.transformTerrain(cache, tw, transformRandom, chunkData, x, z, chunkX, chunkZ);

                //After this whole song and dance, place bedrock
                chunkData.setBlock(x, TerraformGeneratorPlugin.injector.getMinY(), z, Material.BEDROCK);

                //Up till y = minY+HEIGHT_MAP_BEDROCK_HEIGHT
                for(int i = 1; i < config.HEIGHT_MAP_BEDROCK_HEIGHT; i++) {
                    if(GenUtils.chance(dontCareRandom, config.HEIGHT_MAP_BEDROCK_DENSITY, 100))
                        chunkData.setBlock(x, TerraformGeneratorPlugin.injector.getMinY() + i, z, Material.BEDROCK);
                    else
                        break;
                }
            }
        }

    }

    /**
     * Responsible for setting surface biome blocks and biomeTransforms
     */
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random dontCareRandom, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    //This method ONLY fills transformedHeight with meaningful values,
    // and writes nothing.
    public void buildFilledCache(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull ChunkCache cache) {
        //TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog(); don't unnecessarily tick this shit

        //Ensure that this shit is the same as the one in generateSurface
        Random random = tw.getHashedRand(chunkX, chunkZ, 31278);
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = heightMap.getPreciseHeight(tw, rawX, rawZ); //bank.getHandler().calculateHeight(tw, rawX, rawZ);
                cache.writeTransformedHeight(x, z, (short) preciseHeight);

                //Carve caves
                for(int y = (int) preciseHeight; y >= TerraformGeneratorPlugin.injector.getMinY(); y--)
                    //Set stone if a cave CANNOT be carved here
                    //Check canNoiseCarve because carver caves may expose
                    //noise caves below, which contribute to height changes
                    if(tw.noiseCaveRegistry.canGenerateCarve(rawX, y, rawZ, preciseHeight)
                            || tw.noiseCaveRegistry.canNoiseCarve(rawX, y, rawZ, preciseHeight))
                        cache.writeTransformedHeight(x, z, (short) (y - 1));
                    else break;

                //Apply biome transforms to get real height
                BiomeBank bank = tw.getBiomeBank(rawX, (int) preciseHeight, rawZ);
                BiomeHandler transformHandler = bank.getHandler().getTransformHandler();

                if(transformHandler != null)
                    transformHandler.transformTerrain(cache, tw, random, DUD, x, z, chunkX, chunkZ);
            }
        }
    }

    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0, heightMap.getBlockHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return new ArrayList<>() {{
            add(new TerraformPopulator(tw));
            add(new TerraformBukkitBlockPopulator(tw));
        }};
    }

    //Do exactly 0 of this, TFG now handles ALL of it.
    public boolean shouldGenerateNoise() {
        return false;
    }

    public boolean shouldGenerateSurface() {
        return false;
    }

    public boolean shouldGenerateBedrock() {
        return false;
    }

    public boolean shouldGenerateCaves() {
        return false;
    }

    public boolean shouldGenerateDecorations() {
        return false;
    }

    public boolean shouldGenerateMobs() {
        return false;
    }

    //This is true as StructureManager is now being overridden.
    public boolean shouldGenerateStructures() {
        return true;
    }

    public int getSeaLevel() {
        return config.HEIGHT_MAP_SEA_LEVEL;
    }
}
