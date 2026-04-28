package com.theoman02.freehands.client;

import com.theoman02.freehands.FreeHands;
import com.theoman02.freehands.common.action.FreeHandsActions;
import com.theoman02.freehands.config.FreeHandsClientConfig;
import com.theoman02.freehands.network.FreeHandsPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = FreeHands.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FreeHands.MODID, value = Dist.CLIENT)
public class FreeHandsClient {
    private static final Component INVENTORY_FULL_MESSAGE = Component.translatable("freehands.message.inventory_full")
                                                                     .withStyle(ChatFormatting.RED);
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (FreeHandsKeyMappings.FREEHANDS_MAPPING.get().consumeClick()) {
            final var player = Minecraft.getInstance().player;
            boolean clientDebug = FreeHandsClientConfig.DEBUG.get();

            // Guard against early ticks where the local player may not exist yet.
            if (player == null) {
                return;
            }

            final boolean preferOffhand = FreeHandsClientConfig.PREFER_OFFHAND.get();
            final boolean ignoreHotbar = FreeHandsClientConfig.IGNORE_HOTBAR.get();

            if (clientDebug) {
                FreeHands.LOGGER.info(
                        "[FREEHANDS][CLIENT]FreeHands key pressed. Settings: preferOffhand={}, ignoreHotbar={}",
                        preferOffhand,
                        ignoreHotbar
                );
            }

            if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
                if (clientDebug) {
                    FreeHands.LOGGER.info("[FREEHANDS][CLIENT] Both hands are empty; not sending request to server");
                }
                return;
            }

            // Simulate on the client first to avoid sending pointless packets
            // when no valid destination exists.
            boolean canInsert = FreeHandsActions.simulateMoveHandToFreeSlot(player, preferOffhand, ignoreHotbar);

            if (!canInsert) {
                player.displayClientMessage(INVENTORY_FULL_MESSAGE,true);
                if (clientDebug) {
                    FreeHands.LOGGER.info("[FREEHANDS][CLIENT] Client simulation found no valid move; not sending request to server");
                }
                return;
            }

            if (clientDebug) {
                FreeHands.LOGGER.info("[FREEHANDS][CLIENT] Client simulation valid, sending request to server");
            }
            PacketDistributor.sendToServer(new FreeHandsPayload(preferOffhand, ignoreHotbar));
        }
    }

    public FreeHandsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        if (FreeHandsClientConfig.DEBUG.get()) {
            FreeHands.LOGGER.info("[FREEHANDS][CLIENT] Freehands client initialised");
        }
    }

}
