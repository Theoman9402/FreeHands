package com.theoman02.freehands.network;

import com.theoman02.freehands.FreeHands;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FreeHandsPayload(boolean preferOffhand, boolean ignoreHotbar) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FreeHandsPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(FreeHands.MODID, "free_hands")
            );

    public static final StreamCodec<ByteBuf, FreeHandsPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL,
                                  FreeHandsPayload::preferOffhand,
                                  ByteBufCodecs.BOOL,
                                  FreeHandsPayload::ignoreHotbar,
                                  FreeHandsPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}