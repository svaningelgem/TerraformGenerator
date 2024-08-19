package org.terraform.utils.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;

public class RotatableBuilder {

    private final @NotNull Rotatable blockData;

    public RotatableBuilder(@NotNull Material mat) {
        this.blockData = (Rotatable) Bukkit.createBlockData(mat);
    }

    public @NotNull RotatableBuilder setRotation(@NotNull BlockFace face) {
        this.blockData.setRotation(face);
        return this;
    }

    public @NotNull RotatableBuilder apply(@NotNull SimpleBlock block) {
        if ( !TConfig.areDecorationsEnabled() ) return this;

        block.setBlockData(blockData);
        return this;
    }

    public @NotNull RotatableBuilder apply(@NotNull Wall block) {
        if ( !TConfig.areDecorationsEnabled() ) return this;

        block.setBlockData(blockData);
        return this;
    }

    public @NotNull RotatableBuilder apply(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if ( !TConfig.areDecorationsEnabled() ) return this;

        data.setBlockData(x, y, z, blockData);
        return this;
    }

    public @NotNull Rotatable get() {
        return blockData;
    }
}
