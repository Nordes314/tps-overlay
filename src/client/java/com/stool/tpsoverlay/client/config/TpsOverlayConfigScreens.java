package com.stool.tpsoverlay.client.config;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import com.stool.tpsoverlay.config.TpsWindow;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TpsOverlayConfigScreens {
    private TpsOverlayConfigScreens() {
    }

    public static Screen create(Screen parent) {
        var handler = TpsOverlayConfigHandler.HANDLER;
        return YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("tpsoverlay.config.title"))
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("tpsoverlay.config.category.display"))
                .option(boolOption("tpsoverlay.config.option.enabled", handler.defaults().enabled,
                    () -> handler.instance().enabled, v -> handler.instance().enabled = v))
                .option(Option.<String>createBuilder()
                    .name(Component.translatable("tpsoverlay.config.option.display_format"))
                    .description(OptionDescription.of(Component.translatable("tpsoverlay.config.option.display_format.desc")))
                    .binding(handler.defaults().displayFormat,
                        () -> handler.instance().displayFormat,
                        v -> handler.instance().displayFormat = v)
                    .controller(StringControllerBuilder::create)
                    .build())
                .option(boolOption("tpsoverlay.config.option.color_enabled", handler.defaults().colorEnabled,
                    () -> handler.instance().colorEnabled, v -> handler.instance().colorEnabled = v))
                .option(boolOption("tpsoverlay.config.option.background_enabled", handler.defaults().backgroundEnabled,
                    () -> handler.instance().backgroundEnabled, v -> handler.instance().backgroundEnabled = v))
                .option(floatSlider("tpsoverlay.config.option.background_opacity", handler.defaults().backgroundOpacity,
                    () -> handler.instance().backgroundOpacity, v -> handler.instance().backgroundOpacity = v, 0.0f, 1.0f))
                .option(intSlider("tpsoverlay.config.option.background_padding", handler.defaults().backgroundPadding,
                    () -> handler.instance().backgroundPadding, v -> handler.instance().backgroundPadding = v, 0, 16))
                .option(colorOption("tpsoverlay.config.option.background_color", handler.defaults().backgroundColor,
                    () -> handler.instance().backgroundColor, v -> handler.instance().backgroundColor = v))
                .option(boolOption("tpsoverlay.config.option.background_rounded", handler.defaults().backgroundRounded,
                    () -> handler.instance().backgroundRounded, v -> handler.instance().backgroundRounded = v))
                .option(intSlider("tpsoverlay.config.option.background_radius", handler.defaults().backgroundRadius,
                    () -> handler.instance().backgroundRadius, v -> handler.instance().backgroundRadius = v, 1, 16))
                .option(floatSlider("tpsoverlay.config.option.text_scale", handler.defaults().textScale,
                    () -> handler.instance().textScale, v -> handler.instance().textScale = v, 0.5f, 2.0f))
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("tpsoverlay.config.category.graph"))
                .option(boolOption("tpsoverlay.config.option.graph_enabled", handler.defaults().graphEnabled,
                    () -> handler.instance().graphEnabled, v -> handler.instance().graphEnabled = v))
                .option(boolOption("tpsoverlay.config.option.graph_show_tps", handler.defaults().graphShowTps,
                    () -> handler.instance().graphShowTps, v -> handler.instance().graphShowTps = v))
                .option(boolOption("tpsoverlay.config.option.graph_show_mspt", handler.defaults().graphShowMspt,
                    () -> handler.instance().graphShowMspt, v -> handler.instance().graphShowMspt = v))
                .option(boolOption("tpsoverlay.config.option.graph_show_fps", handler.defaults().graphShowFps,
                    () -> handler.instance().graphShowFps, v -> handler.instance().graphShowFps = v))
                .option(boolOption("tpsoverlay.config.option.graph_show_ping", handler.defaults().graphShowPing,
                    () -> handler.instance().graphShowPing, v -> handler.instance().graphShowPing = v))
                .option(intSlider("tpsoverlay.config.option.graph_width", handler.defaults().graphWidth,
                    () -> handler.instance().graphWidth, v -> handler.instance().graphWidth = v, 40, 160))
                .option(intSlider("tpsoverlay.config.option.graph_height", handler.defaults().graphHeight,
                    () -> handler.instance().graphHeight, v -> handler.instance().graphHeight = v, 16, 64))
                .option(floatSlider("tpsoverlay.config.option.graph_center_x", handler.defaults().graphCenterX,
                    () -> handler.instance().graphCenterX, v -> handler.instance().graphCenterX = v, 0.0f, 1.0f))
                .option(floatSlider("tpsoverlay.config.option.graph_center_y", handler.defaults().graphCenterY,
                    () -> handler.instance().graphCenterY, v -> handler.instance().graphCenterY = v, 0.0f, 1.0f))
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("tpsoverlay.config.category.position"))
                .option(floatSlider("tpsoverlay.config.option.overlay_x", handler.defaults().overlayX,
                    () -> handler.instance().overlayX, v -> handler.instance().overlayX = v, 0.0f, 1.0f))
                .option(floatSlider("tpsoverlay.config.option.overlay_y", handler.defaults().overlayY,
                    () -> handler.instance().overlayY, v -> handler.instance().overlayY = v, 0.0f, 1.0f))
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("tpsoverlay.config.category.performance"))
                .option(intSlider("tpsoverlay.config.option.poll_interval", handler.defaults().pollIntervalMs,
                    () -> handler.instance().pollIntervalMs, v -> handler.instance().pollIntervalMs = v, 50, 2000))
                .option(tpsWindowOption())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("tpsoverlay.config.category.visibility"))
                .option(boolOption("tpsoverlay.config.option.hide_in_menus", handler.defaults().hideInMenus,
                    () -> handler.instance().hideInMenus, v -> handler.instance().hideInMenus = v))
                .option(boolOption("tpsoverlay.config.option.hide_with_f3", handler.defaults().hideWithF3,
                    () -> handler.instance().hideWithF3, v -> handler.instance().hideWithF3 = v))
                .option(boolOption("tpsoverlay.config.option.hide_when_chat_open", handler.defaults().hideWhenChatOpen,
                    () -> handler.instance().hideWhenChatOpen, v -> handler.instance().hideWhenChatOpen = v))
                .option(boolOption("tpsoverlay.config.option.hide_in_cinematic", handler.defaults().hideInCinematic,
                    () -> handler.instance().hideInCinematic, v -> handler.instance().hideInCinematic = v))
                .build())
            .save(TpsOverlayConfigHandler::save)
            .build()
            .generateScreen(parent);
    }

    private static Option<TpsWindow> tpsWindowOption() {
        var handler = TpsOverlayConfigHandler.HANDLER;
        return Option.<TpsWindow>createBuilder()
            .name(Component.translatable("tpsoverlay.config.option.tps_window"))
            .description(OptionDescription.of(Component.translatable("tpsoverlay.config.option.tps_window.desc")))
            .binding(handler.defaults().tpsWindow, () -> handler.instance().tpsWindow, v -> handler.instance().tpsWindow = v)
            .controller(opt -> EnumControllerBuilder.create(opt)
                .enumClass(TpsWindow.class)
                .formatValue(value -> Component.translatable("tpsoverlay.config.tps_window." + value.name().toLowerCase())))
            .build();
    }

    private static Option<Boolean> boolOption(String key, boolean def, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
            .name(Component.translatable(key))
            .binding(def, getter, setter)
            .controller(TickBoxControllerBuilder::create)
            .build();
    }

    private static Option<String> stringOption(String key, String def, Supplier<String> getter, Consumer<String> setter) {
        return Option.<String>createBuilder()
            .name(Component.translatable(key))
            .binding(def, getter, setter)
            .controller(StringControllerBuilder::create)
            .build();
    }

    private static Option<Float> floatSlider(String key, float def, Supplier<Float> getter, Consumer<Float> setter, float min, float max) {
        return Option.<Float>createBuilder()
            .name(Component.translatable(key))
            .binding(def, getter, setter)
            .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(0.01f))
            .build();
    }

    private static Option<Integer> intSlider(String key, int def, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max) {
        return Option.<Integer>createBuilder()
            .name(Component.translatable(key))
            .binding(def, getter, setter)
            .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1))
            .build();
    }

    private static Option<Color> colorOption(String key, int defRgb, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Color>createBuilder()
            .name(Component.translatable(key))
            .binding(new Color(defRgb), () -> new Color(getter.get()), color -> setter.accept(color.getRGB() & 0xFFFFFF))
            .controller(ColorControllerBuilder::create)
            .build();
    }
}
