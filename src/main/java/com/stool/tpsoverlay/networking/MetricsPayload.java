package com.stool.tpsoverlay.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record MetricsPayload(float mspt, float tps5s, float tps1m, float tps5m, float tps15m) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MetricsPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tpsoverlay", "metrics"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MetricsPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, MetricsPayload::mspt,
        ByteBufCodecs.FLOAT, MetricsPayload::tps5s,
        ByteBufCodecs.FLOAT, MetricsPayload::tps1m,
        ByteBufCodecs.FLOAT, MetricsPayload::tps5m,
        ByteBufCodecs.FLOAT, MetricsPayload::tps15m,
        MetricsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
