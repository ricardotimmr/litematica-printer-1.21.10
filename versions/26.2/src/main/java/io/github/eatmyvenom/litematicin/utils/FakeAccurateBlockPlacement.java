package io.github.eatmyvenom.litematicin.utils;

//see https://github.com/senseiwells/EssentialClient/blob/1.19.x/src/main/java/me/senseiwells/essentialclient/feature/BetterAccurateBlockPlacement.java

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.*;
import static io.github.eatmyvenom.litematicin.utils.BedrockBreaker.interactBlock;
import static io.github.eatmyvenom.litematicin.utils.InventoryUtils.getStackForState;
import static io.github.eatmyvenom.litematicin.utils.InventoryUtils.isCreative;

public class FakeAccurateBlockPlacement {

	// We implement FIFO Queue structure with responsible ticks.
	// By config, we define 'wait tick' between block placements
	public static Direction fakeDirection = null;
	public static boolean shouldReturnValue = false;
	public static int requestedTicks = -3;
	public static float fakeYaw = 0;
	public static float fakePitch = 0;
	private static BlockState stateGrindStone = null;
	private static float previousFakeYaw = 0;
	private static float previousFakePitch = 0;
	private static int tickElapsed = 0;
	private static int blockPlacedInTick = 0;
	private static BlockState handlingState = null;
	public static Item currentHandling = Items.AIR;
	private static final Queue<PosWithBlock> waitingQueue = new ArrayBlockingQueue<PosWithBlock>(1) {
	};
	private static final HashSet<Block> warningSet = new HashSet<>();

	// Cancel when handling
	public static boolean isHandling() {
		return requestedTicks > 0;
	}

	private static void resetHandlingState(boolean clearQueue) {
		requestedTicks = -3;
		fakeDirection = null;
		currentHandling = Items.AIR;
		stateGrindStone = null;
		handlingState = null;
		shouldReturnValue = false;
		if (clearQueue) {
			waitingQueue.clear();
		}
	}

	// flag for canceling
	public static boolean shouldModifyValues() {
		return requestedTicks > -3 && fakeDirection != null || Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && (PRINTER_FAKE_ROTATION_AGGRESSIVE.getBooleanValue() || PRINTER_SUPPRESS_PACKETS.getBooleanValue());
	}

	public static boolean canHandleOther() {
		return currentHandling == null || currentHandling == Items.AIR;
	}

	/*
		returns if item can be handled
	 */
	public static boolean canHandleOther(Item item) {
		if (canHandleOther()) {
			return true;
		}
		return currentHandling == item;
	}

	private static String canHandleOtherReason(Item item) {
		if (canHandleOther()) {
			return "canHandleOther";
		}
		if (currentHandling != item) {
			return "currently handling "+ currentHandling.getName(currentHandling.getDefaultInstance()).getString() + " but requested " + item.getName(item.getDefaultInstance()).getString();
		}
		return "unknown";
	}

	public static float getYaw(Player player) {
		//#if MC>=11700
		return player.getYRot();
		//#else
		//$$ return player.yaw;
		//#endif
	}

	public static float getPitch(Player player) {
		//#if MC>=11700
		return player.getXRot();
		//#else
		//$$ return player.pitch;
		//#endif
	}


	//I can implement anti-anti cheat, because anti cheats are just checking rotations being too accurate / fast, just interpolating is enough...
	//But I won't. Just follow server rules :shrug:
	public static void tick(ClientPacketListener clientPlayNetworkHandler, LocalPlayer playerEntity) {
		tickElapsed = 0;
		blockPlacedInTick = 0;
		if (playerEntity == null || clientPlayNetworkHandler == null) {
			resetHandlingState(true);
			return;
		}
		if (requestedTicks >= -1) {
			MessageHolder.sendOrderMessage("Requested ticks: " + requestedTicks);
			if (PRINTER_FAKE_ROTATION_AGGRESSIVE.getBooleanValue() || fakeYaw != previousFakeYaw || fakePitch != previousFakePitch) {
				MessageHolder.sendOrderMessage("Sending look packet" + fakeYaw + " " + fakePitch + " " + fakeDirection);
				sendLookPacket(clientPlayNetworkHandler, playerEntity);
				previousFakePitch = fakePitch;
				previousFakeYaw = fakeYaw;
			}
			//we send this at last tick
		}
		if (requestedTicks <= -1) {
			currentHandling = Items.AIR;
			stateGrindStone = null;
			handlingState = null;
		}
		if (requestedTicks <= -3) {
			requestedTicks = -3;
			fakeDirection = null;
			previousFakePitch = getPitch(playerEntity);
			previousFakeYaw = getYaw(playerEntity);
		}
		if (requestedTicks == 0 && PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue()){
			placeFromQueue();
		}
		requestedTicks = requestedTicks - 1;
	}
	public static boolean placeFromQueue() {
		if (requestedTicks > 0) {
			MessageHolder.sendOrderMessage("Requested tick was " + requestedTicks);
			return false;
		}
		if (waitingQueue.isEmpty()) {
			return false;
		}
		PosWithBlock obj = waitingQueue.poll();
		if (obj != null) {
			MessageHolder.sendOrderMessage("found block to place");
			if (canPlace(obj.blockState, obj.pos)) {
				if (placeBlock(obj.pos, obj.blockState)) {
					return true;
				}
				MessageHolder.sendOrderMessage("found block to place but placement failed");
			}
			else {
				MessageHolder.sendOrderMessage("found block to place but can't place");
			}
		}
		resetHandlingState(true);
		return false;
	}
	public static boolean emptyWaitingQueue() {
		return placeFromQueue();
	}

