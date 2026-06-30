package io.github.eatmyvenom.litematicin.mixin.Litematica;

import fi.dy.masa.litematica.util.EasyPlaceUtils;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import io.github.eatmyvenom.litematicin.utils.MessageHolder;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.*;

@Mixin(value = EasyPlaceUtils.class, remap = false, priority = 1010)
public class WorldUtilsMixin {
	private static boolean hasSent = false;

	/**
	 * @author joe mama // fixed by AngelBottomless
	 */
	//@Overwrite
	@Inject(
		method = "handleEasyPlace",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onDoEasyPlaceAction(CallbackInfoReturnable<InteractionResult> cir) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		if (PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue()) {
			LitematicaMixinMod.USE_INVENTORY_CACHE.setBooleanValue(false);
			cir.setReturnValue(Printer.doEasyPlaceFakeRotation(mc));
		}
		else {
			if (LitematicaMixinMod.PRINTER_OFF.getBooleanValue()) {
				return;
			}
			InteractionResult defaultResult = InteractionResult.SUCCESS;
			try {
				defaultResult = Printer.doPrinterAction(mc);
			} catch (NullPointerException e) {
				//in case of NPE, print log instead
				MessageHolder.sendMessageUncheckedUnique(mc.player, e.getMessage());
				if (!hasSent && mc.player != null) {
					//#if MC>=12102
					MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Null pointer exception has occured, please upload log at https://github.com/aria1th/litematica-printer/issues"), false);
					//#elseif MC>=11900
					//$$ mc.player.sendMessage(Text.of("Null pointer exception has occured, please upload log at https://github.com/aria1th/litematica-printer/issues"));
					//#else
					//$$mc.player.sendMessage(new LiteralText("Null pointer exception has occured, please upload log at https://github.com/aria1th/litematica-printer/issues"), false);
					//#endif
					hasSent = true;
				}
			} catch (AssertionError e) {
				MessageHolder.sendOrderMessage("Order error happened " + e.getMessage());
				MessageHolder.sendMessageUncheckedUnique(mc.player, "Order Error Happened " + e.getMessage());
				cir.setReturnValue(InteractionResult.FAIL);
				return;
			}
			cir.setReturnValue(defaultResult);
			//return defaultResult;
		}
	}

	@Inject(
		method = "handleEasyPlace",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
			ordinal = 0
		),
		require = 0
	)
	private static void swingHandAfterPlace(CallbackInfoReturnable<InteractionResult> cir) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		if (PRINTER_OFF.getBooleanValue() && PRINTER_SHOULD_SWING_HAND.getBooleanValue()) {
			mc.player.swing(mc.player.getUsedItemHand());
		}
	}

	@Inject(
		method = "handleEasyPlace",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
			ordinal = 1
		),
		require = 0
	)
	private static void swingHandAfterPlaceDoubleSlab(CallbackInfoReturnable<InteractionResult> cir) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		if (PRINTER_OFF.getBooleanValue() && PRINTER_SHOULD_SWING_HAND.getBooleanValue()) {
			mc.player.swing(mc.player.getUsedItemHand());
		}
	}
}
