package org.terraform.utils.bstats;

import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

public class TerraformGeneratorMetricsHandler {

	@SuppressWarnings("unused")
	private TerraformGeneratorPlugin plugin;
	
	public TerraformGeneratorMetricsHandler(TerraformGeneratorPlugin plugin) {
		this.plugin = plugin;
		
		int pluginId = 13968;
        Metrics metrics = new Metrics(plugin, pluginId);
        
        if(metrics.isEnabled()) {
        	metrics.addCustomChart(new Metrics.SimplePie("onlyUseLogsNoWood", () -> config.getBoolean(TConfig.Option.MISC_TREES_FORCE_LOGS) + ""));
        	metrics.addCustomChart(new Metrics.SimplePie("megaChunkNumBiomeSections", () -> config.getInt(TConfig.Option.STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS) + ""));
        	metrics.addCustomChart(new Metrics.SimplePie("biomeSectionBitshifts", () -> config.getInt(TConfig.Option.BIOME_SECTION_BITSHIFTS) + ""));
        	TerraformGeneratorPlugin.logger.stdout("&abStats Metrics enabled.");
        }
        else
        	TerraformGeneratorPlugin.logger.stdout("&cbStats Metrics disabled.");
        
        
	}
	
	
	
}
