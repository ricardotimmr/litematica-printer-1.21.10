package io.github.eatmyvenom.litematicin.mixin.MinecraftClient;

import io.github.eatmyvenom.litematicin.utils.PacketSyncState;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientCommonPacketListenerImpl.class, remap = false)
public abstract class ClientCommonPacketListenerImplMixin {
	@Inject(method = "onDisconnect", at = @At("HEAD"), require = 0)
	private void resetPrinterSyncState(DisconnectionDetails disconnectionInfo, CallbackInfo ci) {
		PacketSyncState.reset();
	}
}
