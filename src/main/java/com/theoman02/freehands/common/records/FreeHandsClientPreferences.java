package com.theoman02.freehands.common.records;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FreeHandsClientPreferences(
        boolean preferOffhand,
        boolean ignoreHotbar,
        boolean tryOtherHand
) {
    public static final StreamCodec<ByteBuf, FreeHandsClientPreferences> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FreeHandsClientPreferences::preferOffhand,
            ByteBufCodecs.BOOL, FreeHandsClientPreferences::ignoreHotbar,
            ByteBufCodecs.BOOL, FreeHandsClientPreferences::tryOtherHand,
            FreeHandsClientPreferences::new
    );
}