package com.theoman02.freehands.network;

import com.theoman02.freehands.FreeHands;
import com.theoman02.freehands.common.action.FreeHandsActions;
import com.theoman02.freehands.common.debug.FreeHandsDebug;
import com.theoman02.freehands.common.records.FreeHandsClientPreferences;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FreeHands.MODID)
public final class FreeHandsNetworking {
    private FreeHandsNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer( FreeHandsPayload.TYPE,
                                FreeHandsPayload.STREAM_CODEC,
                                FreeHandsNetworking::handleFreeHandsPayload
        );
    }

    private static void handleFreeHandsPayload(FreeHandsPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        FreeHandsClientPreferences clientPreferences = payload.clientPreferences();
        FreeHandsDebug.logServer(
                "Received FreeHands payload from {}. preferences={}",
                player.getName().getString(),
                clientPreferences
        );
        FreeHandsActions.moveHandToFreeSlot(player, clientPreferences);
    }
}
