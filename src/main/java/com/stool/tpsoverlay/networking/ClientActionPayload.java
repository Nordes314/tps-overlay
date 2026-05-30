package com.stool.tpsoverlay.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientActionPayload(ClientAction action) implements CustomPacketPayload {
    public enum ClientAction {
        TOGGLE_EDIT,
        TOGGLE_VISIBLE,
        OPEN_CONFIG,
        RESET_POSITION,
        RELOAD_CONFIG
    }

    public static final CustomPacketPayload.Type<ClientActionPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tpsoverlay", "client_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientActionPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.map(ClientAction::valueOf, ClientAction::name),
        ClientActionPayload::action,
        ClientActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
