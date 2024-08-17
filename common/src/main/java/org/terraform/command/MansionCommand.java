package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.pillager.mansion.MansionJigsawBuilder;

import java.util.Random;
import java.util.Stack;

public class MansionCommand extends TerraCommand {

    public MansionCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Mansion Test";
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
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        MansionJigsawBuilder builder = new MansionJigsawBuilder(
        		config.getInt(TConfig.Option.STRUCTURES_MANSION_SIZE),
        		config.getInt(TConfig.Option.STRUCTURES_MANSION_SIZE),
        		data, x, y, z
        );
        builder.generate(new Random());
        builder.build(new Random());
        p.sendMessage("Complete.");
    }

}
