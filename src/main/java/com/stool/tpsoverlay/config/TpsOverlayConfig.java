package com.stool.tpsoverlay.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class TpsOverlayConfig {
    @SerialEntry public boolean enabled = true;
    @SerialEntry public String displayFormat = "TPS: {tps} | MSPT: {mspt} | Ping: {ping}ms";
    @SerialEntry public float overlayX = 0.02f;
    @SerialEntry public float overlayY = 0.02f;
    @SerialEntry public boolean centerAnchor = false;
    @SerialEntry public boolean colorEnabled = true;
    @SerialEntry public int pollIntervalMs = 250;
    @SerialEntry public boolean estimateClientTps = true;
    @SerialEntry public TpsWindow tpsWindow = TpsWindow.FIVE_SECONDS;
    @SerialEntry public float textScale = 1.0f;
    @SerialEntry public boolean backgroundEnabled = true;
    @SerialEntry public float backgroundOpacity = 0.5f;
    @SerialEntry public int backgroundPadding = 2;
    @SerialEntry public int backgroundColor = 0x000000;
    @SerialEntry public boolean backgroundRounded = true;
    @SerialEntry public int backgroundRadius = 4;
    @SerialEntry public boolean hideInMenus = true;
    @SerialEntry public boolean hideWithF3 = true;
    @SerialEntry public boolean hideWhenChatOpen = false;
    @SerialEntry public boolean hideInCinematic = true;
    @SerialEntry public boolean graphEnabled = false;
    @SerialEntry public boolean graphShowTps = true;
    @SerialEntry public boolean graphShowMspt = false;
    @SerialEntry public boolean graphShowFps = false;
    @SerialEntry public boolean graphShowPing = false;
    @SerialEntry public float graphCenterX = 0.75f;
    @SerialEntry public float graphCenterY = 0.75f;
    @SerialEntry public boolean graphCenterAnchor = true;
    @SerialEntry public int graphWidth = 80;
    @SerialEntry public int graphHeight = 28;
}
