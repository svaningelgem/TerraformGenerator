package org.terraform.structure;

import org.terraform.biome.BiomeBank;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
public class StructureBufferDistanceHandler {
	
	/**
	 * Called from decorators to determine whether or not they
	 * can place large trees and obstructive decorations, or if
	 * they must make way for structures.
	 */
	public static boolean canDecorateChunk(TerraformWorld tw, int chunkX, int chunkZ) {
		MegaChunk mc = new MegaChunk(chunkX, chunkZ);
		BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
        for (StructurePopulator structPop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
            if (structPop == null) continue;
            if(!(structPop instanceof SingleMegaChunkStructurePopulator)) continue;
            SingleMegaChunkStructurePopulator spop = (SingleMegaChunkStructurePopulator) structPop;
            int chunkBufferRadius = spop.getChunkBufferDistance();
            if(chunkBufferRadius <= 0)
            	continue;
            for(int rcx = -chunkBufferRadius; rcx <= chunkBufferRadius; rcx++) {
            	for(int rcz = -chunkBufferRadius; rcz <= chunkBufferRadius; rcz++) {
                    //ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, chunkX+rcx, chunkZ+rcz);
                    //BiomeSection homeSection = BiomeBank.getBiomeSectionFromChunk(tw, chunkX+rcx, chunkZ+rcz);
                    //banks = new ArrayList<BiomeBank>() {{ add(homeSection.getBiomeBank()); }};
            		if (spop.canSpawn(tw, chunkX+rcx, chunkZ+rcz, biome)) {
                    	return false;
                    }
                }
            }
        }
        
        return true;
	}

}
