package io.github.eatmyvenom.litematicin.utils;

import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
//#if MC >= 12100
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The breaking needs to be done every tick, since the WorldUtils.easyPlaceOnUseTick (which calls our Printer)
 * is called multiple times per tick we cannot break blocks through that method. Or the speed will be twice the
 * normal speed and detectable by anti-cheats.
 */
public class Breaker implements IClientTickHandler {

	private static final Direction BREAK_DIRECTION = Direction.UP;
	private boolean breakingBlock = false;
	private BlockPos pos;
	private Block targetBlock;

	public Breaker() {
		TickHandler.getInstance().registerClientTickHandler(this);
	}

	public boolean startBreakingBlock(BlockPos pos, Minecraft mc) {
		if (mc.level == null || mc.player == null || mc.gameMode == null) {
			this.clearBreakingState();
			return false;
		}
		if (this.breakingBlock && pos.equals(this.pos)) {
			BlockState currentState = mc.level.getBlockState(pos);
			if (!currentState.isAir()
				&& !BedrockBreaker.isReplaceable(currentState)
				&& (this.targetBlock == null || currentState.getBlock() == this.targetBlock)) {
				return true;
			}
		}
		this.clearBreakingState();
		this.pos = pos;
		BlockState blockState = mc.level.getBlockState(pos);
		if (blockState.isAir() || BedrockBreaker.isReplaceable(blockState)) {
			this.clearBreakingState();
			return false;
		}
		this.targetBlock = blockState.getBlock();
		// Check for best tool in inventory
		if (blockState.getDestroySpeed(mc.level, pos) == 0) {
			this.clearBreakingState();
			return mc.gameMode.startDestroyBlock(pos, BREAK_DIRECTION);
		}
		int bestSlotId = getBestItemSlotIdToMineBlock(mc, pos);
		// If slot isn't selected, change
		if (bestSlotId != -1) {
			ItemStack stack = InventoryUtils.getInventory(mc.player).getItem(bestSlotId);
			if (!InventoryUtils.swapToItem(mc, stack)) {
				this.clearBreakingState();
				return false;
			}
		}
		// Start breaking
		//#if MC>=11800
		if (blockState.getDestroyProgress(mc.player, mc.player.level(), pos) >= 1.0F) {
		//#else
		//$$ if (blockState.calcBlockBreakingDelta(mc.player, mc.player.world, pos) >= 1.0F) {
		//#endif
			this.clearBreakingState();
			return mc.gameMode.startDestroyBlock(pos, BREAK_DIRECTION);
		}
		if (!mc.gameMode.startDestroyBlock(pos, BREAK_DIRECTION)) {
			this.clearBreakingState();
			return false;
		}
		if (BedrockBreaker.isReplaceable(mc.level.getBlockState(pos))) {
			this.clearBreakingState();
			return true;
		}
		this.breakingBlock = true;
		TickHandler.getInstance().registerClientTickHandler(this);
		return true;
	}

	public boolean isBreakingBlock() {
		if (this.pos == null || Minecraft.getInstance().level == null) {
			return false;
		}
		return this.breakingBlock;
	}

	private void clearBreakingState() {
		this.breakingBlock = false;
		this.pos = null;
		this.targetBlock = null;
	}

	public static int getBestItemSlotIdToMineBlock(Minecraft mc, BlockPos blockToMine) {
		int bestSlot = -1;
		float bestSpeed = 0;
		BlockState state = mc.level.getBlockState(blockToMine);
		return getFastestToolSlot(mc, bestSlot, bestSpeed, state);
	}

	private static int getFastestToolSlot(Minecraft mc, int bestSlot, float bestSpeed, BlockState state) {
		if (mc.player == null)
		{
			return bestSlot;
		}
		//#if MC>=12105
		int inventorySize = mc.player.getInventory().getNonEquipmentItems().size();
		//#else
		//$$ int inventorySize = mc.player.getInventory().main.size();
		//#endif
		for (int i = inventorySize - 1; i >= 0; i--) {
			float speed = getBlockBreakingSpeed(state, mc, i);
			if (speed <= 1.0F) {
				continue;
			}
			ItemStack itemStack = mc.player.getInventory().getItem(i);
			if (speed > bestSpeed || (speed == bestSpeed && bestSlot != -1 && !itemStack.isDamageableItem())) {
				bestSlot = i;
				bestSpeed = speed;
			}
		}
		return bestSlot;
	}

