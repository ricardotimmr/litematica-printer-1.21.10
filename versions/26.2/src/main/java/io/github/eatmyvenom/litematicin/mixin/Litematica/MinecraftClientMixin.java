package io.github.eatmyvenom.litematicin.mixin.Litematica;

import io.github.eatmyvenom.litematicin.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	@Shadow
	public HitResult hitResult;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public abstract ClientPacketListener getConnection();

	@Shadow
	@Nullable
	public LocalPlayer player;

	// On join a new world/server
	@Inject(at = @At("HEAD"), method = "setLevel")
	//#if MC >= 12006
	public void joinWorld(ClientLevel world, CallbackInfo ci) {
	//#else
	//$$ public void joinWorld(ClientWorld world, CallbackInfo ci) {
	//#endif
		Printer.worldBottomY = world.getMinY();
		Printer.worldTopY = world.getMaxY();
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void onPrinterTickCount(CallbackInfo info) {
		BedrockBreaker.tick();
		InventoryUtils.tick();
		FakeAccurateBlockPlacement.tick(this.getConnection(), this.player);
	}

	@Inject(at = @At("HEAD"), method = "startUseItem")
	public void getIfBlockEntity(CallbackInfo info) {
		if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && (this.level != null ? this.level.getBlockEntity(((BlockHitResult) hitResult).getBlockPos()) : null) != null) {
			ItemInputs.clickedPos = ((BlockHitResult) hitResult).getBlockPos();
		} else {
			ItemInputs.clickedPos = null;
		}
	}
}
