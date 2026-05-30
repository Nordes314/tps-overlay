package com.stool.tpsoverlay.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class TpsOverlayConfigHandler {
    public static final ConfigClassHandler<TpsOverlayConfig> HANDLER = ConfigClassHandler.createBuilder(TpsOverlayConfig.class)
        .id(Identifier.fromNamespaceAndPath("tpsoverlay", "config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("tpsoverlay.json"))
            .setJson5(true)
            .build())
        .build();

    private TpsOverlayConfigHandler() {
    }

    public static void load() {
        HANDLER.load();
    }

    public static TpsOverlayConfig getConfig() {
        return HANDLER.instance();
    }

    public static void save() {
        HANDLER.save();
    }

    public static TpsOverlayConfig defaults() {
        return HANDLER.defaults();
    }
}
