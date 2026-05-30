package com.stool.tpsoverlay.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.metrics.MetricsAccess;
import com.stool.tpsoverlay.metrics.MetricsUtil;
import com.stool.tpsoverlay.metrics.TickTimeService;
import com.stool.tpsoverlay.networking.ClientActionPayload;
import com.stool.tpsoverlay.networking.TpsOverlayNetworking;
import com.stool.tpsoverlay.util.TpsOverlayChat;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class TpsOverlayCommands {
    private static final HelpEntry[] HELP_ENTRIES = {
        new HelpEntry("/tpso edit", "tpsoverlay.command.help.tpso_edit"),
        new HelpEntry("/tpso toggle", "tpsoverlay.command.help.tpso_toggle"),
        new HelpEntry("/tpso config", "tpsoverlay.command.help.tpso_config"),
        new HelpEntry("/tpso reset", "tpsoverlay.command.help.tpso_reset"),
        new HelpEntry("/tpso reload", "tpsoverlay.command.help.tpso_reload"),
        new HelpEntry("/tpso scale <value>", "tpsoverlay.command.help.tpso_scale"),
        new HelpEntry("/tpso help", "tpsoverlay.command.help.tpso_help"),
        new HelpEntry("/tping", "tpsoverlay.command.help.tping"),
        new HelpEntry("/tping <player>", "tpsoverlay.command.help.tping_player"),
        new HelpEntry("/tpsc", "tpsoverlay.command.help.tpsc"),
        new HelpEntry("/tpsc detailed", "tpsoverlay.command.help.tpsc_detailed"),
    };

    private TpsOverlayCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(TpsOverlayCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("tpso")
            .executes(ctx -> showNoArgsHint(ctx.getSource()))
            .then(Commands.literal("help")
                .executes(ctx -> showHelp(ctx.getSource())))
            .then(Commands.literal("edit")
                .executes(ctx -> sendClientAction(ctx.getSource(), ClientActionPayload.ClientAction.TOGGLE_EDIT)))
            .then(Commands.literal("toggle")
                .executes(ctx -> sendClientAction(ctx.getSource(), ClientActionPayload.ClientAction.TOGGLE_VISIBLE)))
            .then(Commands.literal("config")
                .executes(ctx -> sendClientAction(ctx.getSource(), ClientActionPayload.ClientAction.OPEN_CONFIG)))
            .then(Commands.literal("reset")
                .executes(ctx -> sendClientAction(ctx.getSource(), ClientActionPayload.ClientAction.RESET_POSITION)))
            .then(Commands.literal("reload")
                .executes(ctx -> reloadConfig(ctx.getSource())))
            .then(Commands.literal("scale")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0.5f, 2.0f))
                    .executes(ctx -> setScale(ctx, FloatArgumentType.getFloat(ctx, "value"))))));

        dispatcher.register(Commands.literal("tping")
            .executes(ctx -> pingSelf(ctx.getSource()))
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> pingPlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("tpsc")
            .executes(ctx -> printMetrics(ctx.getSource(), false))
            .then(Commands.literal("detailed")
                .executes(ctx -> printMetrics(ctx.getSource(), true))));
    }

    private static int showNoArgsHint(CommandSourceStack source) {
        source.sendSuccess(() -> TpsOverlayChat.prefixedGray(Component.translatable("tpsoverlay.command.tpso.no_args")), false);
        return 1;
    }

    private static int showHelp(CommandSourceStack source) {
        source.sendSuccess(() -> TpsOverlayChat.prefixed(buildHelpHeader()), false);
        for (HelpEntry entry : HELP_ENTRIES) {
            source.sendSuccess(() -> TpsOverlayChat.prefixed(buildHelpLine(entry)), false);
        }
        return 1;
    }

    private static Component buildHelpHeader() {
        return Component.literal("=====")
            .withStyle(ChatFormatting.AQUA)
            .append(Component.literal(" TPS Overlay ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("=====").withStyle(ChatFormatting.AQUA));
    }

    private static MutableComponent buildHelpLine(HelpEntry entry) {
        return Component.literal(entry.command)
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" - ").withStyle(ChatFormatting.WHITE))
            .append(Component.translatable(entry.descriptionKey).withStyle(ChatFormatting.WHITE));
    }

    private static int sendClientAction(CommandSourceStack source, ClientActionPayload.ClientAction action) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(TpsOverlayChat.prefixedGray(Component.translatable("tpsoverlay.command.player_only")));
            return 0;
        }
        TpsOverlayNetworking.sendClientAction(player, action);
        return 1;
    }

    private static int reloadConfig(CommandSourceStack source) {
        TpsOverlayConfigHandler.load();
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            TpsOverlayNetworking.sendClientAction(player, ClientActionPayload.ClientAction.RELOAD_CONFIG);
        }
        source.sendSuccess(() -> TpsOverlayChat.prefixedGray(Component.translatable("tpsoverlay.command.tpso.reloaded")), false);
        return 1;
    }

    private static int setScale(CommandContext<CommandSourceStack> ctx, float value) {
        TpsOverlayConfigHandler.getConfig().textScale = value;
        TpsOverlayConfigHandler.save();
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            TpsOverlayNetworking.sendClientAction(player, ClientActionPayload.ClientAction.RELOAD_CONFIG);
        }
        ctx.getSource().sendSuccess(() -> TpsOverlayChat.prefixedGray(Component.translatable("tpsoverlay.command.tpso.scale_set", value)), false);
        return 1;
    }

    private static int pingSelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(TpsOverlayChat.prefixedGray(Component.translatable("tpsoverlay.command.player_only")));
            return 0;
        }
        int ping = player.connection.latency();
        source.sendSuccess(() -> TpsOverlayChat.prefixed(Component.translatable("tpsoverlay.command.ping.self",
            MetricsUtil.coloredPing(ping, true),
            Component.literal("ms").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    private static int pingPlayer(CommandSourceStack source, ServerPlayer target) {
        int ping = target.connection.latency();
        source.sendSuccess(() -> TpsOverlayChat.prefixed(Component.translatable("tpsoverlay.command.ping.other",
            target.getDisplayName(),
            MetricsUtil.coloredPing(ping, true),
            Component.literal("ms").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    private static int printMetrics(CommandSourceStack source, boolean detailed) {
        MinecraftServer server = source.getServer();
        TickTimeService service = MetricsAccess.of(server);
        double mspt = service.averageMspt();
        double[] tps = service.recentTps();

        if (!detailed) {
            source.sendSuccess(() -> TpsOverlayChat.prefixed(Component.translatable("tpsoverlay.command.tpsc.summary",
                MetricsUtil.coloredTps(tps[0], true),
                MetricsUtil.coloredMspt(mspt, true)).withStyle(ChatFormatting.GRAY)), false);
            return 1;
        }

        source.sendSuccess(() -> TpsOverlayChat.prefixed(Component.translatable("tpsoverlay.command.tpsc.detailed",
            MetricsUtil.coloredMspt(mspt, true),
            MetricsUtil.coloredTps(tps[0], true),
            MetricsUtil.coloredTps(tps[1], true),
            MetricsUtil.coloredTps(tps[2], true),
            MetricsUtil.coloredTps(tps[3], true)).withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    private record HelpEntry(String command, String descriptionKey) {
    }
}
