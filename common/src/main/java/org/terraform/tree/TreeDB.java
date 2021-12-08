package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class TreeDB {
    private static final FractalTypes.Tree[] FRACTAL_CORAL_TYPES = {
            FractalTypes.Tree.FIRE_CORAL,
            FractalTypes.Tree.BRAIN_CORAL,
            FractalTypes.Tree.TUBE_CORAL,
            FractalTypes.Tree.HORN_CORAL,
            FractalTypes.Tree.BUBBLE_CORAL
    };
    
    /**
     * Spawns an Azalea tree, complete with rooted dirt.
     * @param base
     */
    public static void spawnAzalea(Random random, TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
    	FractalTreeBuilder builder = new FractalTreeBuilder(FractalTypes.Tree.AZALEA_TOP);
    	builder.build(tw, data, x, y, z);
        
    	SimpleBlock rooter = new SimpleBlock(data,x,y-1,z);
    	rooter.setType(OneOneSevenBlockHandler.ROOTED_DIRT);
    	rooter = rooter.getRelative(0,-1,0);
    	
    	while(!BlockUtils.isAir(rooter.getType())) {
    		rooter.setType(OneOneSevenBlockHandler.ROOTED_DIRT);
    		for(BlockFace face:BlockUtils.xzPlaneBlockFaces) {
    			SimpleBlock rel = rooter.getRelative(face);
    			if(random.nextBoolean() && BlockUtils.isStoneLike(rel.getType())) {
    				rel.setType(OneOneSevenBlockHandler.ROOTED_DIRT);
    				if(BlockUtils.isAir(rel.getRelative(0,-1,0).getType()))
    					rel.getRelative(0,-1,0).setType(OneOneSevenBlockHandler.HANGING_ROOTS);
    			}
    		}
    		rooter = rooter.getRelative(0,-1,0);
    	}
    	rooter.setType(OneOneSevenBlockHandler.HANGING_ROOTS);
    }

    public static void spawnCoconutTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        SimpleBlock base = new SimpleBlock(data, x, y, z);
        FractalTreeBuilder builder = new FractalTreeBuilder(FractalTypes.Tree.COCONUT_TOP);
        
        //If gradient too steep, don't try spawning
        if(!builder.checkGradient(data, x, z))
        	return;
        
        //Spawn the base
        Material log = Material.JUNGLE_WOOD;
        if (TConfigOption.MISC_TREES_FORCE_LOGS.getBoolean()) log = Material.JUNGLE_LOG;
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new Wall(base.getRelative(face), BlockFace.NORTH).downUntilSolid(new Random(), log);
        }
        builder.build(tw, data, x, y, z);
    }

    public static void spawnSmallJungleTree(boolean skipGradientCheck, TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
    	FractalTreeBuilder ftb;
    	if (GenUtils.chance(1, 8))
    		ftb = new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_EXTRA_SMALL);
        else
        	ftb = new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_SMALL);
    	
    	if(skipGradientCheck) ftb.skipGradientCheck();
    	ftb.build(tw, data, x, y, z);
    }

    public static void spawnBigDarkOakTree(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        
    	FractalTreeBuilder bottomBuilder = new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_BIG_BOTTOM);
    	
    	//Don't spawn a tree if the terrain is too steep.
    	if(!bottomBuilder.checkGradient(data, x, z)) return;
    	
    	new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_BIG_TOP).skipGradientCheck().build(tw, data, x, y, z);
        bottomBuilder.build(tw, data, x, y - 5, z);
    }

    /**
     * Corals will always dig 2 blocks deeper first.
     * Grows a random giant coral (fire, tube, etc)
     */
    public static void spawnRandomGiantCoral(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        FractalTypes.Tree type = FRACTAL_CORAL_TYPES[tw.getHashedRand(x, y, z).nextInt(5)];
        FractalTreeBuilder ftb = new FractalTreeBuilder(type);
        ftb.setMaxHeight(TerraformGenerator.seaLevel - y - 1); //Max height is one below sea level
        ftb.build(tw, data, x, y - 2, z);
    }
}
