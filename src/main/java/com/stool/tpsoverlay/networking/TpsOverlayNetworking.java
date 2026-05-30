package com.stool.tpsoverlay.networking;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.metrics.MetricsAccess;
import com.stool.tpsoverlay.metrics.TickTimeService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class TpsOverlayNetworking {
    private TpsOverlayNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry<RegistryFriendlyByteBuf> clientbound = PayloadTypeRegistry.clientboundPlay();
        clientbound.register(MetricsPayload.ID, MetricsPayload.CODEC);
        clientbound.register(ClientActionPayload.ID, ClientActionPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            server.execute(() -> sendMetrics(server, handler.getPlayer())));
    }

    public static MetricsPayload createPayload(MinecraftServer server) {
        TickTimeService service = MetricsAccess.of(server);
        double[] tps = service.recentTps();
        return new MetricsPayload(
            (float) service.averageMspt(),
            (float) tps[0],
            (float) tps[1],
            (float) tps[2],
            (float) tps[3]
        );
    }

    public static void sendMetrics(MinecraftServer server, ServerPlayer player) {
        ServerPlayNetworking.send(player, createPayload(server));
    }

    public static void broadcastMetrics(MinecraftServer server) {
        MetricsPayload payload = createPayload(server);
        for (ServerPlayer player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendClientAction(ServerPlayer player, ClientActionPayload.ClientAction action) {
        ServerPlayNetworking.send(player, new ClientActionPayload(action));
    }

    public static int pollIntervalMs() {
        return Math.max(50, TpsOverlayConfigHandler.getConfig().pollIntervalMs);
    }
}
