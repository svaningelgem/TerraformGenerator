package org.terraform.biome.river;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class JungleRiverHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.RIVER;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }


    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        boolean growsKelp = random.nextBoolean();
        if(surfaceY >= TerraformGenerator.seaLevel) //Don't apply to dry land
            return;

        //Set ground near sea level to sand
        if(surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }else if(surfaceY >= TerraformGenerator.seaLevel - 4) {
            if(random.nextBoolean())
                data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) return;

        generateLilyPad(world, random, data, rawX, rawZ, surfaceY);

        RiverHandler.riverVegetation(world, random, data, rawX, surfaceY, rawZ);

        // Generate clay
        if (GenUtils.chance(random, config.getInt(TConfig.Option.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND), 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
    }

    /**
     * Generate random lily pads in jungle rivers
     * Deeper waters -> less pads. Noise makes sure they are in groups
     */
    public static void generateLilyPad(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int z, int highestGround) {
        if (GenUtils.chance(random, 1, (int) (getLilyPadNoise(tw, x, z) * 7 + Math.pow(TerraformGenerator.seaLevel - highestGround, 3) + 18))) {
            data.setType(x, TerraformGenerator.seaLevel + 1, z, Material.LILY_PAD);
        }
    }

    public static void generateKelp(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        for (int ny = y; ny < TerraformGenerator.seaLevel - GenUtils.randInt(random, 0, 2); ny++) {
            data.setType(x, ny, z, Material.KELP_PLANT);
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		// TODO Auto-generated method stub
		
	}

	public static double getLilyPadNoise(TerraformWorld tw, int x, int z) {
        FastNoise lilyPadNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheEntry.BIOME_JUNGLE_LILYPADS,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 2));
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFrequency(0.05f);

                    return n;
                });

        return lilyPadNoise.GetNoise(x, z);
    }

}
