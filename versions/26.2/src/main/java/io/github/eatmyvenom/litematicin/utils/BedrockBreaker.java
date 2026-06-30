package io.github.eatmyvenom.litematicin.utils;

import org.jetbrains.annotations.Nullable;

import java.util.*;
//#if MC<11700
//$$ import java.util.stream.Collectors;
//#endif
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.*;
import static io.github.eatmyvenom.litematicin.utils.InventoryUtils.getSlotWithStack;

// since 1.19, you can't swap items too fast (huh)
@SuppressWarnings("ConstantConditions")
public class BedrockBreaker {
	public static long lastPlaced = System.currentTimeMillis();
	public static long CurrentTick = 0L;
	private static final long PISTON_TOOL_RETRY_COOLDOWN_TICKS = 20L;
	private static long lastPistonToolFailureTick = -PISTON_TOOL_RETRY_COOLDOWN_TICKS;
	//#if MC>=11700
	static final Direction[] HORIZONTAL = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
	//#else
	//$$ static List<Direction> HORIZONTAL = Arrays.asList(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);
	//#endif
	private static final Direction[] ALL_DIRECTIONS = Direction.values();
	private static final Map<Long, PositionCache> targetPosMap = new LinkedHashMap<>();
	static int rangeX = EASY_PLACE_MODE_RANGE_X.getIntegerValue();
	static int rangeY = EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
	static int rangeZ = EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
	static int MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);

	private static void refreshReachFromConfig() {
		rangeX = EASY_PLACE_MODE_RANGE_X.getIntegerValue();
		rangeY = EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
		rangeZ = EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
		MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);
	}

	private static double maxReachSquared() {
		return (double) MaxReach * MaxReach;
	}

	private static void recordPlacementInteraction() {
		lastPlaced = System.currentTimeMillis();
	}

	public static void clear() {
		targetPosMap.clear();
		positionStorage.clear();
		lastPlaced = 0L;
		lastPistonToolFailureTick = -PISTON_TOOL_RETRY_COOLDOWN_TICKS;
	}

	public static boolean isReplaceable(BlockState state) {
		//#if MC>=12000
		return state.canBeReplaced();
		//#else
		//$$ return state.getMaterial().isReplaceable();
		//#endif
	}

	private static boolean shouldExtend(Level world, BlockPos pos, Direction pistonFace) {
		for (Direction direction : ALL_DIRECTIONS) {
			if (direction != pistonFace && world.hasSignal(pos.relative(direction), direction)) {
				return true;
			}
		}
		if (world.hasSignal(pos, Direction.DOWN)) {
			return true;
		} else {
			BlockPos blockPos = pos.above();
			for (Direction qcDirections : ALL_DIRECTIONS) {
				if (qcDirections != Direction.DOWN && world.hasSignal(blockPos.relative(qcDirections), qcDirections)) {
					return true;
				}
			}
			return false;
		}
	}

	@Nullable
	public static TorchPath getPistonTorchPosDir(Minecraft mc, BlockPos bedrockPos) {
		for (Direction lv : ALL_DIRECTIONS) {
			if (!PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
				if (lv != Direction.DOWN && lv != Direction.UP) {
					continue;
				}
			}
			BlockPos pistonPos = bedrockPos.relative(lv);
			if (!canReplaceTarget(mc, pistonPos)) {
				continue;
			}
			for (Direction pistonFacing : ALL_DIRECTIONS) {
				if (pistonFacing.getOpposite() == lv) {
					continue;
				}
				BlockPos checkAir = pistonPos.relative(pistonFacing);
				if (!isBlockPosinYRange(checkAir)) {
					continue;
				}
				if (shouldExtend(mc.level, pistonPos, pistonFacing)) {
					continue;
				}
				BlockState checkAirState = mc.level.getBlockState(checkAir);
				if (checkAirState.isAir() || isReplaceable(checkAirState)) {
					TorchData torchdata = getPossiblePowerableTorchPosFace(mc, bedrockPos, pistonPos, checkAir);
					if (torchdata != null) {
						TorchPath torchPath = new TorchPath(torchdata.TorchPos, torchdata.Torchfacing, pistonPos, pistonFacing, lv.getOpposite());
						if (torchdata.SlimePos != null) {
							torchPath.slimePos = torchdata.SlimePos;
						}
						return torchPath;
					}
				}
			}
		}
		return null;
	}

	public static boolean isBlockPosinYRange(BlockPos checkPos) {
		int y = checkPos.getY();
		return y >= Printer.worldBottomY && y < Printer.worldTopY;
	}

	@Nullable
	public static TorchData getPossiblePowerableTorchPosFace(Minecraft mc, BlockPos pos1, BlockPos pistonPos, BlockPos pos2) {
		Level world = mc.level;
		boolean forceSlimeBlock = PRINTER_BEDROCK_BREAKING_USE_SLIMEBLOCK.getBooleanValue();
		boolean canUseSlimeBlock = forceSlimeBlock && canPlaceSlime(mc);
		for (Direction hd : HORIZONTAL) { //normal 4 dir
			BlockPos torchCheck = pistonPos.relative(hd);
			if (checkTorchPosition(pos1, pos2, world, torchCheck)) {
				continue;
			}
			// check torch can be placed
			TorchData floorTorchData = getFloorTorchData(world, torchCheck, pos1, pistonPos, pos2, canUseSlimeBlock);
			if (floorTorchData != null) {
				return floorTorchData;
			}
			if (!canUseSlimeBlock) {
				for (Direction hd2 : HORIZONTAL) {
					if (hd2 == hd) {
						continue;
					}
					if (canPlaceAt(hd2, world, torchCheck)) {
						return new TorchData(torchCheck, hd2);
					}
				}
			}
		}
		for (Direction hd : HORIZONTAL) { //qc
			BlockPos torchCheck = pistonPos.above().relative(hd);
			if (checkTorchPosition(pos1, pos2, world, torchCheck)) {
				continue;
			}
			// check torch can be placed
			TorchData floorTorchData = getFloorTorchData(world, torchCheck, pos1, pistonPos, pos2, canUseSlimeBlock);
			if (floorTorchData != null) {
				return floorTorchData;
			}
			if (!canUseSlimeBlock) {
				for (Direction hd2 : HORIZONTAL) {
					if (hd2 == hd) {
						continue;
					}
					if (canPlaceAt(hd2, world, torchCheck)) {
						return new TorchData(torchCheck, hd2);
					}
				}
			}
		}
		BlockPos torchCheck = pistonPos.below(); // down
		if (!torchCheck.equals(pos2) && isBlockPosinYRange(torchCheck)) {
			if (torchCheck.equals(pos1) || torchCheck.equals(pos2)) {
				return null;
			}
			BlockState torchState = world.getBlockState(torchCheck);
			if (!torchState.isAir() && !isReplaceable(torchState)) {
				return null;
			}
			TorchData floorTorchData = getFloorTorchData(world, torchCheck, pos1, pistonPos, pos2, canUseSlimeBlock);
			if (floorTorchData != null) {
				return floorTorchData;
			}
		}
		return null;
	}

	@Nullable
	private static TorchData getFloorTorchData(Level world, BlockPos torchCheck, BlockPos bedrockPos, BlockPos pistonPos, BlockPos extensionPos, boolean canUseSlimeBlock) {
		BlockPos supportPos = torchCheck.below();
		if (!isBlockPosinYRange(supportPos)) {
			return null;
		}
		BlockState supportState = world.getBlockState(supportPos);
		if (!supportState.is(Blocks.PISTON) && Block.canSupportCenter(world, supportPos, Direction.UP)) {
			return new TorchData(torchCheck, Direction.UP);
		}
		if (!canUseSlimeBlock || supportPos.equals(bedrockPos) || supportPos.equals(pistonPos) || supportPos.equals(extensionPos)) {
			return null;
		}
		if (!supportState.isAir() && !isReplaceable(supportState)) {
			return null;
		}
		TorchData torchData = new TorchData(torchCheck, Direction.UP);
		torchData.registerSlimePos(supportPos);
		return torchData;
	}

	private static boolean checkTorchPosition(BlockPos pos1, BlockPos pos2, Level world, BlockPos torchCheck) {
		if (!isBlockPosinYRange(torchCheck)) {
			return true;
		}
		if (torchCheck.equals(pos1) || torchCheck.equals(pos2)) {
			return true;
		}
		BlockState torchState = world.getBlockState(torchCheck);
		return !torchState.isAir() && !isReplaceable(torchState);
	}

	public static void removeScheduledPos(Minecraft mc) {
		Iterator<Map.Entry<Long, PositionCache>> iterator = targetPosMap.entrySet().iterator();
		while (iterator.hasNext()) {
			PositionCache item = iterator.next().getValue();
			if (item == null
				|| item.canSafeRemove(mc.level)
				|| (CurrentTick - item.SysTime > 400L && !item.hasTemporaryBlocks(mc.level))) {
				iterator.remove();
			}
		}
	}

	public static boolean canPlaceAt(Direction lv, Level world, BlockPos pos) {
		BlockPos lv2 = pos.relative(lv.getOpposite());
		if (!isBlockPosinYRange(lv2)) {
			return false;
		}
		BlockState lv3 = world.getBlockState(lv2);
		if (lv3.is(Blocks.PISTON)) {
			return false;
		}
		return lv3.isFaceSturdy(world, lv2, lv);
	}

	private static boolean canReplaceTarget(Minecraft mc, BlockPos pos) {
		if (!isBlockPosinYRange(pos)) {
			return false;
		}
		BlockState state = mc.level.getBlockState(pos);
		return state.isAir() || isReplaceable(state);
	}

	public static boolean placePiston(Minecraft mc, BlockPos pos, Direction facing) {
		if (!canReplaceTarget(mc, pos)) {
			return false;
		}
		final ItemStack PistonStack = Items.PISTON.getDefaultInstance();
		if (!InventoryUtils.swapToItem(mc, PistonStack)) {
			return false;
		}
		MessageHolder.sendDebugMessage("Places piston at " + pos.toShortString() + " with facing " + facing);
		//mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
		if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
			return placeViaCarpet(mc, pos, facing);
		}
		return placeViaPacketReversed(mc, pos, facing, false);
	}

	public static boolean placePiston(Minecraft mc, BlockPos pos, Direction facing, boolean sync) {
		if (!canReplaceTarget(mc, pos)) {
			return false;
		}
		final ItemStack PistonStack = Items.PISTON.getDefaultInstance();
		if (!InventoryUtils.swapToItem(mc, PistonStack)) {
			return false;
		}
		if (sync) {
			//#if MC>=12105
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(getInventory(mc).getSelectedSlot()));
			//#else
			//$$ mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(getInventory(mc).selectedSlot));
			//#endif
		}
		MessageHolder.sendDebugMessage("Places piston at " + pos.toShortString() + " with facing " + facing);
		//mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
		if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
			return placeViaCarpet(mc, pos, facing);
		}
		return placeViaPacketReversed(mc, pos, facing, false);
	}

	public static boolean placeSlime(Minecraft mc, BlockPos pos) {
		if (!canReplaceTarget(mc, pos)) {
			return false;
		}
		final ItemStack SlimeStack = Items.SLIME_BLOCK.getDefaultInstance();
		if (!InventoryUtils.swapToItem(mc, SlimeStack)) {
			return false;
		}
		MessageHolder.sendDebugMessage("Places slime at " + pos.toShortString());
		//mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
		if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
			return placeViaCarpet(mc, pos, Direction.UP);
		}
		return placeViaPacketReversed(mc, pos, Direction.UP, false);
	}

	public static boolean placeViaCarpet(Minecraft mc, BlockPos pos, Direction facing) {
		Vec3 hitVec = new Vec3(pos.getX() + 2 + (facing.get3DDataValue() * 2), pos.getY(), pos.getZ());
		BlockHitResult hitResult = new BlockHitResult(hitVec, facing, pos, false);
		InteractionResult result = handleTweakPlacementPacket(mc, hitResult);
		if (result.consumesAction()) {
			positionStorage.registerPos(pos, true);
			recordPlacementInteraction();
			return true;
		}
		return false;
	}

	// Wrapper function for interacting with blocks
	public static InteractionResult interactBlock(Minecraft mc, BlockHitResult hitResult) {
		//#if MC>=11900
		InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
		//#else
		//$$ ActionResult result =  mc.interactionManager.interactBlock(mc.player, mc.player.clientWorld, Hand.MAIN_HAND, hitResult);
		//#endif
		//#if MC>=12102
		if (PRINTER_SHOULD_SWING_HAND.getBooleanValue() && result.consumesAction() && result.equals(InteractionResult.SUCCESS)) {
		//#else
		//$$ if (PRINTER_SHOULD_SWING_HAND.getBooleanValue() && result.isAccepted() && result.shouldSwingHand()) {
		//#endif
			mc.player.swing(InteractionHand.MAIN_HAND);
		}
		return result;
	}

	public static boolean placeViaPacketReversed(Minecraft mc, BlockPos pos, Direction facing, boolean ShouldOffset) {
		int px = pos.getX();
		int py = pos.getY();
		int pz = pos.getZ();
		Vec3 hitPos = new Vec3(px, py, pz);
		if (ShouldOffset) {
			if (facing == Direction.DOWN) {
				py += 0;
			} else if (facing == Direction.UP) {
				py += 0;
			} else if (facing == Direction.NORTH) {
				pz += 1;
			} else if (facing == Direction.SOUTH) {
				pz -= 1;
			} else if (facing == Direction.EAST) {
				px -= 1;
			} else if (facing == Direction.WEST) {
				px += 1;
			}
		}
		BlockPos npos = new BlockPos(px, py, pz);
		if (ShouldOffset) {
			hitPos = Printer.applyTorchHitVec(npos, new Vec3(0.5, 0.5, 0.5), facing);
			if (facing == Direction.DOWN) {
				facing = Direction.UP;
			}
		}
		//#if MC<11700
		//$$ float OriginPitch = mc.player.pitch;
		//$$ float OriginYaw = mc.player.yaw;
		//#else
		float OriginPitch = mc.player.getXRot();
		float OriginYaw = mc.player.getYRot();
		//#endif
		//#if MC>=12102
		boolean horizontalCollision = mc.player.horizontalCollision;
		if (facing == Direction.DOWN) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(OriginYaw, -90.0f, mc.player.onGround(), horizontalCollision));
		} else if (facing == Direction.UP) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(OriginYaw, 90.0f, mc.player.onGround(), horizontalCollision));
		} else if (facing == Direction.EAST) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(90.0f, OriginPitch, mc.player.onGround(), horizontalCollision));
		} else if (facing == Direction.WEST) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(-90.0f, OriginPitch, mc.player.onGround(), horizontalCollision));
		} else if (facing == Direction.NORTH) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(0.0f, OriginPitch, mc.player.onGround(), horizontalCollision));
		} else if (facing == Direction.SOUTH) {
			mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(180.0f, OriginPitch, mc.player.onGround(), horizontalCollision));
		}
		BlockHitResult hitResult = new BlockHitResult(hitPos, facing, npos, false);
		InteractionResult result = handleTweakPlacementPacket(mc, hitResult);
		mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(OriginYaw, OriginPitch, mc.player.onGround(), horizontalCollision));
		if (result.consumesAction()) {
			positionStorage.registerPos(pos, true);
			recordPlacementInteraction();
			return true;
		}
		return false;
		//#else
		//$$ if (facing == Direction.DOWN) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(OriginYaw, -90.0f, mc.player.isOnGround()));
		//$$ } else if (facing == Direction.UP) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(OriginYaw, 90.0f, mc.player.isOnGround()));
		//$$ } else if (facing == Direction.EAST) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(90.0f, OriginPitch, mc.player.isOnGround()));
		//$$ } else if (facing == Direction.WEST) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-90.0f, OriginPitch, mc.player.isOnGround()));
		//$$ } else if (facing == Direction.NORTH) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(0.0f, OriginPitch, mc.player.isOnGround()));
		//$$ } else if (facing == Direction.SOUTH) {
		//$$ 	mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180.0f, OriginPitch, mc.player.isOnGround()));
		//$$ }
		//$$ BlockHitResult hitResult = new BlockHitResult(hitPos, facing, npos, false);
		//$$ handleTweakPlacementPacket(mc, hitResult);
		//$$ mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(OriginYaw, OriginPitch, mc.player.isOnGround()));
		//#endif
	}

	public static InteractionResult handleTweakPlacementPacket(Minecraft mc, BlockHitResult hitResult) {
		//mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 64));
		return interactBlock(mc, hitResult);
	}

	private static boolean canPlaceTorchAt(Level world, BlockPos pos, Direction torchFacing) {
		BlockPos supportPos = torchFacing.getAxis() == Direction.Axis.Y ? pos.below() : pos.relative(torchFacing.getOpposite());
		if (!isBlockPosinYRange(supportPos)) {
			return false;
		}
		BlockState supportState = world.getBlockState(supportPos);
		if (supportState.is(Blocks.PISTON)) {
			return false;
		}
		if (torchFacing.getAxis() == Direction.Axis.Y) {
			return Block.canSupportCenter(world, supportPos, Direction.UP);
		}
		return supportState.isFaceSturdy(world, supportPos, torchFacing);
	}

	public static boolean placeTorch(Minecraft mc, BlockPos pos, Direction torchFacing) {
		if (!canReplaceTarget(mc, pos)) {
			return false;
		}
		if (!canPlaceTorchAt(mc.level, pos, torchFacing)) {
			return false;
		}
		final ItemStack redstoneTorchStack = Items.REDSTONE_TORCH.getDefaultInstance();
		if (!InventoryUtils.swapToItem(mc, redstoneTorchStack)) {
			return false;
		}
		BlockPos npos;
		if (torchFacing.getAxis() == Direction.Axis.Y) {
			npos = pos.below();
			torchFacing = Direction.UP;
		} else {
			npos = pos.relative(torchFacing.getOpposite());
		}
		MessageHolder.sendDebugMessage("Places torch at " + pos.toShortString() + " with facing " + torchFacing);
		Vec3 hitVec = Vec3.atCenterOf(npos).add(Vec3.atLowerCornerOf(torchFacing.getUnitVec3i()).scale(0.5));
		BlockHitResult hitResult = new BlockHitResult(hitVec, torchFacing, npos, false);
		MessageHolder.sendDebugMessage("Hitresult is " + hitVec.toString() + " " + npos.toShortString());
		InteractionResult result = interactBlock(mc, hitResult);
		if (result.consumesAction()) {
			positionStorage.registerPos(pos, true);
			recordPlacementInteraction();
			return true;
		}
		return false;
	}


	public static boolean canProcess(Minecraft mc, BlockPos pos) {
		double SafetyDistance = PRINTER_BEDROCK_BREAKING_RANGE_SAFE.getIntegerValue();
		if (positionAnyNear(mc, pos, SafetyDistance)) {
			return false;
		}
		PositionCache item = targetPosMap.get(pos.asLong());
		if (item != null) {
			return item.isIdle() && !item.hasTemporaryBlocks(mc.level);
		}
		return true;
	}

	public static boolean positionAnyNear(Minecraft mc, BlockPos pos, double distance) {
		double distanceSquared = distance * distance;
		for (PositionCache item : targetPosMap.values()) {
			if (item == null) {
				continue;
			}
			if (item.distanceSquaredLessThan(pos, distanceSquared) && (!item.isIdle() || item.hasTemporaryBlocks(mc.level))) {
				return true;
			}
		}
		return false;
	}

	private static Inventory getInventory(Minecraft mc) {
		//#if MC<11700
		//$$ return mc.player.inventory;
		//#else
		return mc.player.getInventory();
		//#endif
	}

	public static boolean isItemPrePared(Minecraft mc) {
		Inventory inv = getInventory(mc);
		ItemStack PistonStack = Items.PISTON.getDefaultInstance();
		ItemStack RedstoneTorchStack = Items.REDSTONE_TORCH.getDefaultInstance();
		return getSlotWithStack(inv, PistonStack) != -1
			&& getSlotWithStack(inv, RedstoneTorchStack) != -1
			&& Breaker.getBestItemSlotIdToMineState(mc, Blocks.PISTON.defaultBlockState()) != -1;
	}

	public static boolean canPlaceSlime(Minecraft mc) {
		Inventory inv = getInventory(mc);
		ItemStack SlimeStack = Items.SLIME_BLOCK.getDefaultInstance();
		return getSlotWithStack(inv, SlimeStack) != -1;
	}

	public static boolean switchTool(Minecraft mc) {
		int bestSlotId = Breaker.getBestItemSlotIdToMineState(mc, Blocks.PISTON.defaultBlockState());
		if (bestSlotId == -1) {
			return false;
		}
		ItemStack stack = getInventory(mc).getItem(bestSlotId);
		MessageHolder.sendDebugMessage("Swaps to Pickaxe " + stack);
		if (!InventoryUtils.swapToItem(mc, stack)) {
			return false;
		}
		MessageHolder.sendDebugMessage("Holding stack " + mc.player.getMainHandItem());
		return true;
	}

	private static boolean switchCleanupPistonTool(Minecraft mc) {
		if (isHoldingPistonTool(mc)) {
			return true;
		}
		if (CurrentTick - lastPistonToolFailureTick < PISTON_TOOL_RETRY_COOLDOWN_TICKS) {
			return false;
		}
		boolean swapped = switchTool(mc);
		if (!swapped) {
			lastPistonToolFailureTick = CurrentTick;
		}
		return swapped;
	}

	private static boolean isHoldingPistonTool(Minecraft mc) {
		if (mc.player == null) {
			return false;
		}
		int selectedSlot = getInventory(mc).getSelectedSlot();
		return Breaker.getBlockBreakingSpeed(Blocks.PISTON.defaultBlockState(), mc, selectedSlot) > 1.0F;
	}


	public static boolean attackBlock(Minecraft mc, BlockPos pos, Direction direction) {
		if (!isBlockPosinYRange(pos)) {
			return false;
		}
		if (mc.level.getBlockState(pos).isAir()) {
			return false;
		}
		//#if MC>=11900
		mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, 64));
		if (PRINTER_SHOULD_SWING_HAND.getBooleanValue()) {
			mc.player.swing(InteractionHand.MAIN_HAND);
		}
		//#else
		//$$ mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction));
		//#endif
		//positionStorage.registerPos(pos, false);
		return true;
	}

	private static boolean attackTemporaryTorch(Minecraft mc, BlockPos pos, Direction direction) {
		BlockState state = mc.level.getBlockState(pos);
		if (!isTemporaryTorch(state)) {
			return false;
		}
		return attackBlock(mc, pos, direction);
	}

	private static boolean attackTemporaryPiston(Minecraft mc, BlockPos pos, Direction direction) {
		if (!isTemporaryPiston(mc.level.getBlockState(pos))) {
			return false;
		}
		return attackBlock(mc, pos, direction);
	}

	private static boolean attackTemporarySlime(Minecraft mc, BlockPos pos, Direction direction) {
		if (!isTemporarySlime(mc.level.getBlockState(pos))) {
			return false;
		}
		return attackBlock(mc, pos, direction);
	}

	private static boolean attackTemporaryCleanupBlock(Minecraft mc, BlockPos pos, BlockState state, Direction direction, boolean canBreakPiston) {
		if (isTemporaryTorch(state) || isTemporarySlime(state) || canBreakPiston && isTemporaryPiston(state)) {
			return attackBlock(mc, pos, direction);
		}
		return false;
	}

	private static boolean isTemporaryTorch(BlockState state) {
		return state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH);
	}

	private static boolean isTemporaryPiston(BlockState state) {
		return state.is(Blocks.PISTON);
	}

	private static boolean isTemporarySlime(BlockState state) {
		return state.is(Blocks.SLIME_BLOCK);
	}


	public static boolean isBlockNotInstantBreakable(Block block) {
		return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN;
	}

	public static boolean isPositionInRange(Minecraft mc, BlockPos pos) {
		return isBlockCenterWithinReach(mc.player.getX(), mc.player.getY(), mc.player.getZ(), pos, maxReachSquared());
	}

	private static boolean isBlockCenterWithinReach(double playerX, double playerY, double playerZ, BlockPos pos, double maxReachSquared) {
		double dx = playerX - pos.getX() - 0.5;
		double dy = playerY - pos.getY() - 0.5;
		double dz = playerZ - pos.getZ() - 0.5;
		return dx * dx + dy * dy + dz * dz < maxReachSquared;
	}

	public static int processRemainder(Minecraft mc, int maxInteract) {
		if (maxInteract <= 0 || mc.player == null || mc.level == null) {
			return 0;
		}
		int ret = 0;
		ArrayList<BlockPos> attackList = positionStorage.getFalseMarkedHasBlockPosInAttackRange(mc.level, mc.player.position(), MaxReach, maxInteract);
		if (attackList.isEmpty()) {
			return ret;
		}
		boolean canBreakPiston = false;
		boolean triedPistonTool = false;
		for (BlockPos position : attackList) {
			if (ret >= maxInteract) {
				return ret;
			}
			BlockState state = mc.level.getBlockState(position);
			if (!canBreakPiston && !triedPistonTool && isTemporaryPiston(state)) {
				triedPistonTool = true;
				canBreakPiston = switchCleanupPistonTool(mc);
			}
			if (attackTemporaryCleanupBlock(mc, position, state, positionStorage.getCleanupDirection(position), canBreakPiston)) {
				ret++;
			}
		}
		return ret;
	}

	synchronized public static int scheduledTickHandler(Minecraft mc, @Nullable BlockPos pos) {
		return scheduledTickHandler(mc, pos, PRINTER_MAX_BLOCKS.getIntegerValue());
	}

	private static int processPositionCaches(Minecraft mc, int maxInteract) {
		int interacted = 0;
		for (PositionCache item : targetPosMap.values()) {
			if (interacted >= maxInteract) {
				return interacted;
			}
			if (item == null || !item.isAllPosInRange(mc)) {
				continue;
			}
			interacted += item.doSomething(mc, maxInteract - interacted);
		}
		return interacted;
	}

	synchronized public static int scheduledTickHandler(Minecraft mc, @Nullable BlockPos pos, int maxInteract) {
		if (maxInteract <= 0 || mc.player == null || mc.level == null || mc.gameMode == null) {
			return 0;
		}
		refreshReachFromConfig();
		positionStorage.refresh(mc.level);
		int interacted = 0;
		interacted += BedrockBreaker.processRemainder(mc, maxInteract);
		removeScheduledPos(mc);
		if (interacted >= maxInteract) {
			return interacted;
		}
		interacted += processPositionCaches(mc, maxInteract - interacted);
		if (interacted >= maxInteract) {
			return interacted;
		}
		if (!isItemPrePared(mc)) {
			MessageHolder.sendUniqueMessage(mc.player, "[BedrockBreaking]Items is not prepared, requires Redstone torch, Piston block + haste2 + eff 5 diamond+ pickaxe.");
			return interacted;
		}
		long now = System.currentTimeMillis();
		if (pos != null && isPositionInRange(mc, pos) && canProcess(mc, pos) && now - lastPlaced > 1000.0 * EASY_PLACE_MODE_DELAY.getDoubleValue()) {
			TorchPath torch = getPistonTorchPosDir(mc, pos);
			if (torch != null && torch.isAllPosInRange(mc)) {
				BlockPos TorchPos = torch.TorchPos;
				Direction TorchFacing = torch.Torchfacing;
				BlockPos PistonPos = torch.PistonPos;
				Direction PistonFacing = torch.Pistonfacing;
				Direction PistonExtendFacing = torch.PistonBreakableFacing;
				BlockPos SlimePos = torch.slimePos;
				//MessageHolder.sendDebugMessage("Will place Torch at %s, facing %s \n Piston at %s, Facing %s, and changes as %s \n Optional Slime at %s".formatted(TorchPos.toShortString(), TorchFacing, PistonPos.toShortString(), PistonFacing, PistonExtendFacing, SlimePos));
				MessageHolder.sendDebugMessage("Will place Torch at " + TorchPos.toShortString() + ", facing " + TorchFacing + "\n Piston at " + PistonPos.toShortString() + ", Facing " + PistonFacing + ", and changes as " + PistonExtendFacing + "\n Optional Slime at " + SlimePos);
				boolean slimePlaced = SlimePos == null;
				boolean torchPlaced = false;
				boolean pistonPlaced = false;
				boolean placedAny = false;
				if (SlimePos != null && interacted < maxInteract) {
					if (placeSlime(mc, SlimePos)) {
						interacted++;
						slimePlaced = true;
						placedAny = true;
					}
				}
				if (interacted < maxInteract && placeTorch(mc, TorchPos, TorchFacing)) {
					interacted++;
					torchPlaced = true;
					placedAny = true;
				}
				if (interacted < maxInteract && placePiston(mc, PistonPos, PistonFacing)) {
					interacted++;
					pistonPlaced = true;
					placedAny = true;
				}
				if (placedAny) {
					lastPlaced = Math.max(lastPlaced, now);
					targetPosMap.put(pos.asLong(), new PositionCache(PistonPos, PistonExtendFacing, TorchPos, TorchFacing, PistonFacing, pos, SlimePos, slimePlaced, torchPlaced, pistonPlaced));
				}
			}
		}
		return interacted;
	}

	public static void tick() {
		CurrentTick += 1L;
	}

	public static class PositionCache {
		public final BlockPos pistonPos;
		public final Direction facing;
		public final BlockPos torchPos;
		public final Direction torchFacing;
		public final Direction pistonPlaceFacing;
		public final BlockPos targetPos;
		public long SysTime;
		public final BlockPos slimePos;
		public PositionCache.State state;
		private boolean setupSlimePlaced;
		private boolean setupTorchPlaced;
		private boolean setupPistonPlaced;

		public enum State {
			SETUP,
			WAIT,
			EXTENDED,
			IDLE,
			FAIL,
			DONE,
			CLEAR
		}

		private PositionCache(BlockPos pistonPos, Direction facing, BlockPos torchPos, Direction torchFacing, Direction pistonPlaceFacing, BlockPos targetPos, BlockPos slimePos, boolean slimePlaced, boolean torchPlaced, boolean pistonPlaced) {
			this.pistonPos = pistonPos;
			this.facing = facing;
			this.torchPos = torchPos;
			this.torchFacing = torchFacing;
			this.pistonPlaceFacing = pistonPlaceFacing;
			this.targetPos = targetPos;
			this.SysTime = CurrentTick;
			this.slimePos = slimePos;
			this.setupSlimePlaced = slimePlaced;
			this.setupTorchPlaced = torchPlaced;
			this.setupPistonPlaced = pistonPlaced;
			this.state = this.isSetupComplete() ? PositionCache.State.WAIT : PositionCache.State.SETUP;
		}

		private void setFalse(Level world) {
			this.registerCleanupPosIfTemporary(world, this.torchPos, Direction.UP);
			if (this.slimePos != null) {
				this.registerCleanupPosIfTemporary(world, this.slimePos, Direction.UP);
			}
			this.registerCleanupPosIfTemporary(world, this.pistonPos, this.facing);
		}

		private void registerCleanupPosIfTemporary(Level world, BlockPos pos, Direction cleanupDirection) {
			BlockState state = world.getBlockState(pos);
			if (isTemporaryTorch(state) || isTemporarySlime(state) || isTemporaryPiston(state)) {
				positionStorage.registerPos(pos, false, cleanupDirection);
			}
		}

		public boolean isAllPosInRange(Minecraft mc) {
			double playerX = mc.player.getX();
			double playerY = mc.player.getY();
			double playerZ = mc.player.getZ();
			double reachSquared = maxReachSquared();
			return isBlockCenterWithinReach(playerX, playerY, playerZ, this.pistonPos, reachSquared)
				&& isBlockCenterWithinReach(playerX, playerY, playerZ, this.torchPos, reachSquared)
				&& isBlockCenterWithinReach(playerX, playerY, playerZ, this.targetPos, reachSquared)
				&& (this.slimePos == null || isBlockCenterWithinReach(playerX, playerY, playerZ, this.slimePos, reachSquared));
		}

		public boolean canSafeRemove(Level world) {
			if (this.hasTemporaryBlocks(world)) {
				return false;
			}
			if (this.state == State.DONE || this.state == State.CLEAR || this.state == State.FAIL) {
				return true;
			}
			return this.state == State.SETUP && CurrentTick - this.SysTime > 200L;
		}

		private boolean hasTemporaryBlocks(Level world) {
			return isTemporaryTorch(world.getBlockState(this.torchPos))
				|| isTemporaryPiston(world.getBlockState(this.pistonPos))
				|| this.slimePos != null && isTemporarySlime(world.getBlockState(this.slimePos));
		}

		private void refresh(ClientLevel world) {
			switch (this.state) {
				case SETUP: {
					if (CurrentTick - this.SysTime > 200L) {
						this.state = State.FAIL;
					}
					break;
				}
				case WAIT: {
					if (CurrentTick == this.SysTime + 1 || CurrentTick > this.SysTime + 4) {
						this.state = State.EXTENDED;
					}
					break;
				}
				case IDLE: {
					if (this.SysTime + PRINTER_BEDROCK_DELAY.getIntegerValue() < CurrentTick) {
						this.setFalse(world);
						this.state = world.getBlockState(this.targetPos).is(Blocks.BEDROCK) ? State.FAIL : State.DONE;
					}
					break;
				}
			}
		}

		public int doSomething(Minecraft mc, int maxInteractions) {
			refresh(mc.level);
			switch (this.state) {
				case SETUP : {
					return this.processSetup(mc, maxInteractions);
				}
				case EXTENDED : {
					return this.processBreaking(mc, maxInteractions);
				}
				case FAIL : {
					return this.resetFailure(mc, maxInteractions);
				}
			}
			return 0;
		}

		public boolean isIdle() {
			return this.state == State.CLEAR || this.state == State.DONE;
		}

		private boolean isSetupComplete() {
			return this.setupSlimePlaced && this.setupTorchPlaced && this.setupPistonPlaced;
		}

		private void completeSetupIfReady() {
			if (this.isSetupComplete()) {
				this.SysTime = CurrentTick;
				this.state = State.WAIT;
			}
		}

		private int processSetup(Minecraft mc, int maxInteractions) {
			int interactions = 0;
			if (maxInteractions <= 0) {
				return interactions;
			}
			if (!this.setupSlimePlaced) {
				if (this.slimePos == null || isTemporarySlime(mc.level.getBlockState(this.slimePos))) {
					this.setupSlimePlaced = true;
				} else if (placeSlime(mc, this.slimePos)) {
					interactions++;
					this.setupSlimePlaced = true;
				}
				if (interactions >= maxInteractions) {
					this.completeSetupIfReady();
					return interactions;
				}
			}
			if (!this.setupTorchPlaced) {
				BlockState torchState = mc.level.getBlockState(this.torchPos);
				if (isTemporaryTorch(torchState)) {
					this.setupTorchPlaced = true;
				} else if (placeTorch(mc, this.torchPos, this.torchFacing)) {
					interactions++;
					this.setupTorchPlaced = true;
				}
				if (interactions >= maxInteractions) {
					this.completeSetupIfReady();
					return interactions;
				}
			}
			if (!this.setupPistonPlaced) {
				if (isTemporaryPiston(mc.level.getBlockState(this.pistonPos))) {
					this.setupPistonPlaced = true;
				} else if (placePiston(mc, this.pistonPos, this.pistonPlaceFacing)) {
					interactions++;
					this.setupPistonPlaced = true;
				}
			}
			this.completeSetupIfReady();
			return interactions;
		}

		public boolean distanceSquaredLessThan(BlockPos ReferPos, double distanceSquared) {
			return blockPosDistanceSquaredLessThan(this.targetPos, ReferPos, distanceSquared)
				|| blockPosDistanceSquaredLessThan(this.pistonPos, ReferPos, distanceSquared)
				|| blockPosDistanceSquaredLessThan(this.torchPos, ReferPos, distanceSquared)
				|| this.slimePos != null && blockPosDistanceSquaredLessThan(this.slimePos, ReferPos, distanceSquared);
		}

		private static boolean blockPosDistanceSquaredLessThan(BlockPos pos, BlockPos referencePos, double distanceSquared) {
			double dx = referencePos.getX() - pos.getX();
			double dy = referencePos.getY() - pos.getY();
			double dz = referencePos.getZ() - pos.getZ();
			return dx * dx + dy * dy + dz * dz < distanceSquared;
		}

		public int processBreaking(Minecraft mc, int maxInteractions) {
			int interactions = 0;
			if (maxInteractions <= 0) {
				return interactions;
			}
			if (slimePos != null && isTemporarySlime(mc.level.getBlockState(slimePos))) {
				if (interactions < maxInteractions && attackTemporaryTorch(mc, torchPos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke torch at " + torchPos.toShortString());
				}
				if (interactions < maxInteractions && attackTemporarySlime(mc, slimePos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke slime at " + slimePos.toShortString());
				}
			} else {
				if (interactions < maxInteractions && attackTemporaryTorch(mc, torchPos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke torch at " + torchPos.toShortString());
				}
			}
			if (interactions >= maxInteractions) {
				return interactions;
			}
			if (isTemporaryPiston(mc.level.getBlockState(pistonPos))) {
				if (switchCleanupPistonTool(mc) && attackTemporaryPiston(mc, pistonPos, this.facing)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke piston at " + pistonPos.toShortString());
				}
			}

			if (interactions < maxInteractions && !isTemporaryPiston(mc.level.getBlockState(pistonPos)) && placePiston(mc, pistonPos, facing, true)) {
				interactions++;
				this.SysTime = CurrentTick;
				this.state = State.IDLE;
			}
			return interactions;
		}

		public int resetFailure(Minecraft mc, int maxInteractions) {
			int interactions = 0;
			if (maxInteractions <= 0) {
				return interactions;
			}
			if (slimePos != null && isTemporarySlime(mc.level.getBlockState(slimePos))) {
				if (interactions < maxInteractions && attackTemporaryTorch(mc, torchPos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke torch at + (failure) " + torchPos.toShortString());
				}
				if (interactions < maxInteractions && attackTemporarySlime(mc, slimePos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke slime at + (failure) " + slimePos.toShortString());
				}
			} else {
				if (interactions < maxInteractions && attackTemporaryTorch(mc, torchPos, Direction.UP)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke torch at + (failure) " + torchPos.toShortString());
				}
			}
			if (interactions >= maxInteractions) {
				return interactions;
			}
			if (isTemporaryPiston(mc.level.getBlockState(pistonPos))) {
				if (switchCleanupPistonTool(mc) && attackTemporaryPiston(mc, pistonPos, this.facing)) {
					interactions++;
					MessageHolder.sendDebugMessage("Broke piston at + (failure) " + pistonPos.toShortString());
				}
			}
			if (!isTemporaryPiston(mc.level.getBlockState(pistonPos))) {
				this.state = State.CLEAR;
				this.setFalse(mc.level);
			}
			return interactions;
		}
	}

	public static class TorchPath {
		private final BlockPos TorchPos;
		private final Direction Torchfacing;
		private final BlockPos PistonPos;
		private final Direction Pistonfacing;
		private final Direction PistonBreakableFacing;
		private BlockPos slimePos;

		public TorchPath(BlockPos TorchPos, Direction Torchfacing, BlockPos PistonPos, Direction Pistonfacing, Direction PistonBreakableFacing) {
			this.TorchPos = TorchPos;
			this.Torchfacing = Torchfacing;
			this.Pistonfacing = Pistonfacing;
			this.PistonPos = PistonPos;
			this.PistonBreakableFacing = PistonBreakableFacing;
		}

		public boolean isAllPosInRange(Minecraft mc) {
			double playerX = mc.player.getX();
			double playerY = mc.player.getY();
			double playerZ = mc.player.getZ();
			double reachSquared = maxReachSquared();
			return isBlockCenterWithinReach(playerX, playerY, playerZ, this.TorchPos, reachSquared)
				&& isBlockCenterWithinReach(playerX, playerY, playerZ, this.PistonPos, reachSquared)
				&& (this.slimePos == null || isBlockCenterWithinReach(playerX, playerY, playerZ, this.slimePos, reachSquared));
		}

	}

	public static class TorchData {
		private final BlockPos TorchPos;
		private final Direction Torchfacing;
		private BlockPos SlimePos = null;

		public TorchData(BlockPos TorchPos, Direction Torchfacing) {
			this.TorchPos = TorchPos;
			this.Torchfacing = Torchfacing;
		}

		public void registerSlimePos(BlockPos slimePos) {
			this.SlimePos = slimePos;
		}
	}
}