	public static boolean isWaitingFor(BlockPos pos, BlockState blockState) {
		PosWithBlock obj = waitingQueue.peek();
		return obj != null && obj.pos.equals(pos) && Printer.sameBlockState(obj.blockState, blockState);
	}

	public static void sendLookPacket(ClientPacketListener networkHandler, LocalPlayer playerEntity) {
		networkHandler.send(
			//#if MC>=11700
			new ServerboundMovePlayerPacket.Rot(
			//#else
			//$$ new PlayerMoveC2SPacket.LookOnly(
			//#endif
				fakeYaw,
				fakePitch,
			//#if MC>=12102
			playerEntity.onGround(),
			playerEntity.horizontalCollision
			//#else
			//$$ 	playerEntity.isOnGround()
			//#endif
			)
		);
		//System.out.print(fakeYaw);
		//System.out.print(fakePitch);
	}

	/*
	Pure request function by yaw pitch direction
	 */
	public static boolean request(float yaw, float pitch, Direction direction, int duration, boolean force) {
		if (isHandling()) {
			if (!force) {
				MessageHolder.sendOrderMessage("Already handling " + handlingState + " for " + requestedTicks + " ticks");
				return false;
			}
		}
		final Minecraft minecraftClient = Minecraft.getInstance();
		final ClientPacketListener networkHandler = minecraftClient.getConnection();
		final LocalPlayer playerEntity = minecraftClient.player;
		if (networkHandler == null || playerEntity == null) {
			return false;
		}
		fakeDirection = direction;
		fakeYaw = yaw;
		fakePitch = pitch;
		requestedTicks = duration;
		MessageHolder.sendOrderMessage("Requested " + duration + " ticks of handling " + handlingState + " with yaw " + yaw + " pitch " + pitch + " direction " + direction);
		// we might need it instantly
		sendLookPacket(networkHandler, playerEntity);
		return true;
	}

	private static boolean canPlaceWallMounted(BlockState blockState) {
		if (blockState.getBlock() instanceof TorchBlock) {
			if (blockState.getBlock() instanceof WallTorchBlock || blockState.getBlock() instanceof RedstoneWallTorchBlock) {
				return fakeDirection == blockState.getValue(WallTorchBlock.FACING).getOpposite();
			}
			return fakeDirection == Direction.DOWN;
		}
		if (blockState.getBlock() instanceof FaceAttachedHorizontalDirectionalBlock) {
			//so we have 2 properties, looking at down / up as first direction, horizontals as second direction.
			AttachFace location = blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
			if (location == AttachFace.WALL) {
				return true;
			}
			Direction facingSecond = blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
			return fakeDirection == facingSecond;
		} else {
			return true;
		}
	}

	private static float yawForDirection(Direction direction) {
		if (direction == Direction.EAST) {
			return -87;
		} else if (direction == Direction.WEST) {
			return 87;
		} else if (direction == Direction.NORTH) {
			return 177;
		} else if (direction == Direction.SOUTH) {
			return 3;
		}
		return 0;
	}

	private static float pitchForLookDirection(Direction direction) {
		if (direction == Direction.UP) {
			return -90;
		} else if (direction == Direction.DOWN) {
			return 90;
		}
		return 12;
	}

