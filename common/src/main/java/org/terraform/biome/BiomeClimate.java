package org.terraform.biome;

import org.jetbrains.annotations.NotNull;
import org.terraform.utils.Range;
import org.terraform.main.config.TConfig;

public enum BiomeClimate {

	//Tree-Dense areas
	HUMID_VEGETATION(Range.between(
			(double)config.CLIMATE_HUMIDVEGETATION_MINTEMP,
			(double)config.CLIMATE_HUMIDVEGETATION_MAXTEMP),
			Range.between(
					(double)config.CLIMATE_HUMIDVEGETATION_MINMOIST,
					(double)config.CLIMATE_HUMIDVEGETATION_MAXMOIST),2),
	
	//Savannas
	DRY_VEGETATION(Range.between(
			(double)config.CLIMATE_DRYVEGETATION_MINTEMP,
			(double)config.CLIMATE_DRYVEGETATION_MAXTEMP),
			Range.between(
					(double)config.CLIMATE_DRYVEGETATION_MINMOIST,
					(double)config.CLIMATE_DRYVEGETATION_MAXMOIST),1),
	
	//Deserts
	HOT_BARREN(Range.between(
			(double)config.CLIMATE_HOTBARREN_MINTEMP,
			(double)config.CLIMATE_HOTBARREN_MAXTEMP),
			Range.between(
					(double)config.CLIMATE_HOTBARREN_MINMOIST,
					(double)config.CLIMATE_HOTBARREN_MAXMOIST),2),
	
	//Cold biomes - taigas, maybe eroded plains
	COLD(Range.between(
			(double)config.CLIMATE_COLD_MINTEMP,
			(double)config.CLIMATE_COLD_MAXTEMP),
			Range.between(
					(double)config.CLIMATE_COLD_MINMOIST,
					(double)config.CLIMATE_COLD_MAXMOIST),1),
	
	//Any snowy biomes. 
	SNOWY(Range.between(
			(double)config.CLIMATE_SNOWY_MINTEMP,
			(double)config.CLIMATE_SNOWY_MAXTEMP),
			Range.between(
					(double)config.CLIMATE_SNOWY_MINMOIST,
					(double)config.CLIMATE_SNOWY_MAXMOIST),2),
	
	//Default climate.
	TRANSITION(Range.between(-4.0,4.0),Range.between(-4.0,4.0),0), 
	; 
	
	final Range<Double> temperatureRange;
	final Range<Double> moistureRange;
	final int priority; //Higher priority means override.
	BiomeClimate(Range<Double> temperatureRange, Range<Double> moistureRange, int priority){
		this.temperatureRange = temperatureRange;
		this.moistureRange = moistureRange;
		this.priority = priority;
	}

	public Range<Double> getTemperatureRange() {
		return temperatureRange;
	}

	public Range<Double> getMoistureRange() {
		return moistureRange;
	}
	
	private static boolean isInRange(double val, @NotNull Range<Double> r) {
		return r.getMaximum() >= val && r.getMinimum() <= val;
	}
	
	public static @NotNull BiomeClimate selectClimate(double temp, double moist) {
		
		BiomeClimate candidate = BiomeClimate.TRANSITION;
		
		for(BiomeClimate climate:BiomeClimate.values())
			if(isInRange(temp,climate.getTemperatureRange())
					&& isInRange(moist,climate.getMoistureRange())) {
				
				//If there are multiple climate ranges that apply to this, then
				//the climate with the highest priority will win.
                if(candidate.priority < climate.priority)
                    candidate = climate;
			}
		
		return candidate;
	}
	
}