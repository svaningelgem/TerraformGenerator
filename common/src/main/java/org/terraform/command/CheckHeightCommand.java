package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeSection;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructureRegistry;

import java.util.Stack;

public class CheckHeightCommand extends TerraCommand {

    public CheckHeightCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Checks the heights of various noise maps";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {

        Player p = (Player) sender;
        int x = p.getLocation().getBlockX();
        int z = p.getLocation().getBlockZ();

        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        MegaChunk mc = new MegaChunk(x, 0, z);
        BiomeBank.debugPrint = true;
        BiomeBank biome = tw.getBiomeBank(x, z);
        BiomeBank.debugPrint = false;
        
        BiomeSection section = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        p.sendMessage("Core Height: " + HeightMap.CORE.getHeight(tw, x, z));
        //p.sendMessage("Mountainous Height: " + HeightMap.MOUNTAIN.getHeight(tw, x, z));
        p.sendMessage("Attrition Height: " + HeightMap.ATTRITION.getHeight(tw, x, z));
        p.sendMessage("Gradient (2,3,4): " + HeightMap.getNoiseGradient(tw, x, z, 2) + "," + HeightMap.getNoiseGradient(tw, x, z, 3) + "," + HeightMap.getNoiseGradient(tw, x, z,4)); 
        p.sendMessage("True Gradient (2,3,4): " + HeightMap.getTrueHeightGradient(data, x, z, 2) + "," + HeightMap.getTrueHeightGradient(data, x, z, 3) + "," + HeightMap.getTrueHeightGradient(data, x, z,4));
        p.sendMessage("Result height: " + HeightMap.getBlockHeight(tw, x, z));
        p.sendMessage("River Depth: " + HeightMap.getRawRiverDepth(tw, x, z));
        p.sendMessage("Mega Chunk: " + mc.getX() + "," + mc.getZ());
        p.sendMessage("Mega Chunk Center: " + mc.getCenterBlockCoords()[0] + "," + mc.getCenterBlockCoords()[1]);
        
        p.sendMessage("Biome Section: " + section.toString());
        p.sendMessage("Surrounding Sections:");
        for(BiomeSection sect:BiomeSection.getSurroundingSections(tw, x, z)) {
            p.sendMessage("    - " + sect.toString() + "(" + sect.getBiomeBank() + ")");
        }
        for(SingleMegaChunkStructurePopulator spop:StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
        	if (spop == null) continue;
        	int[] coords = mc.getCenterBlockCoords(); //spop.getCoordsFromMegaChunk(tw, mc);
        	int dist = (int) Math.sqrt(Math.pow(x-coords[0], 2) + Math.pow(z-coords[1], 2));
        	p.sendMessage(" - Structure Registered: " + spop.getClass().getSimpleName() + "(" + coords[0] + "," + coords[1] + ") " + dist + " blocks away");
        }
        
        p.sendMessage("Temperature: " + BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getTemperature());
        p.sendMessage("Moisture: " + BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getMoisture());
        p.sendMessage("Biome edge factor: " + new BiomeBlender(tw, true, false, false)
                .setBiomeThreshold(0.45).getEdgeFactor(biome, x, z));
        p.sendMessage("Result Biome: " + biome);
        
    }
}
