package org.terraform.main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LanguageManager {
    private static final Map<String, String> cache = new HashMap<>();
    private static YamlConfiguration yaml = null;

    public static void init(final @NotNull File languageFile) {
        cache.clear();
        if(languageFile.exists()) {
            yaml = YamlConfiguration.loadConfiguration(languageFile);
        } else {
            yaml = new YamlConfiguration();
        }
        loadDefaults();
    }

    private static void loadDefaults() {
        translate("permissions.insufficient", "&cYou don't have enough permissions to perform this action!");
        translate("command.wrong-arg-length", "&cToo many or too little arguments provided!");
        translate("command.unknown", "&cUnknown subcommand.");
        translate("command.help.postive-pages", "&cThe page specified must be a positive number!");
        translate("permissions.console-cannot-exec", "&cOnly players can execute this command.");

        translate("command.locate.novanilla", "&c&lFor terraformgenerator worlds, use &e&l/terra locate &c&linstead!");
        translate("command.locate.structure.not.enabled", "&cThe specified structure was not enabled!");
        translate("command.locate.locate.coords", "&aLocated at X: %x% Z: %z%");
        translate("command.locate.searching", "&bSearching for structure asynchronously. Please wait...");
        translate("command.locatebiome.invalidbiome", "&cInvalid Biome. Valid Biomes:");
        translate("command.locatebiome.not.in.5000", "&cCould not find this biome within 5000 blocks.");
        translate("command.locatebiome.disabled", "&cThis biome is disabled.");
        translate("command.locate.list.header", "&e-==[&bStructure Handlers&e]==-");
        translate("command.locate.list.entry", "&e - &b%entry%");
        translate("command.locate.completed.task", "&aCompleted Locate task (%time%ms)");
    }

    public static String translate(@NotNull String langKey) {
        return translate(langKey, null);
    }

    public static String translate(@NotNull String langKey, @Nullable String def) {
        return cache.computeIfAbsent(langKey,
                key -> {
                    String value = Objects.requireNonNullElse(yaml.getString(langKey, def), langKey);
                    return ChatColor.translateAlternateColorCodes('&', value);
                });
    }

    public static String parse(final String langKey, Object @NotNull ... placeholders) {
        String parsed = translate(langKey, null);
        for (int i = 0; i < placeholders.length; i += 2) {
            parsed = parsed.replaceAll(placeholders[i].toString(), placeholders[i + 1].toString());
        }
        return parsed;
    }
}