	public static int getBestItemSlotIdToMineState(Minecraft mc, BlockState state) {
		int bestSlot = -1;
		float bestSpeed = 0;
		return getFastestToolSlot(mc, bestSlot, bestSpeed, state);
	}

	public static float getBlockBreakingSpeed(BlockState block, Minecraft mc, int slotId) {
		if (mc.player == null || slotId < 0) {
			return 0;
		}
		//#if MC>=12105
		if (slotId >= InventoryUtils.getInventory(mc.player).getNonEquipmentItems().size()) {
			return 0;
		}
		ItemStack itemStack = InventoryUtils.getInventory(mc.player).getNonEquipmentItems().get(slotId);
		float f = itemStack.getDestroySpeed(block);
		//#else
		//$$ if (slotId >= InventoryUtils.getInventory(mc.player).main.size()) {
		//$$ 	return 0;
		//$$ }
		//$$ ItemStack itemStack = InventoryUtils.getInventory(mc.player).main.get(slotId);
		//$$ float f = itemStack.getMiningSpeedMultiplier(block);
		//#endif
		if (f > 1.0F) {
			//#if MC>=12102
			Optional<Holder.Reference<Enchantment>> optional = mc.level.registryAccess().get(Enchantments.EFFICIENCY);
			int i =  optional.map(enchantmentReference -> EnchantmentHelper.getItemEnchantmentLevel(enchantmentReference, itemStack)).orElse(0);
			//#elseif MC >= 12100
			//$$ ItemStack itemStack = mc.player.getInventory().getMainHandStack();
			//$$ Optional<RegistryEntry.Reference<Enchantment>> optional = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY);
			//$$ int i =  optional.map(enchantmentReference -> EnchantmentHelper.getLevel(enchantmentReference, itemStack)).orElse(0);
			//#else
			//$$ int i = EnchantmentHelper.getEfficiency(mc.player);
			//$$ ItemStack itemStack = mc.player.getInventory().getMainHandStack();
			//#endif
			if (i > 0 && !itemStack.isEmpty()) {
				f += (float) (i * i + 1);
			}
		}
		return f;
	}

	@Override
	public void onClientTick(Minecraft mc) {
		if (!this.breakingBlock) {
			return;
		}
		if (this.pos == null || mc.player == null || mc.level == null || mc.gameMode == null) {
			this.clearBreakingState();
			return;
		}
		BlockState currentState = mc.level.getBlockState(this.pos);
		if (BedrockBreaker.isReplaceable(currentState)) {
			this.clearBreakingState();
			mc.gameMode.stopDestroyBlock();
			return;
		}
		if (this.targetBlock != null && currentState.getBlock() != this.targetBlock) {
			this.clearBreakingState();
			mc.gameMode.stopDestroyBlock();
			return;
		}

		if (Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld()) { // Only continue mining while the correct keys are pressed
			Direction side = BREAK_DIRECTION;
			if (mc.gameMode.continueDestroyBlock(pos, side)) {
				addBlockBreakParticles(mc, pos, side);
				mc.player.swing(InteractionHand.MAIN_HAND);
			}
		}

		if (!BedrockBreaker.isReplaceable(mc.level.getBlockState(pos))) {
			return;
		} // If block isn't broken yet, dont stop
		// Stop breaking
		this.clearBreakingState();
		mc.gameMode.stopDestroyBlock();
	}

	private static void addBlockBreakParticles(Minecraft mc, BlockPos pos, Direction side) {
		if (mc.level == null) {
			return;
		}
		BlockState state = mc.level.getBlockState(pos);
		if (state.isAir()) {
			return;
		}
		RandomSource random = mc.particleEngine.getRandom();
		for (int i = 0; i < 4; i++) {
			double x = pos.getX() + 0.5D + side.getStepX() * 0.51D + (random.nextDouble() - 0.5D) * 0.4D;
			double y = pos.getY() + 0.5D + side.getStepY() * 0.51D + (random.nextDouble() - 0.5D) * 0.4D;
			double z = pos.getZ() + 0.5D + side.getStepZ() * 0.51D + (random.nextDouble() - 0.5D) * 0.4D;
			double dx = side.getStepX() * 0.05D + (random.nextDouble() - 0.5D) * 0.02D;
			double dy = side.getStepY() * 0.05D + (random.nextDouble() - 0.5D) * 0.02D;
			double dz = side.getStepZ() * 0.05D + (random.nextDouble() - 0.5D) * 0.02D;
			mc.particleEngine.add(new TerrainParticle(mc.level, x, y, z, dx, dy, dz, state, pos).scale(0.6F));
		}
	}

}
