package com.stool.tpsoverlay;

import com.stool.tpsoverlay.command.TpsOverlayCommands;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.networking.TpsOverlayNetworking;
import com.stool.tpsoverlay.sync.MetricsSyncTask;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public final class TpsOverlayMod implements ModInitializer {
    public static final String MOD_ID = "tpsoverlay";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        TpsOverlayConfigHandler.load();
        TpsOverlayNetworking.init();
        MetricsSyncTask.register();
        TpsOverlayCommands.register();
        LOGGER.info("TPS Overlay initialized");
    }
}
