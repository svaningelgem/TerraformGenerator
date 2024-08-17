package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.populators.AnimalPopulator;
import org.terraform.utils.version.OneTwentyFiveBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class TerraformAnimalPopulator extends BlockPopulator {

    private final TerraformWorld tw;

    private static final AnimalPopulator[] ANIMAL_POPULATORS = {
    		null, //Slot for goat
            null, //Slot for armadillo
    		
            new AnimalPopulator(EntityType.PIG, config.getInt(TConfig.Option.ANIMALS_PIG_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_PIG_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_PIG_CHANCE), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH,BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.COW, config.getInt(TConfig.Option.ANIMALS_COW_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_COW_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_COW_CHANCE), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.SHEEP, config.getInt(TConfig.Option.ANIMALS_SHEEP_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_SHEEP_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_SHEEP_CHANCE), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.CHICKEN, config.getInt(TConfig.Option.ANIMALS_CHICKEN_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_CHICKEN_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_CHICKEN_CHANCE), false, BiomeBank.BLACK_OCEAN, BiomeBank.MUSHROOM_ISLANDS, BiomeBank.MUSHROOM_BEACH, BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.FROZEN_OCEAN,
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_COLD_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.DEEP_WARM_OCEAN, BiomeBank.SWAMP, BiomeBank.DESERT, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON),
            
            new AnimalPopulator(EntityType.HORSE, config.getInt(TConfig.Option.ANIMALS_HORSE_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_HORSE_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_HORSE_CHANCE), true, BiomeBank.PLAINS, BiomeBank.SAVANNA),
            
            new AnimalPopulator(EntityType.DONKEY, config.getInt(TConfig.Option.ANIMALS_DONKEY_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_DONKEY_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_DONKEY_CHANCE), true, BiomeBank.PLAINS, BiomeBank.SAVANNA),
            
            new AnimalPopulator(EntityType.RABBIT, config.getInt(TConfig.Option.ANIMALS_RABBIT_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_RABBIT_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_RABBIT_CHANCE), true, BiomeBank.DESERT, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.ROCKY_BEACH,
                    BiomeBank.SNOWY_WASTELAND),
            
            new AnimalPopulator(EntityType.POLAR_BEAR, config.getInt(TConfig.Option.ANIMALS_POLAR_BEAR_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_POLAR_BEAR_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_POLAR_BEAR_CHANCE), true, BiomeBank.ICE_SPIKES, BiomeBank.SNOWY_TAIGA, BiomeBank.ICY_BEACH, BiomeBank.SNOWY_WASTELAND),
            
            new AnimalPopulator(EntityType.PANDA, config.getInt(TConfig.Option.ANIMALS_PANDA_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_PANDA_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_PANDA_CHANCE), true, BiomeBank.BAMBOO_FOREST),

            new AnimalPopulator(EntityType.FOX, config.getInt(TConfig.Option.ANIMALS_FOX_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_FOX_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_FOX_CHANCE), true, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA),

            new AnimalPopulator(EntityType.LLAMA, config.getInt(TConfig.Option.ANIMALS_LLAMA_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_LLAMA_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_LLAMA_CHANCE), true, BiomeBank.SAVANNA, BiomeBank.ROCKY_MOUNTAINS),

            new AnimalPopulator(EntityType.PARROT, config.getInt(TConfig.Option.ANIMALS_PARROT_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_PARROT_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_PARROT_CHANCE), true, BiomeBank.JUNGLE),

            new AnimalPopulator(EntityType.OCELOT, config.getInt(TConfig.Option.ANIMALS_OCELOT_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_OCELOT_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_OCELOT_CHANCE), true, BiomeBank.JUNGLE, BiomeBank.BAMBOO_FOREST),

            new AnimalPopulator(EntityType.WOLF, config.getInt(TConfig.Option.ANIMALS_WOLF_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_WOLF_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_WOLF_CHANCE), true, BiomeBank.FOREST, BiomeBank.TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.DARK_FOREST),

            new AnimalPopulator(EntityType.TURTLE, config.getInt(TConfig.Option.ANIMALS_TURTLE_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_TURTLE_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_TURTLE_CHANCE), true, BiomeBank.SANDY_BEACH),

            new AnimalPopulator(EntityType.DOLPHIN, config.getInt(TConfig.Option.ANIMALS_DOLPHIN_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_DOLPHIN_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_DOLPHIN_CHANCE), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
    
            new AnimalPopulator(EntityType.COD, config.getInt(TConfig.Option.ANIMALS_COD_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_COD_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_COD_CHANCE), true, BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.SQUID, config.getInt(TConfig.Option.ANIMALS_SQUID_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_SQUID_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_SQUID_CHANCE), true,
                    BiomeBank.FROZEN_OCEAN, BiomeBank.DEEP_FROZEN_OCEAN,
                    BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN,
                    BiomeBank.BLACK_OCEAN,BiomeBank.DEEP_BLACK_OCEAN,
                    BiomeBank.OCEAN, BiomeBank.DEEP_OCEAN, 
                    BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN,
                    BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN,
                    BiomeBank.RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.JUNGLE_RIVER)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.SALMON, config.getInt(TConfig.Option.ANIMALS_SALMON_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_SALMON_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_SALMON_CHANCE), true,
                    BiomeBank.COLD_OCEAN, BiomeBank.DEEP_COLD_OCEAN, 
                    BiomeBank.FROZEN_OCEAN, BiomeBank.DEEP_COLD_OCEAN, 
                    BiomeBank.RIVER, BiomeBank.FROZEN_RIVER)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.PUFFERFISH, config.getInt(TConfig.Option.ANIMALS_PUFFERFISH_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_PUFFERFISH_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_PUFFERFISH_CHANCE), true, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.DEEP_WARM_OCEAN)
            .setAquatic(true),
            
            new AnimalPopulator(EntityType.TROPICAL_FISH, config.getInt(TConfig.Option.ANIMALS_TROPICALFISH_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_TROPICALFISH_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_TROPICALFISH_CHANCE), true, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.CORAL_REEF_OCEAN, BiomeBank.DEEP_LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN)
            .setAquatic(true),

            new AnimalPopulator(EntityType.MUSHROOM_COW, config.getInt(TConfig.Option.ANIMALS_MOOSHROOM_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_MOOSHROOM_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_MOOSHROOM_CHANCE), true, BiomeBank.MUSHROOM_BEACH, BiomeBank.MUSHROOM_ISLANDS),
    };
    
    public TerraformAnimalPopulator(TerraformWorld tw) {
        this.tw = tw;
        if(Version.isAtLeast(17)) {
        	ANIMAL_POPULATORS[0] = new AnimalPopulator(EntityType.valueOf("GOAT"), config.getInt(TConfig.Option.ANIMALS_GOAT_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_GOAT_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_GOAT_CHANCE), true, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS);
        }
        if(Version.isAtLeast(20.5)) {
            ANIMAL_POPULATORS[1] = new AnimalPopulator(OneTwentyFiveBlockHandler.ARMADILLO, config.getInt(TConfig.Option.ANIMALS_ARMADILLO_MINHERDSIZE), config.getInt(TConfig.Option.ANIMALS_ARMADILLO_MAXHERDSIZE),
                    config.getInt(TConfig.Option.ANIMALS_ARMADILLO_CHANCE), true, BiomeBank.SAVANNA, BiomeBank.SHATTERED_SAVANNA, BiomeBank.BADLANDS, BiomeBank.BADLANDS_CANYON);
        }
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
       
    	PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

    	for (AnimalPopulator pop : ANIMAL_POPULATORS) {
        	if(pop == null) continue;
            if (pop.canSpawn(tw.getHashedRand(chunk.getX(), pop.hashCode(), chunk.getZ()))) {
                pop.populate(tw, tw.getHashedRand(chunk.getX(), 111+pop.hashCode(), chunk.getZ()), data);
            }
        }
    }
}
