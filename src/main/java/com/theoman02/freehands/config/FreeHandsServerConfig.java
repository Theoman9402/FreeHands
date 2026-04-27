package com.theoman02.freehands.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FreeHandsServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG = BUILDER
            .translation("freehands.configuration.debug")
            .comment("Enables server-side debug logging.")
            .define("debug", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
