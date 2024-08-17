package org.terraform.coregen.heights;

import org.jetbrains.annotations.NotNull;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

public class RiverHeight extends HeightMap {
    @Override
    public double getHeight(TerraformWorld tw, int x, int z) {
        FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.HEIGHTMAP_RIVER, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(FastNoise.NoiseType.PerlinFractal);
            n.SetFrequency(config.HEIGHT_MAP_RIVER_FREQUENCY);
            n.SetFractalOctaves(5);
            return n;
        });
        return 15 - 200 * Math.abs(noise.GetNoise(x, z));
    }
}
