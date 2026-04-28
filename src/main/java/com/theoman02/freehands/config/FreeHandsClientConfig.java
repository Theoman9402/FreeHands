package com.theoman02.freehands.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FreeHandsClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PREFER_OFFHAND = BUILDER
            .translation("freehands.configuration.preferOffhand")
            .comment("Checks the offhand first before the main hand.")
            .define("preferOffhand", false);

    public static final ModConfigSpec.BooleanValue IGNORE_HOTBAR = BUILDER
            .translation("freehands.configuration.ignoreHotbar")
            .comment("Ignores your hotbar when stashing items")
            .define("ignoreHotbar", true);

    public static final ModConfigSpec.BooleanValue DEBUG = BUILDER
            .translation("freehands.configuration.clientDebug")
            .comment("Enable debug logging.")
            .define("clientDebug", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
