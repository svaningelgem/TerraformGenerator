package org.terraform.structure.village.plains;

import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.SphereBuilder.SphereType;

public abstract class PlainsVillageAbstractRoomPopulator extends RoomPopulatorAbstract {

	public PlainsVillageAbstractRoomPopulator(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
	}
	
    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	//Test the center and 4 corners to ensure that the ground is fairly stable
    	int roomY = this.calculateRoomY(data, room);
    	
    	int worldHeight = TerraformGeneratorPlugin.injector.getMaxY()-TerraformGeneratorPlugin.injector.getMinY() + 1;
    	
    	for(int[] corner:room.getAllCorners(2)) {
    		SimpleBlock sb = new SimpleBlock(data,corner[0],roomY,corner[1]);
    		int lowSb = sb.findFloor(worldHeight).getY();
    		if(Math.abs(lowSb - roomY) > config.getInt(TConfig.Option.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE))
    		{
    			//place platform as uneven ground was detected.
    			this.placeFixerPlatform(roomY, data, room);
    			break;
    		}
    	}
    }

	public void placeFixerPlatform(int roomY, PopulatorDataAbstract data, CubeRoom room) {
		
    	//Semisphere air and semisphere ground
		SimpleBlock core = room.getCenterSimpleBlock(data);
		core = core.getAtY(roomY);
		//Air
		new SphereBuilder(this.rand, core.getUp(), Material.AIR)
		.setRX(room.getWidthX()/2 - 1.5f)
		.setRZ(room.getWidthZ()/2 - 1.5f)
		.setRY(getRoomRoughNeededHeight())
		.setHardReplace(true)
		.setSphereType(SphereType.UPPER_SEMISPHERE)
		.build();

		//Platform
		new SphereBuilder(this.rand, core.getDown(), Material.DIRT)
		.setRX(room.getWidthX()/2f)
		.setRZ(room.getWidthZ()/2f)
		.setRY(3)
		.setPadding(4)
		.setSphereFrequency(0.11f)
		.setMinRadius(0.8)
		.setHardReplace(false)
		.setUpperType(Material.GRASS_BLOCK)
		.setSphereType(SphereType.LOWER_SEMISPHERE)
		.build(); 

	}
	
	public int getRoomRoughNeededHeight() {
		return 12;
	}
	
	protected int calculateRoomY(PopulatorDataAbstract data, CubeRoom room) {
		int centerHeight = GenUtils.getHighestGroundOrSeaLevel(data, room.getX(), room.getZ());
    	int pathHeight = getPathHeight(data, room);
    	
    	if(Math.abs(centerHeight-pathHeight) > config.getInt(TConfig.Option.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE)) {
    		return pathHeight;
    	}else {
    		return centerHeight;
    	}
	}
	
	protected boolean doesAreaFailTolerance(PopulatorDataAbstract data, CubeRoom room) {
//		int centerHeight = GenUtils.getHighestGroundOrSeaLevel(data, room.getX(), room.getZ());
//    	int pathHeight = getPathHeight(data, room);
//    	
//    	return Math.abs(centerHeight-pathHeight) > config.getInt(TConfig.Option.ption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE);
		int roomY = calculateRoomY(data,room);
		int worldHeight = TerraformGeneratorPlugin.injector.getMaxY()-TerraformGeneratorPlugin.injector.getMinY() + 1;
    	for(int[] corner:room.getAllCorners(2)) {
    		SimpleBlock sb = new SimpleBlock(data,corner[0],roomY,corner[1]);
    		int lowSb = sb.findFloor(worldHeight).getY();
    		if(Math.abs(lowSb - roomY) > config.getInt(TConfig.Option.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE))
    		{
    			return true;
    		}
    	}
		return false;
	}
	
	protected int getPathHeight(PopulatorDataAbstract data, CubeRoom room) {
		int pathHeight; //This is the entry height for the house/object
    	
        BlockFace dir = ((DirectionalCubeRoom) room).getDirection();
        int pad = GenUtils.randInt(1, 3);
    	
    	//calculate center height
    	Entry<Wall, Integer> openingWallSet = room.getWall(data, dir, pad);
    	pathHeight = openingWallSet.getKey().getLeft(openingWallSet.getValue()/2).getGroundOrSeaLevel().getY();
    	return pathHeight;
	}
	

}
