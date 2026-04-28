package com.theoman02.freehands.network;

import com.theoman02.freehands.FreeHands;
import com.theoman02.freehands.common.records.FreeHandsClientPreferences;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FreeHandsPayload(FreeHandsClientPreferences clientPreferences) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FreeHandsPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FreeHands.MODID, "free_hands")
            );

    public static final StreamCodec<ByteBuf, FreeHandsPayload> STREAM_CODEC = StreamCodec.composite(
            FreeHandsClientPreferences.STREAM_CODEC, FreeHandsPayload::clientPreferences,
            FreeHandsPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}