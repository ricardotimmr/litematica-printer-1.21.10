package io.github.eatmyvenom.litematicin.mixin.MinecraftClient;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.tool.ToolMode;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import io.github.eatmyvenom.litematicin.utils.PacketSyncState;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC<=12001
//$$ @Mixin(ClientPlayNetworkHandler.class)
//$$ public class ClientPlayNetworkHandlerMixin {
//$$
//$$ 	@Shadow
//$$ 	@Final
//$$ 	private MinecraftClient client;
//#else
@Mixin(value = ClientPacketListener.class, remap = false)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonPacketListenerImpl{
protected ClientPlayNetworkHandlerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
	super(client, connection, connectionState);
	}
//#endif
	private static final int CONTAINER_STATE_ID_MASK = 32767;

	/*
			SyncHandler defined at ServerPlayerEntity is responsible to sync, but actually client process actions and executes too. so Server sync does not match with client, which causes desync.
			We can see ghost items in this context, especially with high ping. But, if there's no packet loss, whatever client has executed will be done in order correctly, like click recipe -> press Q in result slot even if its empty.
			So server sync is not totally required for most cases.
	 */
	@Inject(method = "handleContainerSetSlot", at = @At("HEAD"), cancellable = true, require = 0)
	private void onUpdateSlots(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
		final LocalPlayer player = this.minecraft.player;
		if (!PacketSyncState.isSynced() && player != null && syncMenuStateId(player, packet.getContainerId(), packet.getStateId())) {
			PacketSyncState.markSynced();
			//player.sendMessage(Text.of("Matched rev on start : "+ packet.getRevision()));
			return;
		}
		if (player != null && player.containerMenu != null && LitematicaMixinMod.DEBUG_PACKET_SYNC.getBooleanValue()) {
			AbstractContainerMenu menu = player.containerMenu;
			int rev = menu.getStateId();
			//MessageHolder.sendPacketOrders("Recieved packet of revision " + packet.getRevision() + " current revision is " + rev);
			if (LitematicaMixinMod.DISABLE_SYNC.getBooleanValue()) {
				if (!(this.minecraft.gui.screen() instanceof CreativeModeInventoryScreen) && shouldCancel(rev, packet.getStateId())) {
					ci.cancel();
				} else {
					syncMenuStateId(player, packet.getStateId());
					if (packet.getSlot() == -1) {
						//okay wtf? server is actually trying to disconnect client.
						if (packet.getContainerId() == -1 && !(this.minecraft.gui.screen() instanceof CreativeModeInventoryScreen)) {
							this.minecraft.execute(() -> menu.setCarried(packet.getItem()));
						}
						return;
					}
					if (packet.getSlot() >= 0 && packet.getSlot() < menu.slots.size()) {
						this.minecraft.execute(() -> menu.setItem(packet.getSlot(), packet.getStateId(), packet.getItem()));
					}
				}
				//MessageHolder.sendMessageUnchecked("Cancelled ");
			}
			return;
		}
		cancelIfRequired(ci);
	}

	@Inject(method = "handleOpenScreen", at = @At("HEAD"), require = 0)
	private void resetSyncStateOnOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
		PacketSyncState.reset();
	}

	@Inject(method = "handleContainerContent", at = @At("TAIL"), require = 0)
	private void syncStateOnContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
		final LocalPlayer player = this.minecraft.player;
		if (player != null && syncMenuStateId(player, packet.containerId(), packet.stateId())) {
			PacketSyncState.markSynced();
		}
	}

	@Inject(method = "handleContainerClose", at = @At("HEAD"), require = 0)
	private void resetSyncStateOnContainerClose(ClientboundContainerClosePacket packet, CallbackInfo ci) {
		PacketSyncState.reset();
	}
	//#if MC<=12001
	//$$ @Inject(method = "onDisconnect", at = @At("HEAD"))
	//$$ private void handleDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
	//$$ 	PacketSyncState.reset();
	//$$ }
	//#endif

	@Inject(method = "handleSetHeldSlot", at = @At("HEAD"), cancellable = true, require = 0)
	private void onUpdateSelectSlots(ClientboundSetHeldSlotPacket packet, CallbackInfo ci) {
		cancelIfRequired(ci);
	}

	private void cancelIfRequired(CallbackInfo ci) {
		if (Printer.isSleeping) {
			return;
		}
		if (DataManager.getToolMode() != ToolMode.REBUILD && Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && Configs.Generic.EASY_PLACE_HOLD_ENABLED.getBooleanValue() && Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld()) {
			if (LitematicaMixinMod.DISABLE_SYNC.getBooleanValue()) {
				ci.cancel();
			}
		}
	}

	private static boolean shouldCancel(int current, int packet) {
		if (current == packet) {
			return false;
		}
		int abs = Math.abs(current - packet);
		if (abs > 1024 && abs < 32760) {
			return false;
		}
		return (Math.abs(current - packet) > 32760) == (current < packet);
	}

	private static boolean syncMenuStateId(LocalPlayer player, int containerId, int stateId) {
		if (player == null) {
			return false;
		}
		if (player.containerMenu != null && player.containerMenu.containerId == containerId) {
			return syncMenuStateId(player.containerMenu, stateId);
		}
		if (containerId == 0 && player.inventoryMenu != null) {
			return syncMenuStateId(player.inventoryMenu, stateId);
		}
		return false;
	}

	private static boolean syncMenuStateId(LocalPlayer player, int stateId) {
		return player != null && syncMenuStateId(player.containerMenu, stateId);
	}

	private static boolean syncMenuStateId(AbstractContainerMenu menu, int stateId) {
		if (stateId < 0) {
			return false;
		}
		if (menu == null) {
			return false;
		}
		((AbstractContainerMenuAccessor) menu).litematicin$setStateId(stateId & CONTAINER_STATE_ID_MASK);
		return true;
	}

}
