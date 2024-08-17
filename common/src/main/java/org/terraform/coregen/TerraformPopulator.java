package org.terraform.coregen;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.cavepopulators.MasterCavePopulatorDistributor;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.populators.AmethystGeodePopulator;
import org.terraform.populators.OrePopulator;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.StructureBufferDistanceHandler;
import org.terraform.structure.StructureRegistry;
import org.terraform.utils.GenUtils;

import java.util.EnumSet;
import java.util.Random;

public class TerraformPopulator extends BlockPopulator {
	
    private static final OrePopulator[] ORE_POPS = {
            // Ores
            new OrePopulator(Material.DEEPSLATE, config.getInt(TConfig.Option.ORE_DEEPSLATE_CHANCE), config.getInt(TConfig.Option.ORE_DEEPSLATE_VEINSIZE),
                    config.getInt(TConfig.Option.ORE_DEEPSLATE_MAXVEINNUMBER), config.getInt(TConfig.Option.ORE_DEEPSLATE_MINSPAWNHEIGHT), config.getInt(TConfig.Option.ORE_DEEPSLATE_COMMONSPAWNHEIGHT),
                    config.getInt(TConfig.Option.ORE_DEEPSLATE_MAXSPAWNHEIGHT),
                    true),//deepslate
            new OrePopulator(Material.TUFF, config.getInt(TConfig.Option.ORE_TUFF_CHANCE), config.getInt(TConfig.Option.ORE_TUFF_VEINSIZE),
                    config.getInt(TConfig.Option.ORE_TUFF_MAXVEINNUMBER), config.getInt(TConfig.Option.ORE_TUFF_MINSPAWNHEIGHT), config.getInt(TConfig.Option.ORE_TUFF_COMMONSPAWNHEIGHT),
                    config.getInt(TConfig.Option.ORE_TUFF_MAXSPAWNHEIGHT),
                    true),//tuff
            new OrePopulator(Material.COPPER_ORE, config.getInt(TConfig.Option.ORE_COPPER_CHANCE), config.getInt(TConfig.Option.ORE_COPPER_VEINSIZE),
                    config.getInt(TConfig.Option.ORE_COPPER_MAXVEINNUMBER), config.getInt(TConfig.Option.ORE_COPPER_MINSPAWNHEIGHT), config.getInt(TConfig.Option.ORE_COPPER_COMMONSPAWNHEIGHT),
                    config.getInt(TConfig.Option.ORE_COPPER_MAXSPAWNHEIGHT),
                    false),//Space for copper
            new OrePopulator(Material.COAL_ORE,
             config.getInt(TConfig.Option.ORE_COAL_CHANCE),
             config.getInt(TConfig.Option.ORE_COAL_VEINSIZE),
             config.getInt(TConfig.Option.ORE_COAL_MAXVEINNUMBER),
			 config.getInt(TConfig.Option.ORE_COAL_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_COAL_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_COAL_MAXSPAWNHEIGHT),
             false),
            
            new OrePopulator(Material.IRON_ORE,
             config.getInt(TConfig.Option.ORE_IRON_CHANCE),
             config.getInt(TConfig.Option.ORE_IRON_VEINSIZE),
             config.getInt(TConfig.Option.ORE_IRON_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_IRON_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_IRON_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_IRON_MAXSPAWNHEIGHT),
             false),
            
            new OrePopulator(Material.GOLD_ORE,
             config.getInt(TConfig.Option.ORE_GOLD_CHANCE),
             config.getInt(TConfig.Option.ORE_GOLD_VEINSIZE),
             config.getInt(TConfig.Option.ORE_GOLD_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_GOLD_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GOLD_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GOLD_MAXSPAWNHEIGHT),
             false),

            //BADLANDS SPAWNRATE
            new OrePopulator(Material.GOLD_ORE,
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_CHANCE),
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_VEINSIZE),
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_MAXVEINNUMBER),
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_MINSPAWNHEIGHT),
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_COMMONSPAWNHEIGHT),
                    config.getInt(TConfig.Option.ORE_BADLANDSGOLD_MAXSPAWNHEIGHT),
                    false,
                    BiomeBank.BADLANDS,
                    BiomeBank.BADLANDS_CANYON,
                    BiomeBank.BADLANDS_CANYON_PEAK,
                    BiomeBank.BADLANDS_BEACH,
                    BiomeBank.BADLANDS_RIVER),
            
            new OrePopulator(Material.DIAMOND_ORE,
             config.getInt(TConfig.Option.ORE_DIAMOND_CHANCE),
             config.getInt(TConfig.Option.ORE_DIAMOND_VEINSIZE),
             config.getInt(TConfig.Option.ORE_DIAMOND_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_DIAMOND_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_DIAMOND_COMMONSPAWNHEIGHT),
			 config.getInt(TConfig.Option.ORE_DIAMOND_MAXSPAWNHEIGHT),
             false),
            
            
            //Emeralds only spawn in mountainous biomes (except deserts)
            new OrePopulator(Material.EMERALD_ORE,
             config.getInt(TConfig.Option.ORE_EMERALD_CHANCE),
             config.getInt(TConfig.Option.ORE_EMERALD_VEINSIZE),
             config.getInt(TConfig.Option.ORE_EMERALD_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_EMERALD_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_EMERALD_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_EMERALD_MAXSPAWNHEIGHT),
             false,
             BiomeBank.BIRCH_MOUNTAINS,
             BiomeBank.ROCKY_MOUNTAINS,
             BiomeBank.SNOWY_MOUNTAINS,
             BiomeBank.FORESTED_MOUNTAINS,
             BiomeBank.COLD_JAGGED_PEAKS,
             BiomeBank.JAGGED_PEAKS,
             BiomeBank.FORESTED_PEAKS),
            
            
            new OrePopulator(Material.LAPIS_ORE,
             config.getInt(TConfig.Option.ORE_LAPIS_CHANCE),
             config.getInt(TConfig.Option.ORE_LAPIS_VEINSIZE),
             config.getInt(TConfig.Option.ORE_LAPIS_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_LAPIS_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_LAPIS_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_LAPIS_MAXSPAWNHEIGHT),
             false),
            
            new OrePopulator(Material.REDSTONE_ORE,
             config.getInt(TConfig.Option.ORE_REDSTONE_CHANCE),
             config.getInt(TConfig.Option.ORE_REDSTONE_VEINSIZE),
             config.getInt(TConfig.Option.ORE_REDSTONE_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_REDSTONE_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_REDSTONE_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_REDSTONE_MAXSPAWNHEIGHT),
             false),
            
            
            //Non-ores
            new OrePopulator(Material.GRAVEL,
             config.getInt(TConfig.Option.ORE_GRAVEL_CHANCE),
             config.getInt(TConfig.Option.ORE_GRAVEL_VEINSIZE),
             config.getInt(TConfig.Option.ORE_GRAVEL_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_GRAVEL_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GRAVEL_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GRAVEL_MAXSPAWNHEIGHT),
             true),
            
            new OrePopulator(Material.ANDESITE,
             config.getInt(TConfig.Option.ORE_ANDESITE_CHANCE),
             config.getInt(TConfig.Option.ORE_ANDESITE_VEINSIZE),
             config.getInt(TConfig.Option.ORE_ANDESITE_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_ANDESITE_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_ANDESITE_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_ANDESITE_MAXSPAWNHEIGHT),
             true),
            
            new OrePopulator(Material.DIORITE,
             config.getInt(TConfig.Option.ORE_DIORITE_CHANCE),
             config.getInt(TConfig.Option.ORE_DIORITE_VEINSIZE),
             config.getInt(TConfig.Option.ORE_DIORITE_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_DIORITE_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_DIORITE_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_DIORITE_MAXSPAWNHEIGHT),
             true),
            
            new OrePopulator(Material.GRANITE,
             config.getInt(TConfig.Option.ORE_GRANITE_CHANCE),
             config.getInt(TConfig.Option.ORE_GRANITE_VEINSIZE),
             config.getInt(TConfig.Option.ORE_GRANITE_MAXVEINNUMBER),
             config.getInt(TConfig.Option.ORE_GRANITE_MINSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GRANITE_COMMONSPAWNHEIGHT),
             config.getInt(TConfig.Option.ORE_GRANITE_MAXSPAWNHEIGHT),
             true)
    }; 

    private final AmethystGeodePopulator amethystGeodePopulator = new AmethystGeodePopulator(
            config.getInt(TConfig.Option.ORE_AMETHYST_GEODE_SIZE),
            config.getDouble(TConfig.Option.ORE_AMETHYST_CHANCE),
            config.getInt(TConfig.Option.ORE_AMETHYST_MIN_DEPTH),
            config.getInt(TConfig.Option.ORE_AMETHYST_MIN_DEPTH_BELOW_SURFACE));
    public TerraformPopulator(TerraformWorld tw) {
    }
    
    private final MasterCavePopulatorDistributor caveDistributor = new MasterCavePopulatorDistributor();

    @Override
    public void populate(@NotNull org.bukkit.generator.WorldInfo worldInfo, @NotNull java.util.Random random,
                         int chunkX, int chunkZ,
                         @NotNull org.bukkit.generator.LimitedRegion limitedRegion)
    {
        TerraformWorld tw = TerraformWorld.get(worldInfo.getName(),worldInfo.getSeed());
        PopulatorDataAbstract data = new PopulatorDataSpigotAPI(limitedRegion, tw, chunkX, chunkZ);
        this.populate(tw, random, data);
    }

    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
    	random = tw.getHashedRand(571162, data.getChunkX(), data.getChunkZ());
        //ores
        for (OrePopulator ore : ORE_POPS) {
            ore.populate(tw, random, data);
        }
        
        //Amethysts
        amethystGeodePopulator.populate(tw, random, data);

        // Get all biomes in a chunk
        EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);

        boolean canDecorate = StructureBufferDistanceHandler.canDecorateChunk(tw, data.getChunkX(), data.getChunkZ());

        //Small Populators run per block.
        for(int rawX = data.getChunkX()*16; rawX <= data.getChunkX()*16+16; rawX++)
            for(int rawZ = data.getChunkZ()*16; rawZ <= data.getChunkZ()*16+16; rawZ++)
            {
                int surfaceY = GenUtils.getTransformedHeight(data.getTerraformWorld(), rawX, rawZ);
                BiomeBank bank = tw.getBiomeBank(rawX,surfaceY,rawZ);
                banks.add(bank);

                //Don't populate wet stuff in places that aren't wet
                if(!bank.getType().isDry() && data.getType(rawX,surfaceY+1,rawZ) != Material.WATER)
                    continue;
                bank.getHandler().populateSmallItems(tw, random, rawX, surfaceY, rawZ, data);
            }

        //Only decorate disruptive features if the structures allow for them
        if(canDecorate)
            for (BiomeBank bank : banks)
                bank.getHandler().populateLargeItems(tw, random, data);

        
		// Cave populators
        //They will recalculate biomes per block.
		caveDistributor.populate(tw, random, data);

		//Multi-megachunk structures
        for (MultiMegaChunkStructurePopulator spop : StructureRegistry.smallStructureRegistry) {
            if (spop.canSpawn(tw, data.getChunkX(), data.getChunkZ())) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                
                //No async events
                //Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(data.getChunkX()*16+8, data.getChunkZ()*16+8, spop.getClass().getName()));
                
                spop.populate(tw, data);
            }
        }
    }
}