	private static Vec3 hitVecOnSide(BlockPos pos, Direction side) {
		return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));
	}

	private static boolean requestFrontAndTopOrientation(BlockState blockState, BlockPos blockPos) {
		Direction lookRefdir = Printer.getFrontAndTopLookDirection(blockState);
		if (lookRefdir == null) {
			return placeBlock(blockPos, blockState);
		}
		Direction yawRefdir = lookRefdir.getAxis() == Direction.Axis.Y ? Printer.getFrontAndTopHorizontalDirection(blockState) : lookRefdir;
		float fy = yawForDirection(yawRefdir);
		float fp = pitchForLookDirection(lookRefdir);
		return requestBlockWithLook(blockState, blockPos, fy, fp, lookRefdir);
	}

	private static boolean requestWallHangingSign(BlockState blockState, BlockPos blockPos) {
		Direction lookRefdir = blockState.getValue(WallHangingSignBlock.FACING).getOpposite();
		return requestBlockWithLook(blockState, blockPos, yawForDirection(lookRefdir), 12.0F, lookRefdir);
	}

	private static boolean requestCeilingHangingSign(BlockState blockState, BlockPos blockPos) {
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null || minecraftClient.player == null) {
			return false;
		}
		Float yaw = Printer.getCeilingHangingSignPlacementYaw(minecraftClient.level, blockPos, blockState, minecraftClient.player.isSecondaryUseActive());
		if (yaw == null) {
			MessageHolder.sendOrderMessage("Cannot derive vanilla ceiling hanging sign yaw for " + blockState + " at " + blockPos.toShortString());
			return false;
		}
		Direction lookRefdir = Direction.fromYRot(yaw);
		return requestBlockWithLook(blockState, blockPos, yaw, 12.0F, lookRefdir);
	}

	private static boolean requestYawRotation16Block(BlockState blockState, BlockPos blockPos) {
		float yaw = Printer.getYawForRotation16Placement(blockState);
		Direction lookRefdir = Direction.fromYRot(yaw);
		return requestBlockWithLook(blockState, blockPos, yaw, 12.0F, lookRefdir);
	}

	private static boolean requestDripleaf(BlockState blockState, BlockPos blockPos) {
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null) {
			return false;
		}
		Direction lookRefdir = Printer.getDripleafPlacementLookDirection(minecraftClient.level, blockPos, blockState);
		if (lookRefdir == null) {
			MessageHolder.sendOrderMessage("Cannot derive vanilla dripleaf facing for " + blockState + " at " + blockPos.toShortString());
			return false;
		}
		return requestBlockWithLook(blockState, blockPos, yawForDirection(lookRefdir), 12.0F, lookRefdir);
	}

	private static boolean requestSpeleothem(BlockState blockState, BlockPos blockPos) {
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null || minecraftClient.player == null) {
			return false;
		}
		Direction lookRefdir = Printer.getSpeleothemPlacementLookDirection(minecraftClient.level, blockPos, blockState, minecraftClient.player.isSecondaryUseActive());
		if (lookRefdir == null) {
			MessageHolder.sendOrderMessage("Cannot derive vanilla speleothem direction for " + blockState + " at " + blockPos.toShortString());
			return false;
		}
		return requestBlockWithLook(blockState, blockPos, 0.0F, pitchForLookDirection(lookRefdir), lookRefdir);
	}

	private static boolean requestMultifaceOrVine(BlockState blockState, BlockPos blockPos) {
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null) {
			return false;
		}
		Direction lookRefdir = Printer.getMultifaceOrVinePlacementLookDirection(minecraftClient.level, blockPos, blockState);
		if (lookRefdir == null) {
			MessageHolder.sendOrderMessage("Cannot derive vanilla multiface/vine direction for " + blockState + " at " + blockPos.toShortString());
			return false;
		}
		return requestBlockWithLook(blockState, blockPos, yawForDirection(lookRefdir), pitchForLookDirection(lookRefdir), lookRefdir);
	}

	private static boolean requestBlockWithLook(BlockState blockState, BlockPos blockPos, float fy, float fp, Direction lookRefdir) {
		if (LitematicaMixinMod.PRINTER_FAKE_ROTATION_DELAY.getIntegerValue() == 0) {
			if (lookRefdir != fakeDirection || fy != fakeYaw || fp != fakePitch) {
				if (tickElapsed >= LitematicaMixinMod.PRINTER_FAKE_ROTATION_LIMIT_PER_TICKS.getIntegerValue()) {
					MessageHolder.sendDebugMessage("Failure because limited fake rotation per tick " + blockPos.toShortString());
					return false;
				}
				tickElapsed += 1;
				if (!request(fy, fp, lookRefdir, LitematicaMixinMod.PRINTER_FAKE_ROTATION_DELAY.getIntegerValue(), true)) {
					return false;
				}
			}
			return placeBlock(blockPos, blockState);
		}
		if (isHandling() && (lookRefdir != fakeDirection || fp != fakePitch || fy != fakeYaw)) {
			MessageHolder.sendOrderMessage("Cannot handle " + blockState + " at " + blockPos.toShortString()
				+ " because requested orientation look/yaw/pitch differs from current handling state");
			return false;
		}
		if (requestedTicks <= 0 && fakeDirection == lookRefdir && fp == fakePitch && fy == fakeYaw) {
			return placeBlock(blockPos, blockState);
		}
		if (waitingQueue.isEmpty()) {
			return requestDelayedPlacement(blockState, blockPos, fy, fp, lookRefdir);
		}
		if (!placeQueuedBlockBeforeCurrent()) {
			return false;
		}
		return requestDelayedPlacement(blockState, blockPos, fy, fp, lookRefdir);
	}

	private static boolean requestGrindStone(BlockState state, BlockPos blockPos) {
		Direction facing = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
		AttachFace location = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
		float fy = 0;
		float fp = 0;
		Direction lookRefdir;
		if (location == AttachFace.CEILING) {
			//primary should be UP
			//secondary should be same as facing
			fp = -90;
			lookRefdir = facing;
		} else if (location == AttachFace.FLOOR) {
			fp = 90;
			lookRefdir = facing;
		} else {
			fp = 0;
			lookRefdir = facing.getOpposite();
		}
		if (lookRefdir == Direction.EAST) {
			fy = -87;
		} else if (lookRefdir == Direction.WEST) {
			fy = 87;
		} else if (lookRefdir == Direction.NORTH) {
			fy = 177;
		} else if (lookRefdir == Direction.SOUTH) {
			fy = 3;
		}
		if (isHandling()) {
			return false;
		}
		if (!waitingQueue.isEmpty() && !placeQueuedBlockBeforeCurrent()) {
			return false;
		}
		stateGrindStone = state;
		boolean accepted = requestBlockWithLook(state, blockPos, fy, fp, lookRefdir);
		if (!accepted && waitingQueue.isEmpty()) {
			resetHandlingState(true);
		}
		return accepted;
	}

	private static boolean enqueueRequestedBlock(BlockState blockState, BlockPos blockPos) {
		if (!pickFirst(blockState, blockPos)) {
			resetHandlingState(true);
			return false;
		}
		boolean offered = waitingQueue.offer(new PosWithBlock(blockPos, blockState));
		if (offered) {
			MessageHolder.sendOrderMessage("Offered " + blockState + " at " + blockPos.toShortString());
		} else {
			resetHandlingState(true);
			MessageHolder.sendOrderMessage("Cannot offer " + blockState + " at " + blockPos.toShortString());
		}
		return false;
	}

	private static boolean requestDelayedPlacement(BlockState blockState, BlockPos blockPos, float fy, float fp, Direction lookRefdir) {
		if (requestedTicks <= 0 && fakeDirection == lookRefdir && fp == fakePitch && fy == fakeYaw) {
			return placeBlock(blockPos, blockState);
		}
		if (!request(fy, fp, lookRefdir, LitematicaMixinMod.PRINTER_FAKE_ROTATION_DELAY.getIntegerValue(), false)) {
			return false;
		}
		return enqueueRequestedBlock(blockState, blockPos);
	}

	private static boolean placeQueuedBlockBeforeCurrent() {
		PosWithBlock queued = waitingQueue.peek();
		MessageHolder.sendOrderMessage("Queue is holding " + queued.blockState + " at " + queued.pos.toShortString());
		if (!placeFromQueue()) {
			return false;
		}
		queued = waitingQueue.peek();
		if (queued != null) {
			MessageHolder.sendOrderMessage("Tried emptying queue but still holding " + queued.blockState + " at " + queued.pos.toShortString());
			return false;
		}
		return true;
	}

	public static Direction getPlayerFacing() {
		if (fakeYaw == -87) {
			return Direction.EAST;
		} else if (fakeYaw == 87) {
			return Direction.WEST;
		} else if (fakeYaw == 177) {
			return Direction.NORTH;
		} else if (fakeYaw == 3) {
			return Direction.SOUTH;
		}
		return null;
	}

	public static Direction[] getFacingOrder() {
		float theta = fakePitch * 0.017453292F;
		float omega = -fakeYaw * 0.017453292F;
		float unitHorizontal = Mth.cos(theta);
		float yVector = -Mth.sin(theta);
		float xVector = unitHorizontal * Mth.sin(omega);
		float zVector = unitHorizontal * Mth.cos(omega);
		float yScalar = Math.abs(yVector);
		float xScalar = Math.abs(xVector);
		float zScalar = Math.abs(zVector);
		Direction directionX = xVector > 0.0F ? Direction.EAST : Direction.WEST;
		Direction directionY = yVector > 0.0F ? Direction.UP : Direction.DOWN;
		Direction directionZ = zVector > 0.0F ? Direction.SOUTH : Direction.NORTH;
		if (xScalar > zScalar) {
			if (yScalar > xScalar) {
				return listClosest(directionY, directionX, directionZ);
			} else {
				return zScalar > yScalar ? listClosest(directionX, directionZ, directionY) : listClosest(directionX, directionY, directionZ);
			}
		} else if (yScalar > zScalar) {
			return listClosest(directionY, directionZ, directionX);
		} else {
			return xScalar > yScalar ? listClosest(directionZ, directionX, directionY) : listClosest(directionZ, directionY, directionX);
		}
	}

	private static Direction[] listClosest(Direction first, Direction second, Direction third) {
		return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
	}

	/***
	 *
	 * @param blockState : Block object(terracotta, etc...)
	 * @param blockPos : Block Position
	 * @return boolean : if its registered and just can place it.
	 * example : boolean canContinue = FakeAccurateBlockPlacement.request(SchematicState, BlockPos)
	 */
	synchronized public static boolean request(BlockState blockState, BlockPos blockPos) {
		// instant
		if (blockState.is(Blocks.GRINDSTONE) && stateGrindStone == null) {
			return requestGrindStone(blockState, blockPos);
		}
		boolean canPlace = canPlace(blockState, blockPos);
		Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
		Item requiredItem = MaterialCache.getInstance().getRequiredBuildItemForState(blockState, schematicWorld, blockPos).getItem();
		if (!canPlace || blockState.isAir() || requiredItem == Items.AIR) {
			MessageHolder.sendOrderMessage("Cannot place "+ blockState.toString() + " at " + blockPos.toShortString());
			// print reason, canPlace / isAir / isRequiredBuildItemForState
			MessageHolder.sendOrderMessage("Reason : " + (canPlace ? "" : "cannotPlace") + " " + (blockState.isAir() ? "isAir" : "") + " " + (requiredItem == Items.AIR ? "materialWasAir" : ""));
			return false;
		}
		if (blockState.getBlock() instanceof WallHangingSignBlock) {
			return requestWallHangingSign(blockState, blockPos);
		}
		if (blockState.getBlock() instanceof CeilingHangingSignBlock) {
			return requestCeilingHangingSign(blockState, blockPos);
		}
		if (Printer.hasDripleafPlacementFacing(blockState)) {
			return requestDripleaf(blockState, blockPos);
		}
		if (Printer.hasSpeleothemPlacement(blockState)) {
			return requestSpeleothem(blockState, blockPos);
		}
		if (Printer.hasMultifaceOrVinePlacement(blockState)) {
			return requestMultifaceOrVine(blockState, blockPos);
		}
		if (Printer.hasYawRotation16Placement(blockState)) {
			return requestYawRotation16Block(blockState, blockPos);
		}
		if (Printer.hasFrontAndTopOrientation(blockState)) {
			return requestFrontAndTopOrientation(blockState, blockPos);
		}
		if (blockState.is(Blocks.HOPPER) || blockState.is(BlockTags.SHULKER_BOXES) ||  blockState.is(Blocks.END_ROD)) {
			return placeBlock(blockPos, blockState);
		}
		//#if MC>=11700
		else if (blockState.is(BlockTags.LIGHTNING_RODS)) {
			return placeBlock(blockPos, blockState);
		}
		//#endif
		if (!blockState.hasProperty(BlockStateProperties.FACING) && !blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && !(blockState.getBlock() instanceof BaseRailBlock) && !(blockState.getBlock() instanceof TorchBlock)) {
			return placeBlock(blockPos, blockState); //without facing properties
		}
		FacingData facingData = FacingData.getFacingData(blockState);
		if (facingData == null && !(blockState.getBlock() instanceof BaseRailBlock) && !(blockState.getBlock() instanceof TorchBlock)) {
			if (!warningSet.contains(blockState.getBlock())) {
				warningSet.add(blockState.getBlock());
				System.out.printf("WARN : Block %s is not found\n", blockState.getBlock().toString());
			}
			return placeBlock(blockPos, blockState);
		}
		Direction facing = Printer.getSimplifiedFirstPropertyFacingValue(blockState); //facing of block itself
		if (facing == null && blockState.getBlock() instanceof BaseRailBlock) {
			facing = Printer.convertRailShapetoFace(blockState);
		} else if (blockState.getBlock() instanceof TorchBlock) {
			if (blockState.getBlock() instanceof WallTorchBlock || blockState.getBlock() instanceof RedstoneWallTorchBlock) {
				facing = blockState.getValue(WallTorchBlock.FACING).getOpposite();
			} else {
				facing = Direction.DOWN;
			}
		}
		if (facing == null) {
			//System.out.println(blockState);
			return placeBlock(blockPos, blockState);
		}
		//assume player is looking at north
		boolean reversed = facingData != null && facingData.isReversed;
		int order = facingData == null ? 0 : facingData.type;
		Direction direction1 = facing;
		float fy = 0, fp = 12;
		if (order == 0 || order == 1) {
			direction1 = reversed ? facing.getOpposite() : facing;
		} else if (order == 2) {
			facing = blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
			direction1 = blockState.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL ? facing.getOpposite() : facing;
			if (blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING) {
				fp = -90;
			} else if (blockState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.FLOOR) {
				fp = 90;
			} else {
				fp = 12;
			}
		} else if (order == 3) {
			direction1 = facing.getCounterClockWise();
		} else if (order == 5 && blockState.getValue(BellBlock.ATTACHMENT) != BellAttachType.FLOOR && blockState.getValue(BellBlock.ATTACHMENT) != BellAttachType.CEILING) {
			direction1 = null;
		}
		if (order != 2 && (direction1 == null || (requestedTicks <= 0 && fakeDirection == direction1 && fy == fakeYaw && fp == fakePitch)) && canPlaceWallMounted(blockState)) {
			return placeBlock(blockPos, blockState);
		}
		Direction lookRefdir = direction1;
		if (lookRefdir == Direction.UP) {
			fp = -90;
		} else if (lookRefdir == Direction.DOWN) {
			fp = 90;
		} else if (lookRefdir == Direction.EAST) {
			fy = -87;
		} else if (lookRefdir == Direction.WEST) {
			fy = 87;
		} else if (lookRefdir == Direction.NORTH) {
			fy = 177;
		} else if (lookRefdir == Direction.SOUTH) {
			fy = 3;
		} else {
			fy = 0;
			fp = 12;
		}
		if (LitematicaMixinMod.PRINTER_FAKE_ROTATION_DELAY.getIntegerValue() == 0) {
			//instant place
			if (lookRefdir != fakeDirection) {
				if (tickElapsed >= LitematicaMixinMod.PRINTER_FAKE_ROTATION_LIMIT_PER_TICKS.getIntegerValue()) {
					MessageHolder.sendDebugMessage("Failure because limited fake rotation per tick " + blockPos.toShortString());
					return false;
				}
				tickElapsed += 1;
				if (!request(fy, fp, lookRefdir, LitematicaMixinMod.PRINTER_FAKE_ROTATION_DELAY.getIntegerValue(), true)) {
					return false;
				}
				return placeBlock(blockPos, blockState);
			} else {
				return placeBlock(blockPos, blockState);
			}
		} else {
			//delay
			if (isHandling() && (fakeDirection != null && lookRefdir != fakeDirection || fp != 12 && fp != fakePitch || fy != 0 && fy != fakeYaw || !canPlaceWallMounted(blockState))) {
				String reason = "Failure because of ";
				if (isHandling()) {
					reason += "isHandling ";
				}
				if (fakeDirection != null && lookRefdir != fakeDirection) {
					reason += "lookRefdir " + lookRefdir + " is different from " + fakeDirection + " ";
				}
				if (fp != 12 && fp != fakePitch) {
					reason += "fp " + fp + " is different from " + fakePitch + " ";
				}
				if (fy != 0 && fy != fakeYaw) {
					reason += "fy " + fy + " is different from " + fakeYaw + " ";
				}
				if (!canPlaceWallMounted(blockState)) {
					reason += "cannot place wall mounted ";
				}
				MessageHolder.sendOrderMessage("Cannot handle "+ blockState + " at " + blockPos.toShortString() + reason);
				return false;
			}
			if (requestedTicks <= 0 && fakeDirection == lookRefdir && fp == fakePitch && fy == fakeYaw) {
				return placeBlock(blockPos, blockState);
			}
			if (waitingQueue.isEmpty()) {
				return requestDelayedPlacement(blockState, blockPos, fy, fp, lookRefdir);
			}
			else {
				if (!placeQueuedBlockBeforeCurrent()) {
					return false;
				}
				return requestDelayedPlacement(blockState, blockPos, fy, fp, lookRefdir);
			}
			// waiting other block?

		}
	}

	/***
	 *
	 * @param state : blockState with Facing, calculates if direction is correct and item is correct for given state
	 * @return : can place or not
	 */
	public static boolean canPlace(BlockState state, BlockPos pos) {
		if (!PRINTER_FAKE_ROTATION.getBooleanValue()) {
			return true;
		}
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null || !state.canSurvive(minecraftClient.level, pos) && !Printer.canPlaceBigDripleafStemAsLeaf(minecraftClient.level, pos, state)) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because state cannot survive");
			return false;
		}
		if (Printer.hasDripleafPlacementFacing(state) && Printer.getDripleafPlacementLookDirection(minecraftClient.level, pos, state) == null) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla dripleaf placement would use a different facing");
			return false;
		}
		if (Printer.hasSpeleothemPlacement(state)
			&& Printer.getSpeleothemPlacementLookDirection(minecraftClient.level, pos, state, minecraftClient.player != null && minecraftClient.player.isSecondaryUseActive()) == null) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla speleothem placement would produce a different state");
			return false;
		}
		if (!Printer.canPlaceSegmentedHorizontalBlock(minecraftClient.level, pos, state, null, minecraftClient.player != null && minecraftClient.player.isSecondaryUseActive())) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla segmented placement would keep a different existing state");
			return false;
		}
		if (!Printer.canPlaceMossyCarpet(minecraftClient.level, pos, state)) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla moss carpet placement would produce a different derived state");
			return false;
		}
		if (!Printer.canPlaceLantern(minecraftClient.level, pos, state)) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla lantern placement would use a different hanging state");
			return false;
		}
		if (Printer.hasMultifaceOrVinePlacement(state)
			&& Printer.getMultifaceOrVinePlacementLookDirection(minecraftClient.level, pos, state) == null) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla multiface/vine placement would add a wrong or unsupported face");
			return false;
		}
		if (wouldEndRodReverseOnPlacement(state, pos)) {
			MessageHolder.sendOrderMessage("Cannot place " + state + " at " + pos.toShortString() + " because vanilla end rod placement would reverse it");
			return false;
		}
		ItemStack stack = getStackForState(minecraftClient, state, SchematicWorldHandler.getSchematicWorld(), pos);
		Item item = stack.getItem();
		if (canHandleOther(item)) {
			if (state.is(Blocks.GRINDSTONE)) {
				if (stateGrindStone != null) {
					return stateGrindStone.getValue(GrindstoneBlock.FACE) == state.getValue(GrindstoneBlock.FACE) && stateGrindStone.getValue(GrindstoneBlock.FACING) == state.getValue(GrindstoneBlock.FACING);
				}
				MessageHolder.sendOrderMessage("No stateGrindStone was found");
				return false;
			} else if (handlingState != null && (Printer.hasFrontAndTopOrientation(handlingState) || Printer.hasFrontAndTopOrientation(state))) {
				return Printer.hasFrontAndTopOrientation(handlingState)
					&& Printer.hasFrontAndTopOrientation(state)
					&& handlingState.getValue(BlockStateProperties.ORIENTATION) == state.getValue(BlockStateProperties.ORIENTATION);
			} else if (handlingState != null && (handlingState.getBlock() instanceof DirectionalBlock || handlingState.getBlock() instanceof HorizontalDirectionalBlock && !(handlingState.getBlock() instanceof FaceAttachedHorizontalDirectionalBlock))) {
				Direction handling = Printer.getSimplifiedFirstPropertyFacingValue(handlingState);
				Direction other = Printer.getSimplifiedFirstPropertyFacingValue(state);
				return handling == other;
			}
			return true;
		}
		MessageHolder.sendOrderMessage("Cannot handle " + state.toString() + " at " + pos.toShortString() + canHandleOtherReason(item));
		return false;
	}

	private static boolean wouldEndRodReverseOnPlacement(BlockState state, BlockPos pos) {
		if (!(state.getBlock() instanceof EndRodBlock)) {
			return false;
		}
		Minecraft minecraftClient = Minecraft.getInstance();
		if (minecraftClient.level == null) {
			return false;
		}
		BlockPos supportPos = pos.relative(state.getValue(EndRodBlock.FACING).getOpposite());
		return Printer.wouldEndRodReverseOnPlacement(state, minecraftClient.level.getBlockState(supportPos));
	}

	private static ItemStack getSchematicStackForState(Minecraft minecraftClient, BlockState state, BlockPos pos) {
		return getStackForState(minecraftClient, state, SchematicWorldHandler.getSchematicWorld(), pos);
	}

	synchronized private static boolean placeBlock(BlockPos pos, BlockState blockState) {
		MessageHolder.sendDebugMessage("Handling placeBlock for " + pos.toShortString() + " and state " + blockState.toString());
		if (blockPlacedInTick >= PRINTER_MAX_BLOCKS.getIntegerValue()) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed due to limiting max block" + pos.toShortString());
			return false;
		}
		final Minecraft minecraftClient = Minecraft.getInstance();
		final LocalPlayer player = minecraftClient.player;
		if (minecraftClient.level == null || player == null) {
			return false;
		}
		if (!blockState.canSurvive(minecraftClient.level, pos) && !Printer.canPlaceBigDripleafStemAsLeaf(minecraftClient.level, pos, blockState)) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because state cannot survive at " + pos.toShortString());
			return false;
		}
		if (!Printer.canPlaceMossyCarpet(minecraftClient.level, pos, blockState)) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because moss carpet derived state would differ at " + pos.toShortString());
			return false;
		}
		if (!Printer.canPlaceLantern(minecraftClient.level, pos, blockState)) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because lantern hanging state is not placeable at " + pos.toShortString());
			return false;
		}
		if (Printer.hasYawRotation16Placement(blockState) && !Printer.canPlaceYawRotation16(blockState, fakeYaw)) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because yaw rotation would derive a different state at " + pos.toShortString());
			return false;
		}
		if (blockState.getBlock() instanceof CeilingHangingSignBlock
			&& !Printer.canPlaceCeilingHangingSign(minecraftClient.level, pos, blockState, fakeYaw, player.isSecondaryUseActive())) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because ceiling hanging sign rotation would differ at " + pos.toShortString());
			return false;
		}
		if (Printer.hasFrontAndTopOrientation(blockState)
			&& !Printer.canPlaceFrontAndTopOrientation(blockState, fakeDirection, Direction.fromYRot(fakeYaw))) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because front/top orientation would differ at " + pos.toShortString());
			return false;
		}
		if (Printer.hasDripleafPlacementFacing(blockState)
			&& Printer.getDripleafPlacementLookDirection(minecraftClient.level, pos, blockState) == null) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because dripleaf facing is no longer placeable at " + pos.toShortString());
			return false;
		}
		if (Printer.hasSpeleothemPlacement(blockState)
			&& Printer.getSpeleothemPlacementLookDirection(minecraftClient.level, pos, blockState, player.isSecondaryUseActive()) == null) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because speleothem direction is no longer placeable at " + pos.toShortString());
			return false;
		}
		//#if MC>=12000
		if (!minecraftClient.level.getBlockState(pos).canBeReplaced()
			&& !Printer.canPlaceSegmentedHorizontalBlock(minecraftClient.level, pos, blockState, null, player.isSecondaryUseActive())) {
		//#else
		//$$ if (!minecraftClient.world.getBlockState(pos).getMaterial().isReplaceable()) {
		//#endif
			MessageHolder.sendDebugMessage("Client block position was not replaceable at " + pos.toShortString());
			return false;
		}
		Direction sideOrig = Direction.NORTH;
		Direction side = Printer.applyPlacementFacing(blockState, sideOrig, minecraftClient.level.getBlockState(pos));
		BlockPos clickPos = pos;
		Vec3 appliedHitVec = Printer.applyHitVec(pos, blockState, side);
		//Trapdoor actually occasionally refers to player and UP DOWN wtf
		if (blockState.getBlock() instanceof TrapDoorBlock) {
			side = blockState.getValue(TrapDoorBlock.HALF) == Half.BOTTOM ? Direction.UP : Direction.DOWN;
			appliedHitVec = Printer.applyHitVec(pos, blockState, side);
		} else {
			Printer.PlacementClick placementClick = Printer.createVanillaPlacementClick(
				minecraftClient.level,
				pos,
				blockState,
				minecraftClient.level.getBlockState(pos),
				sideOrig,
				player.isSecondaryUseActive(),
				getFacingOrder()
			);
			if (placementClick == null) {
				MessageHolder.sendDebugMessage("Handling placeBlock failed because vanilla click target is not valid at " + pos.toShortString());
				return false;
			}
			clickPos = placementClick.blockPos();
			side = placementClick.side();
			appliedHitVec = placementClick.hitVec();
		}
		BlockHitResult blockHitResult = new BlockHitResult(appliedHitVec, side, clickPos, true);
		if (!Printer.canPlaceSourceDerivedState(player, blockState, blockHitResult)) {
			MessageHolder.sendDebugMessage("Handling placeBlock failed because vanilla placement would derive a different state at " + pos.toShortString());
			return false;
		}
		if (!pickFirst(blockState, pos)) {
			MessageHolder.sendDebugMessage("Cannot pick block for " + pos.toShortString());
			return false;
		}
		ItemStack pickedItem = getSchematicStackForState(minecraftClient, blockState, pos);
		if (pickedItem.getItem() == currentHandling && Printer.doSchematicWorldPickBlock(minecraftClient, blockState, pos)) {
			MessageHolder.sendOrderMessage("Placing " + blockState.getBlock().getDescriptionId() + " at " + pos.toShortString() + " stack at hand is " + player.getMainHandItem());

			MessageHolder.sendDebugMessage(player, "Placing " + blockState.getBlock().getDescriptionId() + " at " + pos.toShortString() + " facing : " + Printer.getSimplifiedFirstPropertyFacingValue(blockState));
			MessageHolder.sendDebugMessage(player, "Player facing is set to : " + fakeDirection + " Yaw : " + fakeYaw + " Pitch : " + fakePitch + " ticks : " + requestedTicks + " for pos " + pos.toShortString());
			if (!interactBlock(minecraftClient, blockHitResult).consumesAction()) {
				MessageHolder.sendDebugMessage("Handling placeBlock failed because interaction was not consumed at " + pos.toShortString());
				return false;
			}
			InventoryUtils.decrementCount(isCreative(player));
			blockPlacedInTick++;
			if ( !isCreative(player) && InventoryUtils.lastCount <= 0 && PRINTER_SLEEP_STACK_EMPTIED.getIntegerValue() > 0) {
				shouldReturnValue = true;
				Printer.recordExternalStackEmptiedSleep();
			}
			Printer.cacheEasyPlacePosition(pos, false);
			Printer.recordExternalPlacementInteraction();
			return true;
		}
		MessageHolder.sendDebugMessage("Handling placeBlock failed due to pickBlock assertion failure" + pos.toShortString() + " wanted item :" + pickedItem.getItem() + " current handling : " + currentHandling.asItem());
		return false;
	}

	private static boolean pickFirst(BlockState blockState, BlockPos pos) {
		final Minecraft minecraftClient = Minecraft.getInstance();
		if (Printer.doSchematicWorldPickBlock(minecraftClient, blockState, pos)) {
			currentHandling = getSchematicStackForState(minecraftClient, blockState, pos).getItem();
			handlingState = blockState;
			requestedTicks = Math.max(requestedTicks,0);
			return true;
		}
		return false;
	}

	//#if MC>=11700
	private record PosWithBlock(BlockPos pos, BlockState blockState) {
	}
	//#else
	//$$ // we just record pos + block and put in queue.
	//$$ private static class PosWithBlock {
	//$$ public BlockPos pos;
	//$$ public BlockState blockState;
	//$$ PosWithBlock(BlockPos pos, BlockState blockState) {
	//$$ 	this.pos = pos;
	//$$ 	this.blockState = blockState;
	//$$ }
	//$$ }
	//#endif

}
