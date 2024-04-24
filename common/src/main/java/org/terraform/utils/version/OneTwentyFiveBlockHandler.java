package org.terraform.utils.version;

import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.EntityType;

public class OneTwentyFiveBlockHandler {
    public static final PatternType RHOMBUS = !Version.isAtLeast(20.4) ?
            PatternType.valueOf("RHOMBUS_MIDDLE") : PatternType.valueOf("RHOMBUS");
    public static final PatternType CIRCLE = !Version.isAtLeast(20.4) ?
            PatternType.valueOf("CIRCLE_MIDDLE") : PatternType.valueOf("CIRCLE");

    public static final EntityType ARMADILLO = !Version.isAtLeast(20.4) ?
            EntityType.PIG : EntityType.valueOf("ARMADILLO");
}
