package com.theoman02.freehands.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.theoman02.freehands.FreeHands;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;

@EventBusSubscriber(modid = FreeHands.MODID, value = Dist.CLIENT)
public final class FreeHandsKeyMappings {
    // Key is intentionally unbound by default.
    public static final Lazy<KeyMapping> FREEHANDS_MAPPING = Lazy.of(
            () -> generateKeyMapping(InputConstants.UNKNOWN.getValue())
    );
    // Helper method so additional mappings can follow the same pattern later.
    private static KeyMapping generateKeyMapping(int key) {
        return new KeyMapping(
                "key.freehands.freehands", // Translation key for the mapping name
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
                key,
                "key.categories.freehands" // Translation key for the mapping category
        );
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(FREEHANDS_MAPPING.get());
    }
}
