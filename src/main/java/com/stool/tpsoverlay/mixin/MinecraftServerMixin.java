package com.stool.tpsoverlay.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.stool.tpsoverlay.metrics.MetricsUtil;
import com.stool.tpsoverlay.metrics.RollingAverage;
import com.stool.tpsoverlay.metrics.TickTimeService;
import com.stool.tpsoverlay.metrics.TickTimes;
import com.stool.tpsoverlay.metrics.TickingState;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BooleanSupplier;

/**
 * TPS/MSPT tracking adapted from TabTPS (MIT License).
 */
@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = TickTimeService.class, prefix = "tpsoverlay$"))
abstract class MinecraftServerMixin {
    @Unique
    private final RollingAverage tps5s = new RollingAverage(5);
    @Unique
    private final RollingAverage tps1m = new RollingAverage(60);
    @Unique
    private final RollingAverage tps5m = new RollingAverage(60 * 5);
    @Unique
    private final RollingAverage tps15m = new RollingAverage(60 * 15);
    @Unique
    private final TickTimes tickTimes5s = new TickTimes(100);
    @Unique
    private final TickTimes tickTimes10s = new TickTimes(200);
    @Unique
    private final TickTimes tickTimes60s = new TickTimes(1200);
    @Unique
    private long previousTime;
    @Unique
    private TickingState tickingState = TickingState.NOT_TICKING;

    @Shadow
    private int tickCount;

    @Shadow
    @Final
    private long[] tickTimesNanos;

    @Inject(
        method = "tickServer",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V",
            ordinal = 0,
            remap = false
        )
    )
    private void injectPause(BooleanSupplier keepTicking, CallbackInfo ci) {
        this.tickingState = TickingState.NOT_TICKING;
    }

    @Inject(method = "tickServer", at = @At(value = "RETURN", ordinal = 1))
    private void injectTick(
        BooleanSupplier keepTicking,
        CallbackInfo ci,
        @Local(ordinal = 0) long tickStartTimeNanos,
        @Local(ordinal = 1) long tickDurationNanos
    ) {
        this.tickTimes5s.add(this.tickCount, tickDurationNanos);
        this.tickTimes10s.add(this.tickCount, tickDurationNanos);
        this.tickTimes60s.add(this.tickCount, tickDurationNanos);

        if (this.tickCount % RollingAverage.SAMPLE_INTERVAL == 0) {
            if (this.tickingState == TickingState.NOT_TICKING) {
                this.tickingState = TickingState.INITIALIZING;
            } else if (this.tickingState == TickingState.INITIALIZING) {
                this.tickingState = TickingState.TICKING;
            }
            long diff = tickStartTimeNanos - this.previousTime;
            this.previousTime = tickStartTimeNanos;
            if (this.tickingState == TickingState.TICKING) {
                BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
                this.tps5s.add(currentTps, diff);
                this.tps1m.add(currentTps, diff);
                this.tps5m.add(currentTps, diff);
                this.tps15m.add(currentTps, diff);
            }
        }
    }

    public double tpsoverlay$averageMspt() {
        return MetricsUtil.toMilliseconds(MetricsUtil.average(this.tickTimesNanos));
    }

    public double[] tpsoverlay$recentTps() {
        return new double[] {
            this.tps5s.average(),
            this.tps1m.average(),
            this.tps5m.average(),
            this.tps15m.average()
        };
    }
}
