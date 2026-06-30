package io.github.eatmyvenom.litematicin.mixin.MinecraftClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "handleContainerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), require = 1)
	private void getNextRevision(int syncId, int slotId, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
		AbstractContainerMenu menu = getMenuForContainer(player, syncId);
		if (menu != null) {
			menu.incrementStateId();
		}
	}

	@Inject(method = "handleCreativeModeItemAdd", at = @At("TAIL"), require = 1)
	private void getNextRevision(ItemStack stack, int slotId, CallbackInfo ci) {
		final LocalPlayer player = this.minecraft.player;
		if (player != null) {
			AbstractContainerMenu menu = player.containerMenu;
			if (menu != null
				&& !(menu instanceof CreativeModeInventoryScreen.ItemPickerMenu)
				&& slotId >= 0
				&& slotId < menu.slots.size()
				&& !ItemStack.matches(menu.getSlot(slotId).getItem(), stack)) {
				menu.incrementStateId();
				//player.sendMessage(Text.of("Slot was "+ slotId+ " stack was " +stack));
				menu.setItem(slotId, menu.getStateId(), stack);
			}
		}
	}

	private static AbstractContainerMenu getMenuForContainer(Player player, int syncId) {
		if (player == null) {
			return null;
		}
		if (player.containerMenu != null && player.containerMenu.containerId == syncId) {
			return player.containerMenu;
		}
		if (player.inventoryMenu != null && player.inventoryMenu.containerId == syncId) {
			return player.inventoryMenu;
		}
		return null;
	}
}
