package com.stool.tpsoverlay.mixin.client;

import com.stool.tpsoverlay.client.state.ClientTpsEstimator;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetTimePacket.class)
abstract class ClientboundSetTimePacketMixin {
    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("HEAD"))
    private void tpsoverlay$onTimePacket(ClientGamePacketListener listener, CallbackInfo ci) {
        ClientTpsEstimator.onTimePacket();
    }
}
