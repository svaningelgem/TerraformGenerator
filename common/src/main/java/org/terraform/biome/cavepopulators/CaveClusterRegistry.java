package org.terraform.biome.cavepopulators;

import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

public enum CaveClusterRegistry {
	LUSH(
			9527213,
			config.getInt(TConfig.Option.BIOME_CAVE_LUSHCLUSTER_SEPARATION),
			config.getFloat(TConfig.Option.BIOME_CAVE_LUSHCLUSTER_MAXPERTUB)
			),
	DRIPSTONE(
			5902907,
			config.getInt(TConfig.Option.BIOME_CAVE_DRIPSTONECLUSTER_SEPARATION),
			config.getFloat(TConfig.Option.BIOME_CAVE_DRIPSTONECLUSTER_MAXPERTUB)
			),
	CRYSTALLINE(
			4427781,
			config.getInt(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_SEPARATION),
			config.getFloat(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_MAXPERTUB)
			),
    FLUID(
            79183628,
            40,
            0.2f
    ),
	;
	
	final int hashSeed;
	final int separation;
	final float pertub;
	CaveClusterRegistry(int hashSeed, int separation, float pertub){
		this.hashSeed = hashSeed;
		this.separation = separation;
		this.pertub = pertub;
	}
	
	public @NotNull AbstractCaveClusterPopulator getPopulator(@NotNull Random random) {
        return switch(this) {
            case LUSH -> new LushClusterCavePopulator(
                    GenUtils.randInt(random,
                            config.getInt(TConfig.Option.BIOME_CAVE_LUSHCLUSTER_MINSIZE),
                            config.getInt(TConfig.Option.BIOME_CAVE_LUSHCLUSTER_MAXSIZE)),
                    false);
            case DRIPSTONE -> new DripstoneClusterCavePopulator(
                    GenUtils.randInt(random,
                            config.getInt(TConfig.Option.BIOME_CAVE_DRIPSTONECLUSTER_MINSIZE),
                            config.getInt(TConfig.Option.BIOME_CAVE_DRIPSTONECLUSTER_MAXSIZE)));
            case CRYSTALLINE -> new CrystallineClusterCavePopulator(
                    GenUtils.randInt(random,
                            config.getInt(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE),
                            config.getInt(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE)));
            case FLUID -> new CaveFluidClusterPopulator(
                    GenUtils.randInt(random,
                            config.getInt(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE),
                            config.getInt(TConfig.Option.BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE)));
        };

    }

	public int getHashSeed() {
		return hashSeed;
	}

	public int getSeparation() {
		return separation;
	}

	public float getPertub() {
		return pertub;
	}
}
