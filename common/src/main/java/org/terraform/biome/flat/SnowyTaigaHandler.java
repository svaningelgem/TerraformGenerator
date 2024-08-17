package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Snowable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class SnowyTaigaHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SNOWY_TAIGA;
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
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.DIRT, 3, Material.PODZOL, 2),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        if (data.getType(rawX, surfaceY, rawZ) == Material.DIRT) {
            if (GenUtils.chance(random, 1, 20)) {
                data.setType(rawX, surfaceY + 1, rawZ, Material.DEAD_BUSH);
                if (random.nextBoolean()) {
                    data.setType(rawX, surfaceY + 1, rawZ, Material.ALLIUM);
                }
            }
        }
        if (data.getType(rawX, surfaceY + 1, rawZ) == Material.AIR
                && GenUtils.isGroundLike(data.getType(rawX, surfaceY, rawZ))) {
            data.setType(rawX, surfaceY + 1, rawZ, Material.SNOW);
            if (data.getBlockData(rawX, surfaceY, rawZ) instanceof Snowable snowable) {
                snowable.setSnowy(true);
                data.setBlockData(rawX, surfaceY, rawZ, snowable);
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 11);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                // Rarely spawn huge taiga trees
                if (TConfigOption.TREES_TAIGA_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 1, 20)) {
                    FractalTypes.Tree.TAIGA_BIG.build(tw, new SimpleBlock(data, sLoc.getX(),sLoc.getY(),sLoc.getZ()), (b) -> b.getFractalLeaves().setSnowy(true));
                    defrostAndReplacePodzol(
                    		tw.getHashedRand(sLoc.getX(),sLoc.getY(),sLoc.getZ()).nextInt(9999),
                    		2.5f,
                    		new SimpleBlock(data,sLoc.getX(),sLoc.getY()-1,sLoc.getZ()));
                }else { // Normal trees
                    FractalTypes.Tree.TAIGA_SMALL.build(tw, new SimpleBlock(data, sLoc.getX(),sLoc.getY(),sLoc.getZ()), (b) -> b.getFractalLeaves().setSnowy(true));
                    defrostAndReplacePodzol(
                    		tw.getHashedRand(sLoc.getX(),sLoc.getY(),sLoc.getZ()).nextInt(9999),
                    		1.5f,
                    		new SimpleBlock(data,sLoc.getX(),sLoc.getY()-1,sLoc.getZ()));
                }
            }
        }
	}
    /**
     * Replaces the highest dirt-like blocks with a noise-fuzzed 
     * circle of Podzol. Fuzzes the edges.
     */
    public static void defrostAndReplacePodzol(int seed, float radius, SimpleBlock base) {
    	if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randMaterial(new Random(seed), Material.PODZOL));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.13f);
        Random rand = new Random(seed);
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                rel = rel.getGround();
                if(!BlockUtils.isDirtLike(rel.getType()))
                	continue;
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                double noiseVal = Math.abs(noise.GetNoise(rel.getX(), rel.getZ()));
                if (equationResult <= 1.0+noiseVal) {
                    //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){          
                    if(equationResult * 4 > 0.7+noiseVal) {
                    	if(rand.nextBoolean()) {
                    		rel.setType(Material.PODZOL);
                    		rel.getRelative(0,1,0).lsetType(Material.AIR);
                    	}
                    }else {
                    	rel.setType(Material.PODZOL);
                		rel.getRelative(0,1,0).lsetType(Material.AIR);
                    }
                }
            }
        }
    }

	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ICY_BEACH;
	}
	
	@Override
    public BiomeBank getRiverType() {
    	return BiomeBank.FROZEN_RIVER;
    }
}
