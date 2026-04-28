package com.theoman02.freehands;

import com.mojang.logging.LogUtils;
import com.theoman02.freehands.config.FreeHandsClientConfig;
import com.theoman02.freehands.config.FreeHandsServerConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(FreeHands.MODID)
public final class FreeHands {
    public static final String MODID = "freehands";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FreeHands(ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.CLIENT, FreeHandsClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, FreeHandsServerConfig.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (FreeHandsServerConfig.DEBUG.get()) {
            LOGGER.info("[FREEHANDS][SERVER]: FreeHands server starting");
        }
    }
}
