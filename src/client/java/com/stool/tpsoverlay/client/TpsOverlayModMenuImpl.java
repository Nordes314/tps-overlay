package com.stool.tpsoverlay.client;

import com.stool.tpsoverlay.client.config.TpsOverlayConfigScreens;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class TpsOverlayModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TpsOverlayConfigScreens::create;
    }
}
