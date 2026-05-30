package com.stool.tpsoverlay.client;

import com.stool.tpsoverlay.TpsOverlayMod;
import com.stool.tpsoverlay.client.config.TpsOverlayConfigScreens;
import com.stool.tpsoverlay.client.hud.OverlayDragHandler;
import com.stool.tpsoverlay.client.hud.TpsOverlayHud;
import com.stool.tpsoverlay.client.hud.OverlayWidgets;
import com.stool.tpsoverlay.client.hud.OverlayWidgetKind;
import com.stool.tpsoverlay.client.state.ClientMetricsState;
import com.stool.tpsoverlay.client.state.MetricsHistory;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.networking.ClientActionPayload;
import com.stool.tpsoverlay.networking.MetricsPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.resources.Identifier;

public class TpsOverlayClient implements ClientModInitializer {
    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath(TpsOverlayMod.MOD_ID, "overlay");

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            HUD_ID,
            (graphics, delta) -> TpsOverlayHud.render(graphics, delta.getGameTimeDeltaPartialTick(false))
        );

        ClientPlayNetworking.registerGlobalReceiver(MetricsPayload.ID, (payload, context) ->
            context.client().execute(() -> ClientMetricsState.update(payload)));

        ClientPlayNetworking.registerGlobalReceiver(ClientActionPayload.ID, (payload, context) ->
            context.client().execute(() -> handleClientAction(payload.action())));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientMetricsState.onDisconnect());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientMetricsState.onJoin());

        ClientPreAttackCallback.EVENT.register((client, player, clickCount) ->
            OverlayDragHandler.shouldCaptureMouse(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMetricsState.tickAvailability();
            MetricsHistory.tick();
        });
    }

    private static void handleClientAction(ClientActionPayload.ClientAction action) {
        switch (action) {
            case TOGGLE_EDIT -> OverlayDragHandler.toggleEditMode();
            case TOGGLE_VISIBLE -> {
                var config = TpsOverlayConfigHandler.getConfig();
                config.enabled = !config.enabled;
                TpsOverlayConfigHandler.save();
            }
            case OPEN_CONFIG -> {
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player != null) {
                    minecraft.setScreen(TpsOverlayConfigScreens.create(null));
                }
            }
            case RESET_POSITION -> {
                var defaults = TpsOverlayConfigHandler.defaults();
                var config = TpsOverlayConfigHandler.getConfig();
                OverlayWidgets.reset(config, defaults, OverlayWidgetKind.TEXT);
                OverlayWidgets.reset(config, defaults, OverlayWidgetKind.GRAPH);
                TpsOverlayConfigHandler.save();
            }
            case RELOAD_CONFIG -> TpsOverlayConfigHandler.load();
        }
    }
}
