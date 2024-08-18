package org.terraform.coregen.bukkit;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.HeightMap;
import org.terraform.main.TLogger;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.injection.InjectableObject;
import org.terraform.watchdog.TfgWatchdogSuppressant;

public class TerraformGenerator extends InjectableObject {
    private final @NotNull TConfig config;
    private final @NotNull TerraformGeneratorPlugin plugin;
    private final @NotNull TLogger logger;
    private final @NotNull HeightMap heightMap;

    private final TfgWatchdogSuppressant watchdogSuppressant;

    public TerraformGenerator(@NotNull TerraformGeneratorPlugin plugin) {
        this(plugin, plugin.getTConfig());
    }

    public TerraformGenerator(@NotNull TerraformGeneratorPlugin plugin, @NotNull TConfig config) {
        this.plugin = plugin;
        this.config = register(config);
        this.logger = create(TLogger.class);
        this.watchdogSuppressant = create(TfgWatchdogSuppressant.class);
        register(this);
        this.heightMap = create(HeightMap.class);
    }
}
