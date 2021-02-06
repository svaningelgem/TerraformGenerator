package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.Random;

public class JungleHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.JUNGLE;
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
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.PODZOL, 5),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
    	//Almost everything about jungle population is highly disruptive.
    	//Only grass spawning remains here. Mushrooms and everything else go to
    	//populateLargeItems
    	
    	 for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
             for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                 int y = GenUtils.getHighestGround(data, x, z);

                 // Generate grass
                 if (data.getBiome(x, z) == getBiome() &&
                         BlockUtils.isDirtLike(data.getType(x, y, z))) {
                     if (data.getType(x, y + 1, z).isAir() && GenUtils.chance(2, 3)) {
                         if (random.nextBoolean()) {
                             data.setType(x, y + 1, z, GenUtils.weightedRandomMaterial(random, Material.GRASS, 5, BlockUtils.pickFlower(), 1));
                         } else {
                             if (data.getType(x, y + 2, z).isAir())
                                 BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                         }
                     }
                 }
             }
    	 }
    }

    private void createBush(PopulatorDataAbstract data, float noiseIncrement, int oriX, int oriY, int oriZ) {
        // noiseIncrement is always < 0.5 and > 0
        float rX = 2.5f + (float) (noiseIncrement * Math.random());
        float rY = 1.3f + (float) (noiseIncrement * Math.random());
        float rZ = 2.5f + (float) (noiseIncrement * Math.random());

        SimpleBlock base = new SimpleBlock(data, oriX, oriY, oriZ);

        for (int x = -Math.round(rX); x <= rX; x++) {
            for (int y = -Math.round(rY); y <= rY; y++) {
                for (int z = -Math.round(rZ); z <= rZ; z++) {
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2) +
                            Math.pow(y, 2) / Math.pow(rY, 2) +
                            Math.pow(z, 2) / Math.pow(rZ, 2);

                    if (equationResult <= 1) {
                        SimpleBlock block = base.getRelative(x, y + 1, z);

                        // Skip random leaves, less leaves when close to center.
                        if (Math.random() < equationResult - 0.5)
                            continue;

                        if (!block.getType().isSolid()) {
                            block.setType(Material.JUNGLE_LEAVES);
                        }
                    }
                }
            }
        }

        if (Math.random() > 0.3)
            base.setType(Material.JUNGLE_LOG);
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        FastNoise groundWoodNoise = new FastNoise((int) (tw.getSeed() * 12));
        groundWoodNoise.SetNoiseType(NoiseType.SimplexFractal);
        groundWoodNoise.SetFractalOctaves(3);
        groundWoodNoise.SetFrequency(0.07f);

        FastNoise groundLeavesNoise = new FastNoise((int) (tw.getSeed() * 2));
        groundLeavesNoise.SetNoiseType(NoiseType.SimplexFractal);
        groundLeavesNoise.SetFrequency(0.07f);

        Vector2f[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);

        for (Vector2f tree : bigTrees) {
            int treeY = GenUtils.getHighestGround(data, (int) tree.x, (int) tree.y);

            if(data.getBiome((int) tree.x, (int) tree.y) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType((int) tree.x, treeY, (int) tree.y))) {
                new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG).build(tw, data, (int) tree.x, treeY, (int) tree.y);
            }
        }

        Vector2f[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 9);

        for (Vector2f tree : trees) {
            int treeY = GenUtils.getHighestGround(data, (int) tree.x, (int) tree.y);

            if (data.getBiome((int) tree.x, (int) tree.y) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType((int) tree.x, treeY, (int) tree.y))) {
                TreeDB.spawnSmallJungleTree(tw, data, (int) tree.x, treeY, (int) tree.y);
            }
        }

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);

                // Fades noise, see below
                int distanceToSeaOrMountain = Math.min(y - TerraformGenerator.seaLevel, TerraformGenerator.minMountainLevel - y);

                if (distanceToSeaOrMountain > 0) {
                    float leavesNoiseValue = groundLeavesNoise.GetNoise(x, z);
                    float groundWoodNoiseValue = groundWoodNoise.GetNoise(x, z);

                    // If close to mountain level or sea level (=river), fade noise linearly
                    // so that beaches are clear.
                    if (distanceToSeaOrMountain <= 4) {
                        leavesNoiseValue -= -0.25f * distanceToSeaOrMountain + 1;
                        groundWoodNoiseValue -= -0.25f * distanceToSeaOrMountain + 1;
                    }

                    // Generate some ground leaves
                    if (leavesNoiseValue > -0.12
                            && Math.random() > 0.85)
                        createBush(data, leavesNoiseValue, x, y, z);
                    else if (GenUtils.chance(1, 95)) // Some random ones where there is no noise.
                        createBush(data, 0, x, y, z);

                    // Generate random wood, or "roots" on the ground
                    if (groundWoodNoiseValue > 0.3)
                        data.setType(x, y + 1, z, Material.JUNGLE_WOOD);
                }

                // Generate mushrooms
                if (data.getBiome(x, z) == getBiome() &&
                        BlockUtils.isDirtLike(data.getType(x, y, z))) {
                    if (data.getType(x, y + 1, z) == Material.JUNGLE_WOOD
                            && data.getType(x, y + 2, z).isAir()
                            && GenUtils.chance(2, 9)) {
                        data.setType(x, y + 2, z, GenUtils.randMaterial(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM));
                    }
                }
            }
        }
    }
}
