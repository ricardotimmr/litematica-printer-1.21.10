package io.github.eatmyvenom.litematicin.utils;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement.RequiredEnabled;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.*;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
//#if MC>=12105
import fi.dy.masa.malilib.util.EquipmentUtils;
//#endif
import fi.dy.masa.malilib.util.position.IntBoundingBox;
import fi.dy.masa.malilib.util.position.LayerRange;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BaseCoralFanBlock;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DriedGhastBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.SpeleothemBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.Predicate;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.*;
import static io.github.eatmyvenom.litematicin.utils.BedrockBreaker.interactBlock;
import static io.github.eatmyvenom.litematicin.utils.BedrockBreaker.isReplaceable;
import static io.github.eatmyvenom.litematicin.utils.FakeAccurateBlockPlacement.getYaw;
import static io.github.eatmyvenom.litematicin.utils.InventoryUtils.*;

@SuppressWarnings("ConstantConditions")
public class Printer {

	private static final HashSet<Long> signCache = new HashSet<>();
	private static final LinkedHashMap<Tuple<Long, Boolean>, PositionCache> positionCache = new LinkedHashMap<>();
	private static Box CURRENT_BOX = null;
	// For printing delay
	public static boolean isSleeping = false;
	public static long lastPlaced = System.currentTimeMillis();
	public static Breaker breaker = new Breaker();
	public static int worldBottomY = 0; // this is handled in MinecraftClientMixin.joinWorld callback
	public static int worldTopY = 256;
	private static final LinkedHashMap<Long, String> causeMap = new LinkedHashMap<>();
	private static final Long2LongOpenHashMap referenceMap = new Long2LongOpenHashMap();
	private static final long DEFAULT_POSITION_CACHE_TIMEOUT_NANOS = 2_800_000_000L;
	private static final long POSITION_CACHE_PRUNE_INTERVAL_NANOS = 250_000_000L;
	private static final Direction[] ALL_DIRECTIONS = Direction.values();
	private static long lastPositionCachePrune = 0L;

	private record Tuple<A, B>(A getA, B getB) {
	}

	record PlacementClick(BlockPos blockPos, Direction side, Vec3 hitVec) {
	}

	private record CeilingHangingSignPlacement(boolean attached, int rotation) {
	}

	private enum FakePlacementResult {
		NONE,
		QUEUED,
		PLACED
	}

	private static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	private static void updateLastPlacedAfterInteraction() {
		updateLastPlacedAfterInteraction(0L);
	}

	private static void updateLastPlacedAfterInteraction(long extraDelayMillis) {
		lastPlaced = Math.max(lastPlaced, currentTimeMillis() + extraDelayMillis);
	}

	static void recordExternalPlacementInteraction() {
		updateLastPlacedAfterInteraction();
	}

	static void recordExternalStackEmptiedSleep() {
		lastPlaced = Math.max(lastPlaced, currentTimeMillis() + PRINTER_SLEEP_STACK_EMPTIED.getIntegerValue());
	}


	public static boolean sameBlockState(BlockState stateA, BlockState stateB) {
		if (stateA == stateB) {
			return true;
		}
		if (stateA.getBlock() != stateB.getBlock() || stateA.getProperties().size() != stateB.getProperties().size()) {
			return false;
		}
		for (Property<?> property : stateA.getProperties()) {
			if (!stateB.hasProperty(property)) {
				return false;
			}
			if (isPrinterIgnoredProperty(stateA, property)) {
				continue;
			}
			if (!samePropertyValue(stateA, stateB, property)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isPrinterIgnoredProperty(BlockState state, Property<?> property) {
		Block block = state.getBlock();
		if (block instanceof LeavesBlock) {
			return property == LeavesBlock.DISTANCE || property == LeavesBlock.PERSISTENT;
		}
		if (block instanceof ButtonBlock) {
			return property == ButtonBlock.POWERED;
		}
		if (block instanceof BarrelBlock) {
			return property == BarrelBlock.OPEN;
		}
		if (block instanceof BedBlock) {
			return property == BedBlock.OCCUPIED;
		}
		if (block instanceof BellBlock) {
			return property == BellBlock.POWERED;
		}
		if (block instanceof BubbleColumnBlock) {
			return property == BubbleColumnBlock.DRAG_DOWN;
		}
		if (block instanceof PressurePlateBlock) {
			return property == PressurePlateBlock.POWERED;
		}
		if (block instanceof WeightedPressurePlateBlock) {
			return property == WeightedPressurePlateBlock.POWER;
		}
		if (block instanceof DoorBlock) {
			return property == DoorBlock.POWERED;
		}
		if (block instanceof TrapDoorBlock) {
			return property == TrapDoorBlock.POWERED;
		}
		if (block instanceof FenceGateBlock) {
			return property == FenceGateBlock.POWERED || property == FenceGateBlock.IN_WALL;
		}
		if (block instanceof PoweredRailBlock) {
			return property == PoweredRailBlock.POWERED;
		}
		if (block instanceof DispenserBlock) {
			return property == DispenserBlock.TRIGGERED;
		}
		if (block instanceof HopperBlock) {
			return property == HopperBlock.ENABLED;
		}
		if (block instanceof NoteBlock) {
			return property == NoteBlock.POWERED;
		}
		if (block instanceof RepeaterBlock) {
			return property == DiodeBlock.POWERED || property == RepeaterBlock.LOCKED;
		}
		if (block instanceof DiodeBlock) {
			return property == DiodeBlock.POWERED;
		}
		if (block instanceof RedstoneLampBlock || block instanceof RedStoneOreBlock || block instanceof RedstoneTorchBlock) {
			return property == BlockStateProperties.LIT;
		}
		if (block instanceof AbstractSkullBlock) {
			return property == AbstractSkullBlock.POWERED;
		}
		if (block instanceof LecternBlock) {
			return property == LecternBlock.POWERED;
		}
		if (block instanceof TargetBlock) {
			return isPropertyNamed(property, "power");
		}
		if (block instanceof WallBlock) {
			return property == WallBlock.UP || isHorizontalConnectionProperty(property);
		}
		if (block instanceof FenceBlock || block instanceof IronBarsBlock) {
			return isHorizontalConnectionProperty(property);
		}
		if (block instanceof ScaffoldingBlock) {
			return property == ScaffoldingBlock.DISTANCE || property == ScaffoldingBlock.BOTTOM;
		}
		if (block instanceof BambooStalkBlock) {
			return property == BambooStalkBlock.AGE || property == BambooStalkBlock.LEAVES || property == BambooStalkBlock.STAGE;
		}
		if (block instanceof BigDripleafBlock) {
			return isPropertyNamed(property, "tilt");
		}
		if (block instanceof MangrovePropaguleBlock) {
			return property == SaplingBlock.STAGE || property == MangrovePropaguleBlock.AGE;
		}
		if (block instanceof SaplingBlock) {
			return property == SaplingBlock.STAGE;
		}
		if (block instanceof FarmlandBlock) {
			return property == FarmlandBlock.MOISTURE;
		}
		if (block instanceof SnowyBlock) {
			return property == SnowyBlock.SNOWY;
		}
		if (block instanceof FrostedIceBlock) {
			return property == FrostedIceBlock.AGE;
		}
		if (block instanceof SnifferEggBlock) {
			return property == SnifferEggBlock.HATCH;
		}
		if (block instanceof TurtleEggBlock) {
			return property == TurtleEggBlock.HATCH;
		}
		if (block instanceof GrowingPlantHeadBlock && isPropertyNamed(property, "age")) {
			return true;
		}
		if (isGrowthAgeProperty(block, property)) {
			return true;
		}
		if ((block instanceof CaveVinesBlock || block instanceof CaveVinesPlantBlock)
			&& isPropertyNamed(property, "berries")) {
			return true;
		}
		if (block instanceof DriedGhastBlock) {
			return property == DriedGhastBlock.HYDRATION_LEVEL;
		}
		if (block instanceof PotentSulfurBlock) {
			return property == PotentSulfurBlock.STATE;
		}
		if (block instanceof ShelfBlock) {
			return property == ShelfBlock.POWERED || property == ShelfBlock.SIDE_CHAIN_PART;
		}
		if (block instanceof CopperBulbBlock) {
			return property == CopperBulbBlock.POWERED;
		}
		if (block instanceof LightningRodBlock) {
			return property == LightningRodBlock.POWERED;
		}
		if (block instanceof CreakingHeartBlock) {
			return property == CreakingHeartBlock.STATE || property == CreakingHeartBlock.NATURAL;
		}
		if (block instanceof CampfireBlock) {
			return property == CampfireBlock.SIGNAL_FIRE;
		}
		if (block instanceof AbstractFurnaceBlock) {
			return property == AbstractFurnaceBlock.LIT;
		}
		if (block instanceof BrewingStandBlock) {
			for (BooleanProperty bottleProperty : BrewingStandBlock.HAS_BOTTLE) {
				if (property == bottleProperty) {
					return true;
				}
			}
		}
		if (block instanceof CocoaBlock) {
			return property == CocoaBlock.AGE;
		}
		if (block instanceof ChiseledBookShelfBlock) {
			return ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.contains(property);
		}
		if (block instanceof DecoratedPotBlock) {
			return property == DecoratedPotBlock.CRACKED;
		}
		if (block instanceof TntBlock) {
			return property == TntBlock.UNSTABLE;
		}
		if (block instanceof CrafterBlock) {
			return property == CrafterBlock.CRAFTING || property == CrafterBlock.TRIGGERED;
		}
		if (block instanceof HangingMossBlock) {
			return property == HangingMossBlock.TIP;
		}
		if (block instanceof VaultBlock) {
			return property == VaultBlock.STATE || property == VaultBlock.OMINOUS;
		}
		if (block instanceof TrialSpawnerBlock) {
			return property == TrialSpawnerBlock.STATE || property == TrialSpawnerBlock.OMINOUS;
		}
		if (block instanceof SculkSensorBlock) {
			return property == SculkSensorBlock.PHASE || property == SculkSensorBlock.POWER;
		}
		if (block instanceof SculkCatalystBlock) {
			return property == SculkCatalystBlock.PULSE;
		}
		if (block instanceof SculkShriekerBlock) {
			return property == SculkShriekerBlock.SHRIEKING || property == SculkShriekerBlock.CAN_SUMMON;
		}
		if (block instanceof DaylightDetectorBlock) {
			return property == DaylightDetectorBlock.POWER;
		}
		if (block instanceof BrushableBlock) {
			return "dusted".equals(property.getName());
		}
		if (block instanceof TripWireHookBlock) {
			return property == TripWireHookBlock.ATTACHED || property == TripWireHookBlock.POWERED;
		}
		if (block instanceof TripWireBlock) {
			return property == TripWireBlock.ATTACHED
				|| property == TripWireBlock.DISARMED
				|| property == TripWireBlock.POWERED
				|| property == TripWireBlock.NORTH
				|| property == TripWireBlock.EAST
				|| property == TripWireBlock.SOUTH
				|| property == TripWireBlock.WEST;
		}
		return false;
	}

	private static boolean isGrowthAgeProperty(Block block, Property<?> property) {
		return isPropertyNamed(property, "age")
			&& (block instanceof CropBlock
			|| block instanceof StemBlock
			|| block instanceof NetherWartBlock
			|| block instanceof SweetBerryBushBlock
			|| block instanceof CactusBlock
			|| block instanceof SugarCaneBlock
			|| block instanceof PitcherCropBlock
			|| block instanceof ChorusFlowerBlock);
	}

	private static boolean isPropertyNamed(Property<?> property, String name) {
		return name.equals(property.getName());
	}

	private static boolean isHorizontalConnectionProperty(Property<?> property) {
		return isPropertyNamed(property, "north")
			|| isPropertyNamed(property, "east")
			|| isPropertyNamed(property, "south")
			|| isPropertyNamed(property, "west");
	}

	private static <T extends Comparable<T>> boolean samePropertyValue(BlockState stateA, BlockState stateB, Property<T> property) {
		if (stateA.getBlock() instanceof RedStoneWireBlock && property == RedStoneWireBlock.POWER) {
			return (stateA.getValue(RedStoneWireBlock.POWER) == 0) == (stateB.getValue(RedStoneWireBlock.POWER) == 0);
		}
		return Objects.equals(stateA.getValue(property), stateB.getValue(property));
	}

	// TODO: This must be moved to another class and not be static.
	// Simulates and returns if player can place block as wanted.
	private static boolean simulateFacingData(BlockState state, BlockPos blockPos, Vec3 hitVec) {
		if (!state.getProperties().contains(BlockStateProperties.FACING) && !state.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
			return true;
		}
		if (isClickFaceControlledBlock(state)) {
			return true;
		}
		// int 0 : none, 1 : clockwise, 2 : counterclockwise, 3 : reverse
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return true;
		}
		BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.NORTH, blockPos, false);
		Block block = state.getBlock();
		BlockPlaceContext ctx = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, state.getBlock().asItem().getDefaultInstance(), hitResult);
		BlockState testState;
		try {
			testState = block.getStateForPlacement(ctx);
		} catch (Exception e) { //doors wtf
			MessageHolder.sendMessageUncheckedUnique("Cannot get tested orientation of given block "+ state.getBlock().getName());
			//fallback to player horizontal facing...
			return player.getDirection() == getSimplifiedFirstPropertyFacingValue(state);
		}
		if (testState == null) {
			MessageHolder.sendMessageUncheckedUnique("Cannot get tested orientation of given block "+ state.getBlock().getName());
			return player.getDirection() == getSimplifiedFirstPropertyFacingValue(state);
		}
		Direction testFacing = getSimplifiedFirstPropertyFacingValue(testState);
		return testFacing == getSimplifiedFirstPropertyFacingValue(state);
	}

	private static boolean isClickFaceControlledBlock(BlockState state) {
		return state.is(Blocks.HOPPER)
			|| state.is(BlockTags.SHULKER_BOXES)
			|| state.getBlock() instanceof AmethystClusterBlock
			|| state.getBlock() instanceof LightningRodBlock
			|| state.getBlock() instanceof EndRodBlock;
	}

	@Nullable
	public static Direction getSimplifiedFirstPropertyFacingValue(BlockState stateIn)
	{
		//#if MC>=12105
		return fi.dy.masa.malilib.util.game.BlockUtils.getFirstPropertyFacingValue(stateIn).orElse(null);
		//#else
		//$$ return fi.dy.masa.malilib.util.BlockUtils.getFirstPropertyFacingValue(stateIn);
		//#endif
	}

	static boolean hasFrontAndTopOrientation(BlockState state) {
		return state.hasProperty(BlockStateProperties.ORIENTATION)
			&& (state.getBlock() instanceof CrafterBlock || state.getBlock() instanceof JigsawBlock);
	}

	static Direction getFrontAndTopLookDirection(BlockState state) {
		FrontAndTop orientation = state.getValue(BlockStateProperties.ORIENTATION);
		if (state.getBlock() instanceof JigsawBlock) {
			Direction front = orientation.front();
			return front.getAxis() == Direction.Axis.Y ? orientation.top().getOpposite() : null;
		}
		return orientation.front().getOpposite();
	}

	static Direction getFrontAndTopPlacementSide(BlockState state) {
		FrontAndTop orientation = state.getValue(BlockStateProperties.ORIENTATION);
		if (state.getBlock() instanceof JigsawBlock) {
			return orientation.front();
		}
		return getFrontAndTopLookDirection(state);
	}

	static Direction getFrontAndTopHorizontalDirection(BlockState state) {
		FrontAndTop orientation = state.getValue(BlockStateProperties.ORIENTATION);
		Direction front = orientation.front();
		if (state.getBlock() instanceof JigsawBlock) {
			return front.getAxis() == Direction.Axis.Y ? orientation.top().getOpposite() : front;
		}
		if (front == Direction.DOWN) {
			return orientation.top().getOpposite();
		}
		if (front == Direction.UP) {
			return orientation.top();
		}
		return front.getOpposite();
	}

	static boolean hasDripleafPlacementFacing(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BigDripleafBlock
			|| block instanceof BigDripleafStemBlock
			|| block instanceof SmallDripleafBlock;
	}

	static boolean canPlaceDripleaf(Level world, BlockPos pos, BlockState state, Direction horizontalFacing) {
		if (isAutoPlacedUpperHalf(state)) {
			return false;
		}
		if (state.getBlock() instanceof SmallDripleafBlock && !world.getBlockState(pos.above()).canBeReplaced()) {
			return false;
		}
		Direction placementFacing = getDripleafPlacementFacing(world, pos, state, horizontalFacing);
		return placementFacing != null
			&& state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
			&& placementFacing == state.getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Nullable
	static Direction getDripleafPlacementLookDirection(Level world, BlockPos pos, BlockState state) {
		if (!hasDripleafPlacementFacing(state) || isAutoPlacedUpperHalf(state) || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return null;
		}
		if (state.getBlock() instanceof SmallDripleafBlock && !world.getBlockState(pos.above()).canBeReplaced()) {
			return null;
		}
		Direction wantedFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		if (state.getBlock() instanceof BigDripleafBlock || state.getBlock() instanceof BigDripleafStemBlock) {
			BlockState belowState = world.getBlockState(pos.below());
			if (isBigDripleafColumnState(belowState)) {
				return belowState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
					&& belowState.getValue(BlockStateProperties.HORIZONTAL_FACING) == wantedFacing ? wantedFacing.getOpposite() : null;
			}
		}
		return wantedFacing.getOpposite();
	}

	private static Direction getDripleafPlacementFacing(Level world, BlockPos pos, BlockState state, Direction horizontalFacing) {
		if (horizontalFacing == null || !hasDripleafPlacementFacing(state)) {
			return null;
		}
		if (state.getBlock() instanceof BigDripleafBlock || state.getBlock() instanceof BigDripleafStemBlock) {
			BlockState belowState = world.getBlockState(pos.below());
			if (isBigDripleafColumnState(belowState) && belowState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
				return belowState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			}
		}
		return horizontalFacing.getOpposite();
	}

	private static boolean isBigDripleafColumnState(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BigDripleafBlock || block instanceof BigDripleafStemBlock;
	}

	static boolean canPlaceBigDripleafStemAsLeaf(Level world, BlockPos pos, BlockState state) {
		return state.getBlock() instanceof BigDripleafStemBlock
			&& hasBigDripleafLeafSupport(world, pos)
			&& getDripleafPlacementLookDirection(world, pos, state) != null;
	}

	private static boolean canPlaceBigDripleafStemAsLeaf(Level world, BlockPos pos, BlockState state, Direction horizontalFacing) {
		return state.getBlock() instanceof BigDripleafStemBlock
			&& hasBigDripleafLeafSupport(world, pos)
			&& canPlaceDripleaf(world, pos, state, horizontalFacing);
	}

	private static boolean hasBigDripleafLeafSupport(Level world, BlockPos pos) {
		BlockState belowState = world.getBlockState(pos.below());
		return isBigDripleafColumnState(belowState) || belowState.is(BlockTags.SUPPORTS_BIG_DRIPLEAF);
	}

	static boolean hasSpeleothemPlacement(BlockState state) {
		return state.getBlock() instanceof SpeleothemBlock;
	}

	static boolean canPlaceSpeleothem(Level world, BlockPos pos, BlockState state, Direction nearestLookingVerticalDirection, boolean secondaryUseActive) {
		if (!hasSpeleothemPlacement(state) || nearestLookingVerticalDirection == null || nearestLookingVerticalDirection.getAxis() != Direction.Axis.Y) {
			return false;
		}
		Direction initialTipDirection = nearestLookingVerticalDirection.getOpposite();
		Direction tipDirection = getSpeleothemPlacementTipDirection(world, pos, state, initialTipDirection);
		if (tipDirection == null || tipDirection != state.getValue(SpeleothemBlock.TIP_DIRECTION)) {
			return false;
		}
		SpeleothemThickness thickness = calculateSpeleothemThickness(world, pos, state, tipDirection, !secondaryUseActive);
		return thickness == state.getValue(SpeleothemBlock.THICKNESS);
	}

	@Nullable
	static Direction getSpeleothemPlacementLookDirection(Level world, BlockPos pos, BlockState state, boolean secondaryUseActive) {
		if (!hasSpeleothemPlacement(state)) {
			return null;
		}
		Direction lookDirection = state.getValue(SpeleothemBlock.TIP_DIRECTION).getOpposite();
		return canPlaceSpeleothem(world, pos, state, lookDirection, secondaryUseActive) ? lookDirection : null;
	}

	private static Direction getSpeleothemPlacementTipDirection(Level world, BlockPos pos, BlockState state, Direction initialTipDirection) {
		if (isValidSpeleothemPlacement(world, pos, state, initialTipDirection)) {
			return initialTipDirection;
		}
		Direction opposite = initialTipDirection.getOpposite();
		return isValidSpeleothemPlacement(world, pos, state, opposite) ? opposite : null;
	}

	private static boolean isValidSpeleothemPlacement(Level world, BlockPos pos, BlockState state, Direction tipDirection) {
		BlockPos supportPos = pos.relative(tipDirection.getOpposite());
		BlockState supportState = world.getBlockState(supportPos);
		return supportState.isFaceSturdy(world, supportPos, tipDirection)
			|| supportState.is(state.getBlock()) && isSpeleothemWithDirection(supportState, tipDirection);
	}

	private static boolean isSpeleothemWithDirection(BlockState state, Direction tipDirection) {
		return state.is(BlockTags.SPELEOTHEMS)
			&& state.hasProperty(SpeleothemBlock.TIP_DIRECTION)
			&& state.getValue(SpeleothemBlock.TIP_DIRECTION) == tipDirection;
	}

	private static SpeleothemThickness calculateSpeleothemThickness(Level world, BlockPos pos, BlockState state, Direction tipDirection, boolean mergeTip) {
		Direction opposite = tipDirection.getOpposite();
		BlockState forwardState = world.getBlockState(pos.relative(tipDirection));
		if (forwardState.is(state.getBlock()) && isSpeleothemWithDirection(forwardState, opposite)) {
			if (mergeTip || forwardState.getValue(SpeleothemBlock.THICKNESS) == SpeleothemThickness.TIP_MERGE) {
				return SpeleothemThickness.TIP_MERGE;
			}
			return SpeleothemThickness.TIP;
		}
		if (!isSpeleothemWithDirection(forwardState, tipDirection)) {
			return SpeleothemThickness.TIP;
		}
		SpeleothemThickness forwardThickness = forwardState.getValue(SpeleothemBlock.THICKNESS);
		if (forwardThickness == SpeleothemThickness.TIP || forwardThickness == SpeleothemThickness.TIP_MERGE) {
			return SpeleothemThickness.FRUSTUM;
		}
		BlockState backwardState = world.getBlockState(pos.relative(opposite));
		return isSpeleothemWithDirection(backwardState, tipDirection) ? SpeleothemThickness.MIDDLE : SpeleothemThickness.BASE;
	}

	private static boolean isAutoPlacedUpperHalf(BlockState state) {
		return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
			&& state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
	}

	private static Direction getNearestLookingVerticalDirection(LocalPlayer player) {
		return Direction.getFacingAxis(player, Direction.Axis.Y);
	}

	static boolean hasSegmentedHorizontalPlacement(BlockState state) {
		return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && getSegmentAmountProperty(state) != null;
	}

	static boolean hasMossyCarpetPlacement(BlockState state) {
		return state.getBlock() instanceof MossyCarpetBlock;
	}

	static boolean canPlaceMossyCarpet(Level world, BlockPos pos, BlockState state) {
		if (!hasMossyCarpetPlacement(state)) {
			return true;
		}
		if (!state.getValue(MossyCarpetBlock.BASE)) {
			return false;
		}
		return sameBlockState(getMossyCarpetPlacementState(world, pos, state), state);
	}

	private static BlockState getMossyCarpetPlacementState(Level world, BlockPos pos, BlockState state) {
		BlockState placementState = state.getBlock().defaultBlockState()
			.setValue(MossyCarpetBlock.BASE, true);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			WallSide wallSide = MultifaceBlock.canAttachTo(world, pos, direction) ? WallSide.LOW : WallSide.NONE;
			if (wallSide == WallSide.LOW) {
				BlockState aboveState = world.getBlockState(pos.above());
				if (aboveState.is(state.getBlock())
					&& aboveState.hasProperty(MossyCarpetBlock.getPropertyForFace(direction))
					&& aboveState.getValue(MossyCarpetBlock.getPropertyForFace(direction)) != WallSide.NONE
					&& !aboveState.getValue(MossyCarpetBlock.BASE)) {
					wallSide = WallSide.TALL;
				}
			}
			placementState = placementState.setValue(MossyCarpetBlock.getPropertyForFace(direction), wallSide);
		}
		return placementState;
	}

	static boolean hasMultifaceOrVinePlacement(BlockState state) {
		return state.getBlock() instanceof MultifaceBlock || state.getBlock() instanceof VineBlock;
	}

	@Nullable
	static Direction getMultifaceOrVinePlacementLookDirection(Level world, BlockPos pos, BlockState state) {
		return getMultifaceOrVinePlacementLookDirection(world, pos, state, ALL_DIRECTIONS, false);
	}

	@Nullable
	private static Direction getMultifaceOrVinePlacementLookDirection(Level world, BlockPos pos, BlockState state, Direction[] lookDirections) {
		return getMultifaceOrVinePlacementLookDirection(world, pos, state, lookDirections, true);
	}

	@Nullable
	private static Direction getMultifaceOrVinePlacementLookDirection(Level world, BlockPos pos, BlockState state, Direction[] lookDirections, boolean strictLookOrder) {
		if (!hasMultifaceOrVinePlacement(state)) {
			return null;
		}
		BlockState clientState = world.getBlockState(pos);
		if (hasExtraMultifaceOrVineFace(clientState, state)) {
			return null;
		}
		for (Direction face : lookDirections) {
			BooleanProperty property = getMultifaceOrVineFaceProperty(state, face);
			if (property == null || !state.hasProperty(property)) {
				continue;
			}
			if (clientState.is(state.getBlock()) && clientState.hasProperty(property) && clientState.getValue(property)) {
				continue;
			}
			if (!canSupportMultifaceOrVineFace(world, pos, clientState, state, face)) {
				continue;
			}
			if (state.getValue(property)) {
				return face;
			}
			if (strictLookOrder) {
				return null;
			}
		}
		return null;
	}

	static boolean canPlaceMultifaceOrVine(Level world, BlockPos pos, BlockState state, Direction[] lookDirections) {
		return !hasMultifaceOrVinePlacement(state)
			|| getMultifaceOrVinePlacementLookDirection(world, pos, state, lookDirections) != null;
	}

	static boolean canPlaceMultifaceOrVineFace(Level world, BlockPos pos, BlockState state, Direction face) {
		if (!hasMultifaceOrVinePlacement(state)) {
			return true;
		}
		BlockState clientState = world.getBlockState(pos);
		if (hasExtraMultifaceOrVineFace(clientState, state)) {
			return false;
		}
		BooleanProperty property = getMultifaceOrVineFaceProperty(state, face);
		return property != null
			&& state.hasProperty(property)
			&& state.getValue(property)
			&& !(clientState.is(state.getBlock()) && clientState.hasProperty(property) && clientState.getValue(property))
			&& canSupportMultifaceOrVineFace(world, pos, clientState, state, face);
	}

	static Direction getMultifaceOrVineClickSide(Direction lookDirection) {
		return lookDirection.getOpposite();
	}

	@Nullable
	private static BooleanProperty getMultifaceOrVineFaceProperty(BlockState state, Direction face) {
		Block block = state.getBlock();
		if (block instanceof MultifaceBlock) {
			return MultifaceBlock.getFaceProperty(face);
		}
		if (block instanceof VineBlock && face != Direction.DOWN) {
			return VineBlock.getPropertyForFace(face);
		}
		return null;
	}

	private static boolean hasExtraMultifaceOrVineFace(BlockState clientState, BlockState targetState) {
		if (!clientState.is(targetState.getBlock())) {
			return false;
		}
		for (Direction face : ALL_DIRECTIONS) {
			BooleanProperty property = getMultifaceOrVineFaceProperty(targetState, face);
			if (property != null
				&& clientState.hasProperty(property)
				&& targetState.hasProperty(property)
				&& clientState.getValue(property)
				&& !targetState.getValue(property)) {
				return true;
			}
		}
		return false;
	}

	private static boolean canSupportMultifaceOrVineFace(Level world, BlockPos pos, BlockState clientState, BlockState targetState, Direction face) {
		Block block = targetState.getBlock();
		if (block instanceof MultifaceBlock multifaceBlock) {
			return multifaceBlock.isValidStateForPlacement(world, clientState, pos, face);
		}
		if (block instanceof VineBlock) {
			return canSupportVineFace(world, pos, targetState, face);
		}
		return false;
	}

	private static boolean canSupportVineFace(Level world, BlockPos pos, BlockState targetState, Direction face) {
		if (face == Direction.DOWN) {
			return false;
		}
		if (VineBlock.isAcceptableNeighbour(world, pos.relative(face), face)) {
			return true;
		}
		if (face.getAxis() == Direction.Axis.Y) {
			return false;
		}
		BooleanProperty property = VineBlock.getPropertyForFace(face);
		BlockState aboveState = world.getBlockState(pos.above());
		return aboveState.is(targetState.getBlock())
			&& aboveState.hasProperty(property)
			&& aboveState.getValue(property);
	}

	static boolean canPlaceSegmentedHorizontalBlock(Level world, BlockPos pos, BlockState state, @Nullable Direction horizontalFacing, boolean secondaryUseActive) {
		if (!hasSegmentedHorizontalPlacement(state)) {
			return true;
		}
		BlockState clientState = world.getBlockState(pos);
		if (clientState.is(state.getBlock())) {
			return !secondaryUseActive
				&& hasSegmentedHorizontalPlacement(clientState)
				&& clientState.getValue(BlockStateProperties.HORIZONTAL_FACING) == state.getValue(BlockStateProperties.HORIZONTAL_FACING)
				&& getSegmentAmount(clientState) < getSegmentAmount(state);
		}
		return horizontalFacing == null || horizontalFacing.getOpposite() == state.getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Nullable
	private static IntegerProperty getSegmentAmountProperty(BlockState state) {
		if (state.hasProperty(BlockStateProperties.SEGMENT_AMOUNT)) {
			return BlockStateProperties.SEGMENT_AMOUNT;
		}
		if (state.hasProperty(BlockStateProperties.FLOWER_AMOUNT)) {
			return BlockStateProperties.FLOWER_AMOUNT;
		}
		return null;
	}

	private static int getSegmentAmount(BlockState state) {
		IntegerProperty amountProperty = getSegmentAmountProperty(state);
		return amountProperty == null ? 1 : state.getValue(amountProperty);
	}

	static boolean hasLanternHangingPlacement(BlockState state) {
		return state.getBlock() instanceof LanternBlock && state.hasProperty(LanternBlock.HANGING);
	}

	static Direction getLanternPlacementSide(BlockState state) {
		return state.getValue(LanternBlock.HANGING) ? Direction.DOWN : Direction.UP;
	}

	static boolean canPlaceLantern(Level world, BlockPos pos, BlockState state) {
		if (!hasLanternHangingPlacement(state)) {
			return true;
		}
		BlockPos supportPos = pos.relative(getLanternPlacementSide(state).getOpposite());
		return !isReplaceable(world.getBlockState(supportPos)) && state.canSurvive(world, pos);
	}

	private static boolean hasSourceDerivedPlacementState(BlockState state) {
		return hasFullSourceDerivedPlacementState(state)
			|| hasWaterloggedSourceOnlyPlacementState(state);
	}

	private static boolean hasFullSourceDerivedPlacementState(BlockState state) {
		Block block = state.getBlock();
		return block instanceof ChestBlock
			|| block instanceof BellBlock
			|| block instanceof LadderBlock
			|| block instanceof DoorBlock
			|| block instanceof DecoratedPotBlock
			|| block instanceof DriedGhastBlock
			|| block instanceof RotatedPillarBlock
			|| block instanceof SlabBlock
			|| block instanceof ShelfBlock
			|| block instanceof StairBlock
			|| block instanceof TrapDoorBlock
			|| block instanceof WallHangingSignBlock
			|| block instanceof CreakingHeartBlock
			|| block instanceof CopperGolemStatueBlock
			|| block instanceof GrowingPlantBlock
			|| block instanceof SimpleWaterloggedBlock && !hasPostPlacementWaterloggedSourceState(state)
			|| hasLookOrderPlacementState(state)
			|| hasFrontAndTopOrientation(state)
			|| isClickFaceControlledBlock(state);
	}

	private static boolean hasWaterloggedSourceOnlyPlacementState(BlockState state) {
		return state.getBlock() instanceof SimpleWaterloggedBlock
			&& state.hasProperty(BlockStateProperties.WATERLOGGED)
			&& hasPostPlacementWaterloggedSourceState(state);
	}

	private static boolean hasPostPlacementWaterloggedSourceState(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BaseRailBlock
			|| block instanceof CandleBlock
			|| block instanceof MultifaceBlock
			|| block instanceof SeaPickleBlock
			|| block instanceof CampfireBlock;
	}

	private static boolean hasLookOrderPlacementState(BlockState state) {
		Block block = state.getBlock();
		return block instanceof BaseCoralWallFanBlock
			|| block instanceof ButtonBlock
			|| block instanceof CocoaBlock
			|| block instanceof GrindstoneBlock
			|| block instanceof LanternBlock
			|| block instanceof TripWireHookBlock
			|| block instanceof WallBannerBlock
			|| block instanceof WallSignBlock
			|| block instanceof WallSkullBlock
			|| block instanceof WallTorchBlock;
	}

	static boolean canPlaceSourceDerivedState(LocalPlayer player, BlockState state, BlockHitResult hitResult) {
		if (!hasSourceDerivedPlacementState(state)) {
			return true;
		}
		BlockState placementState = getSourcePlacementState(player, state, hitResult);
		return placementState != null && matchesSourceDerivedPlacementState(placementState, state);
	}

	private static boolean matchesSourceDerivedPlacementState(BlockState placementState, BlockState expectedState) {
		if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() && IsBlockSupportedCarpet(expectedState.getBlock())) {
			return true;
		}
		if (expectedState.getBlock() instanceof DoorBlock) {
			return matchesDoorPlacementState(placementState, expectedState);
		}
		if (expectedState.getBlock() instanceof CopperGolemStatueBlock) {
			return matchesCopperGolemStatuePlacementState(placementState, expectedState);
		}
		if (hasWaterloggedSourceOnlyPlacementState(expectedState)) {
			return matchesWaterloggedSourceOnlyPlacementState(placementState, expectedState);
		}
		return sameBlockState(placementState, expectedState);
	}

	private static boolean matchesWaterloggedSourceOnlyPlacementState(BlockState placementState, BlockState expectedState) {
		if (placementState.getBlock() != expectedState.getBlock()
			|| !placementState.hasProperty(BlockStateProperties.WATERLOGGED)
			|| placementState.getValue(BlockStateProperties.WATERLOGGED) != expectedState.getValue(BlockStateProperties.WATERLOGGED)) {
			return false;
		}
		if (expectedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
			&& (!placementState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
			|| placementState.getValue(BlockStateProperties.HORIZONTAL_FACING) != expectedState.getValue(BlockStateProperties.HORIZONTAL_FACING))) {
			return false;
		}
		if (expectedState.hasProperty(BlockStateProperties.VERTICAL_DIRECTION)
			&& (!placementState.hasProperty(BlockStateProperties.VERTICAL_DIRECTION)
			|| placementState.getValue(BlockStateProperties.VERTICAL_DIRECTION) != expectedState.getValue(BlockStateProperties.VERTICAL_DIRECTION))) {
			return false;
		}
		return !expectedState.hasProperty(BlockStateProperties.FACING)
			|| placementState.hasProperty(BlockStateProperties.FACING)
			&& placementState.getValue(BlockStateProperties.FACING) == expectedState.getValue(BlockStateProperties.FACING);
	}

	private static boolean matchesDoorPlacementState(BlockState placementState, BlockState expectedState) {
		return placementState.getBlock() == expectedState.getBlock()
			&& placementState.getValue(DoorBlock.FACING) == expectedState.getValue(DoorBlock.FACING)
			&& placementState.getValue(DoorBlock.HINGE) == expectedState.getValue(DoorBlock.HINGE)
			&& placementState.getValue(DoorBlock.HALF) == expectedState.getValue(DoorBlock.HALF);
	}

	private static boolean matchesCopperGolemStatuePlacementState(BlockState placementState, BlockState expectedState) {
		return placementState.getBlock() == expectedState.getBlock()
			&& placementState.getValue(CopperGolemStatueBlock.FACING) == expectedState.getValue(CopperGolemStatueBlock.FACING)
			&& placementState.getValue(CopperGolemStatueBlock.WATERLOGGED) == expectedState.getValue(CopperGolemStatueBlock.WATERLOGGED);
	}

	@Nullable
	private static BlockState getSourcePlacementState(LocalPlayer player, BlockState state, BlockHitResult hitResult) {
		try {
			ItemStack stack = getSourcePlacementStack(player, state, hitResult.getBlockPos());
			BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hitResult);
			BlockState placementState = state.getBlock().getStateForPlacement(context);
			return applyStackBlockStateProperties(stack, placementState);
		} catch (RuntimeException e) {
			MessageHolder.sendMessageUncheckedUnique("Cannot simulate placement state of " + state.getBlock().getName());
			return null;
		}
	}

	private static ItemStack getSourcePlacementStack(LocalPlayer player, BlockState state, BlockPos pos) {
		if (player != null) {
			ItemStack stack = player.getMainHandItem();
			if (isUsableSourcePlacementStack(stack, state)) {
				return stack;
			}
		}
		Minecraft client = Minecraft.getInstance();
		Level schematicWorld = SchematicWorldHandler.getSchematicWorld();
		if (client != null && player != null && schematicWorld != null) {
			ItemStack stack = getStackForState(client, state, schematicWorld, pos);
			if (isUsableSourcePlacementStack(stack, state)) {
				return stack;
			}
		}
		return state.getBlock().asItem().getDefaultInstance();
	}

	private static boolean isUsableSourcePlacementStack(ItemStack stack, BlockState state) {
		return !stack.isEmpty() && !stack.is(Items.AIR) && stack.is(state.getBlock().asItem());
	}

	@Nullable
	private static BlockState applyStackBlockStateProperties(ItemStack stack, @Nullable BlockState placementState) {
		if (placementState == null || stack.isEmpty() || stack.is(Items.AIR)) {
			return placementState;
		}
		BlockItemStateProperties stateProperties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		return stateProperties.isEmpty() ? placementState : stateProperties.apply(placementState);
	}

	private static int getCopperGolemStatuePoseClicks(BlockState stateClient, BlockState stateSchematic) {
		int current = stateClient.getValue(CopperGolemStatueBlock.POSE).ordinal();
		int target = stateSchematic.getValue(CopperGolemStatueBlock.POSE).ordinal();
		int count = CopperGolemStatueBlock.Pose.values().length;
		return (target - current + count) % count;
	}

	private static boolean selectNonAxeHotbarSlot(Minecraft mc) {
		if (!canSelectHotbarSlot(mc)) {
			return false;
		}
		Inventory inventory = getInventory(mc.player);
		if (!inventory.getSelectedItem().is(ItemTags.AXES)) {
			clearCache();
			return true;
		}
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (!inventory.getItem(slot).is(ItemTags.AXES)) {
				inventory.setSelectedSlot(slot);
				mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
				clearCache();
				return !inventory.getSelectedItem().is(ItemTags.AXES);
			}
		}
		if (!canSwapWithInventoryMenu(mc)) {
			return false;
		}
		int selectedSlot = inventory.getSelectedSlot();
		int sourceSlot = getNonAxeNonHotbarInventorySlot(inventory);
		if (sourceSlot == -1) {
			sourceSlot = getEmptyNonHotbarInventorySlot(inventory);
		}
		if (sourceSlot == -1) {
			return false;
		}
		mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, sourceSlot, selectedSlot, ContainerInput.SWAP, mc.player);
		inventory.setSelectedSlot(selectedSlot);
		mc.getConnection().send(new ServerboundSetCarriedItemPacket(selectedSlot));
		clearCache();
		return !inventory.getSelectedItem().is(ItemTags.AXES);
	}

	private static Direction getPlacementSideForAxis(Direction.Axis axis) {
		if (axis == Direction.Axis.X) {
			return Direction.WEST;
		}
		if (axis == Direction.Axis.Y) {
			return Direction.DOWN;
		}
		return Direction.NORTH;
	}

	static boolean canPlaceFrontAndTopOrientation(BlockState state, Direction primaryFacing, Direction horizontalFacing) {
		FrontAndTop orientation = state.getValue(BlockStateProperties.ORIENTATION);
		Direction front = orientation.front();
		if (state.getBlock() instanceof JigsawBlock) {
			if (front.getAxis() == Direction.Axis.Y) {
				return horizontalFacing == orientation.top().getOpposite();
			}
			return orientation.top() == Direction.UP;
		}
		if (primaryFacing.getOpposite() != front) {
			return false;
		}
		if (front == Direction.DOWN) {
			return horizontalFacing == orientation.top().getOpposite();
		}
		if (front == Direction.UP) {
			return horizontalFacing == orientation.top();
		}
		return orientation.top() == Direction.UP;
	}

	static Direction getWallHangingSignPlacementSide(Level world, BlockPos pos, BlockState state) {
		Direction preferred = getWallHangingSignPreferredSide(state);
		if (canAttachWallHangingSignTo(world, pos, state, preferred)) {
			return preferred;
		}
		Direction other = preferred.getOpposite();
		if (canAttachWallHangingSignTo(world, pos, state, other)) {
			return other;
		}
		return preferred;
	}

	private static Direction getWallHangingSignPreferredSide(BlockState state) {
		return state.getValue(WallHangingSignBlock.FACING).getCounterClockWise();
	}

	private static boolean canAttachWallHangingSignTo(Level world, BlockPos pos, BlockState state, Direction side) {
		BlockPos supportPos = pos.relative(side.getOpposite());
		BlockState supportState = world.getBlockState(supportPos);
		if (supportState.is(BlockTags.WALL_HANGING_SIGNS)) {
			return supportState.hasProperty(WallHangingSignBlock.FACING)
				&& supportState.getValue(WallHangingSignBlock.FACING).getAxis().test(state.getValue(WallHangingSignBlock.FACING));
		}
		return supportState.isFaceSturdy(world, supportPos, side, SupportType.FULL);
	}

	static boolean canPlaceWallHangingSignFromPlayer(BlockState state, Direction[] orderedDirections, Direction clickedSide) {
		if (clickedSide == null) {
			return false;
		}
		Direction wantedFacing = state.getValue(WallHangingSignBlock.FACING);
		for (Direction direction : orderedDirections) {
			if (direction.getAxis().isHorizontal()) {
				if (direction.getAxis() == clickedSide.getAxis()) {
					continue;
				}
				return direction.getOpposite() == wantedFacing;
			}
		}
		return false;
	}

	static boolean canPlaceCeilingHangingSign(Level world, BlockPos pos, BlockState state, float yaw, boolean secondaryUseActive) {
		CeilingHangingSignPlacement placement = simulateCeilingHangingSignPlacement(world, pos, yaw, secondaryUseActive);
		return placement.attached() == state.getValue(CeilingHangingSignBlock.ATTACHED)
			&& placement.rotation() == state.getValue(CeilingHangingSignBlock.ROTATION);
	}

	static boolean hasYawRotation16Placement(BlockState state) {
		Block block = state.getBlock();
		return (block instanceof StandingSignBlock && !(block instanceof WallSignBlock))
			|| block instanceof BannerBlock
			|| block instanceof SkullBlock;
	}

	static boolean canPlaceYawRotation16(BlockState state, float yaw) {
		return getYawRotation16ForPlacement(state, yaw) == state.getValue(BlockStateProperties.ROTATION_16);
	}

	static float getYawForRotation16Placement(BlockState state) {
		int rotation = state.getValue(BlockStateProperties.ROTATION_16);
		float yaw = RotationSegment.convertToDegrees(rotation);
		if (state.getBlock() instanceof BannerBlock || state.getBlock() instanceof StandingSignBlock) {
			yaw -= 180.0F;
		}
		return yaw;
	}

	private static int getYawRotation16ForPlacement(BlockState state, float yaw) {
		if (state.getBlock() instanceof BannerBlock || state.getBlock() instanceof StandingSignBlock) {
			return RotationSegment.convertToSegment(yaw + 180.0F);
		}
		return RotationSegment.convertToSegment(yaw);
	}

	@Nullable
	static Float getCeilingHangingSignPlacementYaw(Level world, BlockPos pos, BlockState state, boolean secondaryUseActive) {
		boolean desiredAttached = state.getValue(CeilingHangingSignBlock.ATTACHED);
		int desiredRotation = state.getValue(CeilingHangingSignBlock.ROTATION);
		float yaw;
		if (desiredAttached) {
			yaw = RotationSegment.convertToDegrees(desiredRotation) - 180.0F;
		} else {
			Optional<Direction> direction = RotationSegment.convertToDirection(desiredRotation);
			if (direction.isEmpty()) {
				return null;
			}
			yaw = yawForHorizontalDirection(direction.get().getOpposite());
		}
		return canPlaceCeilingHangingSign(world, pos, state, yaw, secondaryUseActive) ? yaw : null;
	}

	static float yawForHorizontalDirection(Direction direction) {
		if (direction == Direction.EAST) {
			return -87.0F;
		}
		if (direction == Direction.WEST) {
			return 87.0F;
		}
		if (direction == Direction.NORTH) {
			return 177.0F;
		}
		if (direction == Direction.SOUTH) {
			return 3.0F;
		}
		return 0.0F;
	}

	private static CeilingHangingSignPlacement simulateCeilingHangingSignPlacement(Level world, BlockPos pos, float yaw, boolean secondaryUseActive) {
		BlockPos supportPos = pos.above();
		BlockState supportState = world.getBlockState(supportPos);
		Direction yawDirection = Direction.fromYRot(yaw);
		boolean attached = !Block.isFaceFull(supportState.getCollisionShape(world, supportPos), Direction.DOWN) || secondaryUseActive;
		if (supportState.is(BlockTags.ALL_HANGING_SIGNS) && !secondaryUseActive) {
			if (supportState.hasProperty(WallHangingSignBlock.FACING)) {
				Direction supportFacing = supportState.getValue(WallHangingSignBlock.FACING);
				if (supportFacing.getAxis().test(yawDirection)) {
					attached = false;
				}
			} else if (supportState.hasProperty(CeilingHangingSignBlock.ROTATION)) {
				Optional<Direction> supportDirection = RotationSegment.convertToDirection(supportState.getValue(CeilingHangingSignBlock.ROTATION));
				if (supportDirection.isPresent() && supportDirection.get().getAxis().test(yawDirection)) {
					attached = false;
				}
			}
		}
		int rotation = attached
			? RotationSegment.convertToSegment(yaw + 180.0F)
			: RotationSegment.convertToSegment(yawDirection.getOpposite());
		return new CeilingHangingSignPlacement(attached, rotation);
	}

	private static boolean isHangingSignChainClick(Block targetBlock, BlockState supportState, Direction clickedSide) {
		if (!(targetBlock instanceof WallHangingSignBlock || targetBlock instanceof CeilingHangingSignBlock) || !supportState.is(BlockTags.ALL_HANGING_SIGNS)) {
			return false;
		}
		if (targetBlock instanceof CeilingHangingSignBlock) {
			return clickedSide == Direction.DOWN;
		}
		return supportState.hasProperty(WallHangingSignBlock.FACING)
			&& supportState.getValue(WallHangingSignBlock.FACING).getAxis() != clickedSide.getAxis();
	}

	public static boolean canPickBlock(Minecraft mc, BlockState preference, BlockPos pos) {
		Level world = SchematicWorldHandler.getSchematicWorld();
		ItemStack stack = getStackForState(mc, preference, world, pos);
		if (stack.isEmpty()) {
			MessageHolder.sendDebugMessage(mc.player, "Cannot pick block " + preference.getBlock().getName() + " at " + pos.toShortString() + " because no stack");
			return false;
		}
		// Inventory Cache
		if (USE_INVENTORY_CACHE.getBooleanValue() && !ITEMS.isEmpty()) { // if cache is enabled and cache is not empty
			return io.github.eatmyvenom.litematicin.utils.InventoryUtils.swapToItem(mc, stack);
		}
		if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
			Inventory inv = getInventory(mc.player);
			if (!isCreative(mc.player)) {
				//#if MC>=12105
				if (EquipmentUtils.isRegularTool(stack) || stack.getItem() instanceof FlintAndSteelItem) {
				//#elseif MC>=12102
				//$$ if (stack.getItem() instanceof MiningToolItem || stack.getItem() instanceof FlintAndSteelItem) {
				//#else
				//$$ if (stack.getItem() instanceof ToolItem || stack.getItem() instanceof FlintAndSteelItem) {
				//#endif
					// manually search through inventories
					int slot = io.github.eatmyvenom.litematicin.utils.InventoryUtils.getSlotWithItem(inv, stack);
					if (slot == -1) {
						MessageHolder.sendDebugMessage(mc.player, "Cannot pick block " + preference.getBlock().getName() + " at " + pos.toShortString() + " because no slot");
						return false;
					}
					if (EASY_PLACE_MODE_HOTBAR_ONLY.getBooleanValue()) {
						boolean isHotbar = slot < 9;
						if (!isHotbar) {
							MessageHolder.sendDebugMessage(mc.player, "Cannot pick block " + preference.getBlock().getName() + " at " + pos.toShortString() + " because not in hotbar");
						}
						return isHotbar;
					}
					return true;
				}
				else {
					int slot = getSlotWithStack(inv, stack);
					if (slot == -1) {
						MessageHolder.sendDebugMessage(mc.player, "Cannot pick block " + preference.getBlock().getName() + " at " + pos.toShortString() + " because no slot");
						return false;
					}
					if (EASY_PLACE_MODE_HOTBAR_ONLY.getBooleanValue()) {
						boolean isHotbar = slot < 9;
						if (!isHotbar) {
							MessageHolder.sendDebugMessage(mc.player, "Cannot pick block " + preference.getBlock().getName() + " at " + pos.toShortString() + " because not in hotbar");
						}
						return isHotbar;
					}
				}
			}
			return true;
		}
		return true;
	}

	public static boolean canPickItem(Minecraft mc, ItemStack stack) {
		if (!stack.isEmpty()) {
			Inventory inv = getInventory(mc.player);
			if (!isCreative(mc.player)) {
				int slot = getSlotWithStack(inv, stack);
				if (slot == -1) {
					return false;
				}
				if (EASY_PLACE_MODE_HOTBAR_ONLY.getBooleanValue()) {
					return slot < 9;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * New doSchematicWorldPickBlock that allows you to choose which block you want
	 */
	@Environment(EnvType.CLIENT)
	synchronized public static boolean doSchematicWorldPickBlock(Minecraft mc, BlockState preference,
	                                                             BlockPos pos) {
		Level world = SchematicWorldHandler.getSchematicWorld();
		ItemStack stack = getStackForState(mc, preference, world, pos);
		if (!FakeAccurateBlockPlacement.canHandleOther(stack.getItem())) {
			MessageHolder.sendOrderMessage("Cannot handle block " + stack.getHoverName() + ", handling other");
			return false;
		}
		if (!stack.isEmpty()) {
			if (USE_INVENTORY_CACHE.getBooleanValue()) {
				boolean swapResult = io.github.eatmyvenom.litematicin.utils.InventoryUtils.swapToItem(mc, stack);
				MessageHolder.sendDebugMessage(mc.player, "Swapped to " + stack.getHoverName() + " at " + pos.toShortString() + " with result " + swapResult);
				return swapResult;
			} else {
				fi.dy.masa.litematica.util.InventoryUtils.schematicWorldPickBlock(stack, pos, world, mc);
				//#if MC>11650
				return io.github.eatmyvenom.litematicin.utils.InventoryUtils.areItemsExact(mc.player.getMainHandItem(), stack);
				//#else
				//$$ return mc.player.getMainHandStack().isItemEqual(stack);
				//#endif
			}
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	synchronized public static boolean doSchematicWorldPickBlock(Minecraft mc, ItemStack stack) {
		if (stack.isEmpty() || stack.is(Items.AIR)) {
			return false;
		}
		if (!FakeAccurateBlockPlacement.canHandleOther(stack.getItem())) {
			MessageHolder.sendOrderMessage("Cannot handle block " + stack.getHoverName() + ", handling other");
			return false;
		}
		if (USE_INVENTORY_CACHE.getBooleanValue()) {
			return io.github.eatmyvenom.litematicin.utils.InventoryUtils.swapToItem(mc, stack);
		} else {
			fi.dy.masa.malilib.util.InventoryUtils.swapItemToMainHand(stack, mc);
			//#if MC>11650
			return io.github.eatmyvenom.litematicin.utils.InventoryUtils.areItemsExact(mc.player.getMainHandItem(), stack);
			//#else
			//$$ return mc.player.getMainHandStack().isItemEqual(stack);
			//#endif
		}
	}

	@Environment(EnvType.CLIENT)
	synchronized public static boolean doSchematicWorldPickBlock(Minecraft mc, Predicate<ItemStack> stack) {
		ItemStack stack1 = io.github.eatmyvenom.litematicin.utils.InventoryUtils.findItem(mc, stack);
		return doSchematicWorldPickBlock(mc, stack1);
	}

	private static boolean interactBlockConsumed(Minecraft mc, BlockHitResult hitResult) {
		boolean consumed = interactBlock(mc, hitResult).consumesAction();
		if (consumed) {
			updateLastPlacedAfterInteraction();
		}
		return consumed;
	}

	private static boolean tryFixChiseledBookshelf(Minecraft mc, Level schematicWorld, Level clientWorld,
	                                               BlockPos pos, BlockState stateSchematic, BlockState stateClient,
	                                               boolean creative) {
		if (!(stateSchematic.getBlock() instanceof ChiseledBookShelfBlock)
			|| !(stateClient.getBlock() instanceof ChiseledBookShelfBlock)
			|| !sameBlockState(stateSchematic, stateClient)) {
			return false;
		}
		BlockEntity clientEntity = clientWorld.getBlockEntity(pos);
		if (!(clientEntity instanceof ChiseledBookShelfBlockEntity)) {
			return false;
		}
		ChiseledBookShelfBlockEntity clientShelf = (ChiseledBookShelfBlockEntity) clientEntity;
		BlockEntity schematicEntity = schematicWorld.getBlockEntity(pos);
		Container schematicContainer = schematicEntity instanceof Container ? (Container) schematicEntity : null;
		for (int slot = 0; slot < 6; slot++) {
			ItemStack wantedStack = getContainerItemOrEmpty(schematicContainer, slot);
			ItemStack currentStack = clientShelf.getItem(slot);
			if (ItemStack.matches(currentStack, wantedStack)) {
				continue;
			}
			BlockHitResult hitResult = new BlockHitResult(getSelectableSlotHitVec(pos,
				stateClient.getValue(ChiseledBookShelfBlock.FACING), 2, 3, slot),
				stateClient.getValue(ChiseledBookShelfBlock.FACING), pos, false);
			if (!currentStack.isEmpty()) {
				if (!selectEmptyHotbarSlot(mc)) {
					recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
						+ " requires an empty hotbar slot to remove a book", pos);
					MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
					return false;
				}
				if (interactBlockConsumed(mc, hitResult)) {
					clearCache();
					cacheEasyPlacePosition(pos, true, 400);
					sleepWhenRequired(mc);
					updateLastPlacedAfterInteraction(200L);
					return true;
				}
				return false;
			}
			if (!wantedStack.isEmpty()) {
				if (!wantedStack.is(ItemTags.BOOKSHELF_BOOKS) || !doSchematicWorldPickBlock(mc, wantedStack)) {
					recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
						+ " requires " + wantedStack.getHoverName().getString(), pos);
					MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
					return false;
				}
				if (interactBlockConsumed(mc, hitResult)) {
					io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(creative);
					cacheEasyPlacePosition(pos, true, 400);
					sleepWhenRequired(mc);
					updateLastPlacedAfterInteraction(200L);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	private static boolean isPrinterStateSatisfied(Level schematicWorld, Level clientWorld, BlockPos pos,
	                                               BlockState stateSchematic, BlockState stateClient) {
		return sameBlockState(stateSchematic, stateClient)
			&& !hasChiseledBookshelfContentsMismatch(schematicWorld, clientWorld, pos, stateSchematic, stateClient)
			&& !hasShelfContentsMismatch(schematicWorld, clientWorld, pos, stateSchematic, stateClient);
	}

	private static boolean hasChiseledBookshelfContentsMismatch(Level schematicWorld, Level clientWorld, BlockPos pos,
	                                                           BlockState stateSchematic, BlockState stateClient) {
		if (!(stateSchematic.getBlock() instanceof ChiseledBookShelfBlock)
			|| !(stateClient.getBlock() instanceof ChiseledBookShelfBlock)
			|| !sameBlockState(stateSchematic, stateClient)) {
			return false;
		}
		BlockEntity schematicEntity = schematicWorld.getBlockEntity(pos);
		Container schematicContainer = schematicEntity instanceof Container ? (Container) schematicEntity : null;
		BlockEntity clientEntity = clientWorld.getBlockEntity(pos);
		if (!(clientEntity instanceof ChiseledBookShelfBlockEntity)) {
			return schematicContainer != null && !schematicContainer.isEmpty();
		}
		ChiseledBookShelfBlockEntity clientShelf = (ChiseledBookShelfBlockEntity) clientEntity;
		for (int slot = 0; slot < 6; slot++) {
			ItemStack wantedStack = getContainerItemOrEmpty(schematicContainer, slot);
			if (!ItemStack.matches(clientShelf.getItem(slot), wantedStack)) {
				return true;
			}
		}
		return false;
	}

	private static void recordChiseledBookshelfMismatchIfNeeded(BlockPos pos, BlockState stateSchematic) {
		if (!causeMap.containsKey(pos.asLong())) {
			recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
				+ " requires bookshelf contents update", pos);
		}
	}

	private static boolean tryFixShelf(Minecraft mc, Level schematicWorld, Level clientWorld,
	                                   BlockPos pos, BlockState stateSchematic, BlockState stateClient,
	                                   boolean creative) {
		if (!(stateSchematic.getBlock() instanceof ShelfBlock)
			|| !(stateClient.getBlock() instanceof ShelfBlock)
			|| !sameBlockState(stateSchematic, stateClient)) {
			return false;
		}
		if (stateClient.getValue(ShelfBlock.POWERED)) {
			if (hasShelfContentsMismatch(schematicWorld, clientWorld, pos, stateSchematic, stateClient)) {
				recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
					+ " requires an unpowered shelf for safe slot update", pos);
				MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
			}
			return false;
		}
		BlockEntity clientEntity = clientWorld.getBlockEntity(pos);
		if (!(clientEntity instanceof ShelfBlockEntity)) {
			return false;
		}
		ShelfBlockEntity clientShelf = (ShelfBlockEntity) clientEntity;
		BlockEntity schematicEntity = schematicWorld.getBlockEntity(pos);
		Container schematicContainer = schematicEntity instanceof Container ? (Container) schematicEntity : null;
		for (int slot = 0; slot < clientShelf.getContainerSize(); slot++) {
			ItemStack wantedStack = getContainerItemOrEmpty(schematicContainer, slot);
			ItemStack currentStack = clientShelf.getItem(slot);
			if (ItemStack.matches(currentStack, wantedStack)) {
				continue;
			}
			BlockHitResult hitResult = new BlockHitResult(getSelectableSlotHitVec(pos,
				stateClient.getValue(ShelfBlock.FACING), 1, clientShelf.getContainerSize(), slot),
				stateClient.getValue(ShelfBlock.FACING), pos, false);
			if (!currentStack.isEmpty() && !wantedStack.isEmpty()) {
				if (!selectShelfInsertStack(mc, wantedStack, creative)) {
					recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
						+ " requires exact shelf stack " + wantedStack.getHoverName().getString()
						+ " x" + wantedStack.getCount(), pos);
					MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
					return false;
				}
				return interactShelfSlot(mc, hitResult, false);
			}
			if (!currentStack.isEmpty()) {
				if (!selectEmptyHotbarSlot(mc)) {
					recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
						+ " requires an empty hotbar slot to remove shelf item", pos);
					MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
					return false;
				}
				return interactShelfSlot(mc, hitResult, true);
			}
			if (!wantedStack.isEmpty()) {
				if (!selectShelfInsertStack(mc, wantedStack, creative)) {
					recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
						+ " requires exact shelf stack " + wantedStack.getHoverName().getString()
						+ " x" + wantedStack.getCount(), pos);
					MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
					return false;
				}
				return interactShelfSlot(mc, hitResult, false);
			}
		}
		return false;
	}

	private static boolean interactShelfSlot(Minecraft mc, BlockHitResult hitResult, boolean acceptPass) {
		InteractionResult result = interactBlock(mc, hitResult);
		if (result.consumesAction() || acceptPass && !result.equals(InteractionResult.FAIL)) {
			clearCache();
			cacheEasyPlacePosition(hitResult.getBlockPos(), true, 400);
			sleepWhenRequired(mc);
			updateLastPlacedAfterInteraction(200L);
			return true;
		}
		return false;
	}

	private static boolean selectShelfInsertStack(Minecraft mc, ItemStack wantedStack, boolean creative) {
		if (creative) {
			return doSchematicWorldPickBlock(mc, wantedStack)
				&& ItemStack.matches(mc.player.getMainHandItem(), wantedStack);
		}
		return selectExactHotbarStack(mc, wantedStack);
	}

	private static boolean selectExactHotbarStack(Minecraft mc, ItemStack wantedStack) {
		if (!canSelectHotbarSlot(mc)) {
			return false;
		}
		Inventory inventory = getInventory(mc.player);
		if (ItemStack.matches(inventory.getSelectedItem(), wantedStack)) {
			clearCache();
			return true;
		}
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (ItemStack.matches(inventory.getItem(slot), wantedStack)) {
				inventory.setSelectedSlot(slot);
				mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
				clearCache();
				return ItemStack.matches(inventory.getSelectedItem(), wantedStack);
			}
		}
		int sourceSlot = getExactNonHotbarInventorySlot(inventory, wantedStack);
		int targetHotbarSlot = getAvailableHotbarSwapTarget(inventory);
		if (sourceSlot != -1 && targetHotbarSlot != -1 && canSwapWithInventoryMenu(mc)) {
			mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, sourceSlot, targetHotbarSlot, ContainerInput.SWAP, mc.player);
			inventory.setSelectedSlot(targetHotbarSlot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(targetHotbarSlot));
			clearCache();
			return ItemStack.matches(inventory.getSelectedItem(), wantedStack);
		}
		return false;
	}

	private static boolean canSelectHotbarSlot(Minecraft mc) {
		return mc != null && mc.player != null && mc.getConnection() != null;
	}

	private static boolean canSwapWithInventoryMenu(Minecraft mc) {
		LocalPlayer player = mc == null ? null : mc.player;
		return player != null
			&& mc.gameMode != null
			&& mc.getConnection() != null
			&& player.inventoryMenu != null
			&& player.containerMenu == player.inventoryMenu;
	}

	private static ItemStack getContainerItemOrEmpty(Container container, int slot) {
		return container != null && slot >= 0 && slot < container.getContainerSize()
			? container.getItem(slot) : ItemStack.EMPTY;
	}

	private static int getExactNonHotbarInventorySlot(Inventory inventory, ItemStack wantedStack) {
		for (int slot = Inventory.getSelectionSize(); slot < inventory.getNonEquipmentItems().size(); slot++) {
			if (ItemStack.matches(inventory.getItem(slot), wantedStack)) {
				return slot;
			}
		}
		return -1;
	}

	private static int getNonAxeNonHotbarInventorySlot(Inventory inventory) {
		for (int slot = Inventory.getSelectionSize(); slot < inventory.getNonEquipmentItems().size(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && !stack.is(ItemTags.AXES)) {
				return slot;
			}
		}
		return -1;
	}

	private static int getAvailableHotbarSwapTarget(Inventory inventory) {
		int emptySlot = getEmptyHotbarSlot(inventory);
		return emptySlot != -1 ? emptySlot : inventory.getSelectedSlot();
	}

	private static int getEmptyHotbarSlot(Inventory inventory) {
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				return slot;
			}
		}
		return -1;
	}

	private static int getEmptyNonHotbarInventorySlot(Inventory inventory) {
		for (int slot = Inventory.getSelectionSize(); slot < inventory.getNonEquipmentItems().size(); slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				return slot;
			}
		}
		return -1;
	}

	private static boolean hasShelfContentsMismatch(Level schematicWorld, Level clientWorld, BlockPos pos,
	                                               BlockState stateSchematic, BlockState stateClient) {
		if (!(stateSchematic.getBlock() instanceof ShelfBlock)
			|| !(stateClient.getBlock() instanceof ShelfBlock)
			|| !sameBlockState(stateSchematic, stateClient)) {
			return false;
		}
		BlockEntity schematicEntity = schematicWorld.getBlockEntity(pos);
		Container schematicContainer = schematicEntity instanceof Container ? (Container) schematicEntity : null;
		BlockEntity clientEntity = clientWorld.getBlockEntity(pos);
		if (!(clientEntity instanceof ShelfBlockEntity)) {
			return schematicContainer != null && !schematicContainer.isEmpty();
		}
		ShelfBlockEntity clientShelf = (ShelfBlockEntity) clientEntity;
		for (int slot = 0; slot < clientShelf.getContainerSize(); slot++) {
			ItemStack wantedStack = getContainerItemOrEmpty(schematicContainer, slot);
			if (!ItemStack.matches(clientShelf.getItem(slot), wantedStack)) {
				return true;
			}
		}
		return false;
	}

	private static void recordShelfMismatchIfNeeded(BlockPos pos, BlockState stateSchematic) {
		if (!causeMap.containsKey(pos.asLong())) {
			recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString()
				+ " requires shelf contents update", pos);
		}
	}

	private static boolean selectEmptyHotbarSlot(Minecraft mc) {
		if (!canSelectHotbarSlot(mc)) {
			return false;
		}
		Inventory inventory = getInventory(mc.player);
		if (inventory.getSelectedItem().isEmpty()) {
			clearCache();
			return true;
		}
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.getItem(slot).isEmpty()) {
				inventory.setSelectedSlot(slot);
				mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
				clearCache();
				return inventory.getSelectedItem().isEmpty();
			}
		}
		int emptyInventorySlot = getEmptyNonHotbarInventorySlot(inventory);
		if (emptyInventorySlot != -1 && canSwapWithInventoryMenu(mc)) {
			int selectedSlot = inventory.getSelectedSlot();
			mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId, emptyInventorySlot, selectedSlot, ContainerInput.SWAP, mc.player);
			inventory.setSelectedSlot(selectedSlot);
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(selectedSlot));
			clearCache();
			return inventory.getSelectedItem().isEmpty();
		}
		return false;
	}

	private static Vec3 getSelectableSlotHitVec(BlockPos pos, Direction face, int rows, int columns, int slot) {
		int row = slot / columns;
		int column = slot % columns;
		double localX = (column + 0.5D) / columns;
		double localY = 1.0D - (row + 0.5D) / rows;
		return switch (face) {
			case NORTH -> new Vec3(pos.getX() + 1.0D - localX, pos.getY() + localY, pos.getZ());
			case SOUTH -> new Vec3(pos.getX() + localX, pos.getY() + localY, pos.getZ() + 1.0D);
			case WEST -> new Vec3(pos.getX(), pos.getY() + localY, pos.getZ() + localX);
			case EAST -> new Vec3(pos.getX() + 1.0D, pos.getY() + localY, pos.getZ() + 1.0D - localX);
			default -> Vec3.atCenterOf(pos);
		};
	}

	private static boolean hasLightableStateMismatch(BlockState clientState, BlockState schematicState) {
		Block block = clientState.getBlock();
		if (!(block instanceof CampfireBlock || block instanceof CandleBlock || block instanceof CandleCakeBlock)) {
			return false;
		}
		return clientState.hasProperty(BlockStateProperties.LIT)
			&& schematicState.hasProperty(BlockStateProperties.LIT)
			&& clientState.getValue(BlockStateProperties.LIT) != schematicState.getValue(BlockStateProperties.LIT);
	}

	private static boolean canLightCurrentState(BlockState clientState) {
		return CampfireBlock.canLight(clientState) || CandleBlock.canLight(clientState) || CandleCakeBlock.canLight(clientState);
	}

	private static boolean canExtinguishWithEmptyHand(BlockState clientState) {
		return clientState.hasProperty(BlockStateProperties.LIT)
			&& clientState.getValue(BlockStateProperties.LIT)
			&& (clientState.getBlock() instanceof CandleBlock || clientState.getBlock() instanceof CandleCakeBlock);
	}

	private static ItemStack getLightStack(Minecraft mc) {
		ItemStack flintAndSteelStack = Items.FLINT_AND_STEEL.getDefaultInstance();
		if (isCreative(mc.player) || getSlotWithStack(mc.player, flintAndSteelStack) != -1) {
			return flintAndSteelStack;
		}
		ItemStack fireChargeStack = Items.FIRE_CHARGE.getDefaultInstance();
		return getSlotWithStack(mc.player, fireChargeStack) == -1 ? ItemStack.EMPTY : fireChargeStack;
	}

	private static boolean pickShovel(Minecraft mc) {
		if (isCreative(mc.player)) {
			return doSchematicWorldPickBlock(mc, Items.IRON_SHOVEL.getDefaultInstance());
		}
		return doSchematicWorldPickBlock(mc, stack -> stack.getItem() instanceof ShovelItem);
	}

	private static boolean pickHoe(Minecraft mc) {
		if (isCreative(mc.player)) {
			return doSchematicWorldPickBlock(mc, Items.IRON_HOE.getDefaultInstance());
		}
		return doSchematicWorldPickBlock(mc, stack -> stack.getItem() instanceof HoeItem);
	}

	private static ItemStack getCandleStackForCandleCake(Block block) {
		if (block == Blocks.CANDLE_CAKE) return Items.CANDLE.getDefaultInstance();
		int dyedIndex = Blocks.DYED_CANDLE_CAKE.asList().indexOf(block);
		if (dyedIndex >= 0) {
			return Items.DYED_CANDLE.asList().get(dyedIndex).getDefaultInstance();
		}
		return ItemStack.EMPTY;
	}

	private static boolean tryPlaceCandleOnCake(Minecraft mc, BlockState schematicState, BlockState clientState, BlockPos pos, boolean creative) {
		if (!(schematicState.getBlock() instanceof CandleCakeBlock) || !(clientState.getBlock() instanceof CakeBlock)) {
			return false;
		}
		if (clientState.getValue(CakeBlock.BITES) != 0) {
			return false;
		}
		ItemStack candleStack = getCandleStackForCandleCake(schematicState.getBlock());
		if (candleStack.isEmpty() || !doSchematicWorldPickBlock(mc, candleStack)) {
			return false;
		}
		BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		if (interactBlockConsumed(mc, hitResult)) {
			io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(creative);
			cacheEasyPlacePosition(pos, true, 400);
			sleepWhenRequired(mc);
			updateLastPlacedAfterInteraction(200L);
			return true;
		}
		return false;
	}

	private static boolean tryApplyLightableState(Minecraft mc, BlockState clientState, BlockState schematicState, BlockPos pos, boolean creative) {
		if (!hasLightableStateMismatch(clientState, schematicState)) {
			return false;
		}
		boolean shouldLight = schematicState.getValue(BlockStateProperties.LIT);
		if (shouldLight) {
			if (!canLightCurrentState(clientState)) {
				return false;
			}
			ItemStack lightStack = getLightStack(mc);
			if (lightStack.isEmpty() || !doSchematicWorldPickBlock(mc, lightStack)) {
				return false;
			}
			BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
			if (interactBlockConsumed(mc, hitResult)) {
				io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(creative);
				cacheEasyPlacePosition(pos, true, 400);
				sleepWhenRequired(mc);
				updateLastPlacedAfterInteraction(200L);
				return true;
			}
			return false;
		}
		if (clientState.getBlock() instanceof CampfireBlock) {
			if (!pickShovel(mc)) {
				return false;
			}
			BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
			if (interactBlockConsumed(mc, hitResult)) {
				cacheEasyPlacePosition(pos, true, 400);
				sleepWhenRequired(mc);
				updateLastPlacedAfterInteraction(200L);
				return true;
			}
			return false;
		}
		if (!canExtinguishWithEmptyHand(clientState) || !mc.player.getMainHandItem().isEmpty()) {
			return false;
		}
		Vec3 hitPos = clientState.getBlock() instanceof CandleCakeBlock
			? new Vec3(pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5)
			: Vec3.atCenterOf(pos);
		BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
		if (interactBlockConsumed(mc, hitResult)) {
			cacheEasyPlacePosition(pos, true, 400);
			updateLastPlacedAfterInteraction(200L);
			return true;
		}
		return false;
	}

	private static int runBedrockBreakerInteractions(Minecraft mc, @Nullable BlockPos pos, int maxInteract) {
		int interactions = BedrockBreaker.scheduledTickHandler(mc, pos, maxInteract);
		if (interactions > 0) {
			updateLastPlacedAfterInteraction();
		}
		return interactions;
	}

	private static boolean runBedrockBreaker(Minecraft mc, @Nullable BlockPos pos, int maxInteract) {
		return runBedrockBreakerInteractions(mc, pos, maxInteract) > 0;
	}

	private static InteractionResult queuedFakePlacementSuccess(int interactions) {
		if (interactions > 0) {
			updateLastPlacedAfterInteraction();
		}
		return InteractionResult.SUCCESS;
	}

	private static void cacheSmartRedstoneAfterPlacement(Minecraft mc, Level world, BlockState stateSchematic, BlockPos pos) {
		if (stateSchematic.hasProperty(RedstoneTorchBlock.LIT) && !stateSchematic.getValue(RedstoneTorchBlock.LIT)) {
			cacheEasyPlacePosition(pos.above(), false, 700);
		}
		Set<BlockPos> shouldCache = ObserverCantAvoidPos(mc, world, pos);
		if (!shouldCache.isEmpty()) {
			shouldCache.forEach(a -> {
				MessageHolder.sendDebugMessage("Caching position " + a.toShortString() + " because observer can't avoid ");
				cacheEasyPlacePosition(a, true, (int) Math.ceil(Math.sqrt(a.distSqr(pos)) * 100));
			});
		}
	}

	public static InteractionResult doEasyPlaceFakeRotation(Minecraft mc) { //force normal easyplace action, ignore condition checks
		if (FakeAccurateBlockPlacement.isHandling()){
			MessageHolder.sendDebugMessage(mc.player, "Passed because already handling something");
			return InteractionResult.PASS;
		}
		RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(mc.level, mc.player, 6);
		FakeAccurateBlockPlacement.requestedTicks = Math.max(-2, FakeAccurateBlockPlacement.requestedTicks);
		if (traceWrapper == null) {
			return InteractionResult.PASS;
		}
		BlockHitResult trace = traceWrapper.getBlockHitResult();
		if (trace == null) {
			return InteractionResult.PASS;
		}
		Level world = SchematicWorldHandler.getSchematicWorld();
		Level clientWorld = mc.level;
		BlockPos blockPos = trace.getBlockPos();
		if (isPositionCached(blockPos, false)){
			MessageHolder.sendDebugMessage(mc.player, "Passed because position "+ blockPos.toShortString() + " is cached");
			return InteractionResult.PASS;
		}
		BlockState schematicState = world.getBlockState(blockPos);
		BlockState clientState = clientWorld.getBlockState(blockPos);
		if (tryFixChiseledBookshelf(mc, world, clientWorld, blockPos, schematicState, clientState, isCreative(mc.player))) {
			return InteractionResult.SUCCESS;
		}
		if (tryFixShelf(mc, world, clientWorld, blockPos, schematicState, clientState, isCreative(mc.player))) {
			return InteractionResult.SUCCESS;
		}
		if (hasChiseledBookshelfContentsMismatch(world, clientWorld, blockPos, schematicState, clientState)) {
			recordChiseledBookshelfMismatchIfNeeded(blockPos, schematicState);
			MessageHolder.sendUniqueMessage(mc.player, getReason(blockPos.asLong()));
			return InteractionResult.FAIL;
		}
		if (hasShelfContentsMismatch(world, clientWorld, blockPos, schematicState, clientState)) {
			recordShelfMismatchIfNeeded(blockPos, schematicState);
			MessageHolder.sendUniqueMessage(mc.player, getReason(blockPos.asLong()));
			return InteractionResult.FAIL;
		}
		if (isPrinterStateSatisfied(world, clientWorld, blockPos, schematicState, clientState) || schematicState.isAir()) {
			MessageHolder.sendDebugMessage(mc.player, "Passed because position "+ blockPos.toShortString() + " is satisfied");
			return InteractionResult.FAIL;
		}
		if (FakeAccurateBlockPlacement.canHandleOther(schematicState.getBlock().asItem()) && canPickBlock(mc, schematicState, blockPos)) {
			MessageHolder.sendOrderMessage("Requested " + schematicState + " at " +blockPos.toShortString());
			return requestFakePlacementAccepted(schematicState, blockPos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
		MessageHolder.sendDebugMessage(mc.player, "Passed because position "+ blockPos.toShortString() + " cannot pick block or cannot handle other, handling "+ FakeAccurateBlockPlacement.currentHandling);
		return InteractionResult.FAIL;
	}
	public static InteractionResult doEasyPlaceNormally(Minecraft mc) { //force normal easyplace action, ignore condition checks
		RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(mc.level, mc.player, 6);
		if (traceWrapper == null) {
			return InteractionResult.PASS;
		}
		BlockHitResult trace = traceWrapper.getBlockHitResult();
		if (trace == null) {
			return InteractionResult.PASS;
		}
		Level world = SchematicWorldHandler.getSchematicWorld();
		Level clientWorld = mc.level;
		BlockPos blockPos = trace.getBlockPos();
		BlockState schematicState = world.getBlockState(blockPos);
		BlockState clientState = clientWorld.getBlockState(blockPos);
		if (tryFixChiseledBookshelf(mc, world, clientWorld, blockPos, schematicState, clientState, isCreative(mc.player))) {
			return InteractionResult.SUCCESS;
		}
		if (tryFixShelf(mc, world, clientWorld, blockPos, schematicState, clientState, isCreative(mc.player))) {
			return InteractionResult.SUCCESS;
		}
		if (hasChiseledBookshelfContentsMismatch(world, clientWorld, blockPos, schematicState, clientState)) {
			recordChiseledBookshelfMismatchIfNeeded(blockPos, schematicState);
			MessageHolder.sendUniqueMessage(mc.player, getReason(blockPos.asLong()));
			return InteractionResult.FAIL;
		}
		if (hasShelfContentsMismatch(world, clientWorld, blockPos, schematicState, clientState)) {
			recordShelfMismatchIfNeeded(blockPos, schematicState);
			MessageHolder.sendUniqueMessage(mc.player, getReason(blockPos.asLong()));
			return InteractionResult.FAIL;
		}
		if (isPrinterStateSatisfied(world, clientWorld, blockPos, schematicState, clientState) || schematicState.isAir()) {
			return InteractionResult.FAIL;
		}
		if (isAutoPlacedUpperHalf(schematicState)) {
			return InteractionResult.FAIL;
		}
		if (!schematicState.canSurvive(clientWorld, blockPos)
			&& !canPlaceBigDripleafStemAsLeaf(clientWorld, blockPos, schematicState, mc.player.getDirection())) {
			return InteractionResult.FAIL;
		}
		ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(schematicState);
		if (!stack.isEmpty()) {
			fi.dy.masa.litematica.util.InventoryUtils.schematicWorldPickBlock(stack, blockPos, world, mc);
			InteractionHand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
			if (hand == null) {
				return InteractionResult.FAIL;
			}
			if (PRINTER_FAKE_ROTATION.getBooleanValue() && !PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
				return requestFakePlacementAccepted(schematicState, blockPos) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
			}
			if (hasYawRotation16Placement(schematicState) && !canPlaceYawRotation16(schematicState, getYaw(mc.player))) {
				return InteractionResult.FAIL;
			}
			if (!canPlaceSegmentedHorizontalBlock(clientWorld, blockPos, schematicState, mc.player.getDirection(), mc.player.isSecondaryUseActive())) {
				return InteractionResult.FAIL;
			}
			if (hasDripleafPlacementFacing(schematicState) && !canPlaceDripleaf(clientWorld, blockPos, schematicState, mc.player.getDirection())) {
				return InteractionResult.FAIL;
			}
			if (hasSpeleothemPlacement(schematicState)
				&& !canPlaceSpeleothem(clientWorld, blockPos, schematicState, getNearestLookingVerticalDirection(mc.player), mc.player.isSecondaryUseActive())) {
				return InteractionResult.FAIL;
			}
			if (!canPlaceMossyCarpet(clientWorld, blockPos, schematicState)) {
				return InteractionResult.FAIL;
			}
			if (!canPlaceLantern(clientWorld, blockPos, schematicState)) {
				return InteractionResult.FAIL;
			}
			Vec3 hitPos;
			Direction sideOrig = trace.getDirection();
			Direction side = applyPlacementFacing(schematicState, sideOrig, clientState);
			BlockPos clickPos = blockPos;
			if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
				hitPos = applyCarpetProtocolHitVec(blockPos, schematicState);
			} else {
				if (schematicState.getBlock() instanceof WallHangingSignBlock) {
					Direction wallHangingSide = getWallHangingSignPlacementSide(clientWorld, blockPos, schematicState);
					if (!canPlaceWallHangingSignFromPlayer(schematicState, Direction.orderedByNearest(mc.player), wallHangingSide)) {
						return InteractionResult.FAIL;
					}
				}
				if (schematicState.getBlock() instanceof CeilingHangingSignBlock
					&& !canPlaceCeilingHangingSign(clientWorld, blockPos, schematicState, getYaw(mc.player), mc.player.isSecondaryUseActive())) {
					return InteractionResult.FAIL;
				}
				Direction[] facingSides = Direction.orderedByNearest(mc.player);
				if (!canPlaceMultifaceOrVine(clientWorld, blockPos, schematicState, facingSides)) {
					return InteractionResult.FAIL;
				}
				PlacementClick click = createVanillaPlacementClick(clientWorld, blockPos, schematicState, clientState, sideOrig, mc.player.isSecondaryUseActive(), facingSides);
				if (click == null) {
					return InteractionResult.FAIL;
				}
				clickPos = click.blockPos();
				side = click.side();
				hitPos = click.hitVec();
			}
			BlockHitResult hitResult = new BlockHitResult(hitPos, side, clickPos, false);
			if (!canPlaceSourceDerivedState(mc.player, schematicState, hitResult)) {
				return InteractionResult.FAIL;
			}
			boolean canContinue = interactBlockConsumed(mc, hitResult); //PLACE block
			if (canContinue) {
				io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative(mc.player));
				cacheEasyPlacePosition(blockPos, false);
				sleepWhenRequired(mc);
				updateLastPlacedAfterInteraction();
			}
			if (canContinue) {
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.FAIL;
	}

	private static boolean requestFakePlacementAccepted(BlockState state, BlockPos pos) {
		return requestFakePlacement(state, pos) != FakePlacementResult.NONE;
	}

	private static FakePlacementResult requestFakePlacement(BlockState state, BlockPos pos) {
		if (FakeAccurateBlockPlacement.request(state, pos)) {
			return FakePlacementResult.PLACED;
		}
		if (FakeAccurateBlockPlacement.isWaitingFor(pos, state)) {
			return FakePlacementResult.QUEUED;
		}
		return FakePlacementResult.NONE;
	}

	private static void recordCause(BlockPos pos, String reason, BlockPos reasonPos) {
		if (reasonPos != null) {
			if (pos.asLong() == reasonPos.asLong()) {
				causeMap.put(pos.asLong(), "self registered+\n");
				//throw new AssertionError("Position should not equal to reason position!");
			}
			referenceMap.put(pos.asLong(), reasonPos.asLong());
		}
		causeMap.put(pos.asLong(), reason + '\n');
	}

	private static boolean containsPositionAsReason(BlockPos resultPos, BlockPos pos) {
		// recursively check if the position is a reason for the result position
		return containsPositionsAsReasonInternal(resultPos, pos, null);
	}

	private static boolean containsPositionsAsReasonInternal(BlockPos resultPos, BlockPos pos, Set<Long> recursiveSet) {
		if (recursiveSet == null) {
			recursiveSet = new HashSet<>();
		}
		if (recursiveSet.contains(resultPos.asLong())) {
			return false;
		}
		recursiveSet.add(resultPos.asLong());
		if (resultPos.asLong() == pos.asLong()) {
			return true;
		}
		if (referenceMap.containsKey(resultPos.asLong())) {
			return containsPositionsAsReasonInternal(BlockPos.of(referenceMap.get(resultPos.asLong())), pos, recursiveSet);
		}
		return false;
	}



	private static String getReason(Long pos) {
		return "<" + internalGetReason(pos, null, 0) + ">";
	}

	private static String internalGetReason(Long pos, LongOpenHashSet set, int count) {
		if (count > 10) {
			return BlockPos.of(pos).toShortString() + "RECURSIVE_COUNT_EXCEED";
		}
		if (set == null) {
			set = new LongOpenHashSet();
		}
		if (set.contains((long) pos)) {
			return BlockPos.of(pos).toShortString() + "Recursive ";
		}
		if (referenceMap.containsKey((long) pos)) {
			set.add((long) pos);
			return causeMap.getOrDefault(pos, BlockPos.of(pos).toShortString() + " : Not registered") + " " + internalGetReason(referenceMap.get((long) pos), set, count + 1);
		}
		return causeMap.getOrDefault(pos, BlockPos.of(pos).toShortString() + " : Not registered");
	}

	private static boolean isPositionWithinBox(Box box, BlockPos pos) {
		if (box == null) {
			return true;
		}
		BlockPos start = box.getPos1();
		BlockPos end = box.getPos2();
		BlockPos ref1 = new BlockPos(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()));
		BlockPos ref2 = new BlockPos(Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
		return (ref1.getX() <= pos.getX() && pos.getX() <= ref2.getX() && ref1.getY() <= pos.getY() && pos.getY() <= ref2.getY() && ref1.getZ() <= pos.getZ() && pos.getZ() <= ref2.getZ());
	}

	private static boolean isPositionWithinBox(BlockPos pos) {
		if (Printer.CURRENT_BOX == null) {
			return true;
		}
		return isPositionWithinBox(Printer.CURRENT_BOX, pos);
	}

	@Environment(EnvType.CLIENT)
	synchronized public static InteractionResult doPrinterAction(Minecraft mc) {
		io.github.eatmyvenom.litematicin.utils.InventoryUtils.itemChangeCount = 0;
		if (!DEBUG_MESSAGE.getBooleanValue()) {
			causeMap.clear(); //reduce ram usage
		}
		boolean bedrockBreaking = PRINTER_BEDROCK_BREAKING.getBooleanValue();
		if (!bedrockBreaking) {
			BedrockBreaker.clear();
		}
		int maxInteract = PRINTER_MAX_BLOCKS.getIntegerValue();
		FakeAccurateBlockPlacement.requestedTicks = Math.max(-2, FakeAccurateBlockPlacement.requestedTicks);
		if (breaker.isBreakingBlock()) {
			MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling breakBlock!"), true);
			return InteractionResult.SUCCESS;
		}
		if (PRINTER_ALLOW_INVENTORY_OPERATIONS.getBooleanValue()) {
			ItemInputs.execute(mc);
			MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling inventory operation!"), true);
			return InteractionResult.PASS;
		} else {
			ItemInputs.clear();
		}
		if (currentTimeMillis() < lastPlaced + 1000.0 * EASY_PLACE_MODE_DELAY.getDoubleValue()) {
			MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling delay"), true);
			return InteractionResult.PASS;
		} else {
			isSleeping = false;
		}
		LocalPlayer player = mc.player;
		Level clientWorld = mc.level;
		boolean isCreative = isCreative(player);
		BlockPos tracePos = player.blockPosition();
		int posX = tracePos.getX();
		int posY = tracePos.getY();
		int posZ = tracePos.getZ();

		RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(clientWorld, player, 6);
		//RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(mc.world, mc.player, 6, true); previous litematica code
		if (traceWrapper != null) {
			BlockHitResult trace = traceWrapper.getBlockHitResult();
			tracePos = trace.getBlockPos();
			posX = tracePos.getX();
			posY = tracePos.getY();
			posZ = tracePos.getZ();
		}

		boolean ClearArea = PRINTER_CLEAR_FLUIDS.getBooleanValue(); // if it's true, will ignore everything and remove fluids.
		boolean UseCobble = PRINTER_CLEAR_FLUIDS_USE_COBBLESTONE.getBooleanValue() && ClearArea;
		boolean ClearSnow = PRINTER_CLEAR_SNOW_LAYER.getBooleanValue() && ClearArea;
		boolean CanUseProtocol = PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
		boolean FillInventory = PRINTER_PUMPKIN_PIE_FOR_COMPOSTER.getBooleanValue();
		ItemStack composableItem = Items.PUMPKIN_PIE.getDefaultInstance();
		//#if MC>=11902
		List<PlacementPart> allPlacementsTouchingSubChunk = DataManager.getSchematicPlacementManager().getAllPlacementsTouchingChunk(tracePos);
		//#else
		//$$ List<PlacementPart> allPlacementsTouchingSubChunk = DataManager.getSchematicPlacementManager().getAllPlacementsTouchingSubChunk(new SubChunkPos(tracePos));
		//#endif
		Box selectedBox = null;
		Printer.CURRENT_BOX = null;
		if (allPlacementsTouchingSubChunk.isEmpty() && !ClearArea) {
			if (bedrockBreaking && runBedrockBreaker(mc, null, maxInteract)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;
		int minX = 0;
		int minY = 0;
		int minZ = 0;
		int rangeX = EASY_PLACE_MODE_RANGE_X.getIntegerValue();
		int rangeY = EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
		int rangeZ = EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
		if (rangeX == 0 && rangeY == 0 && rangeZ == 0 && traceWrapper != null) {
			return doEasyPlaceNormally(mc);
		}
		boolean foundBox = false;
		if (ClearArea) {
			foundBox = true;
			maxX = posX + rangeX;
			maxY = posY + rangeY;
			maxZ = posZ + rangeZ;
			minX = posX - rangeX;
			minY = posY - rangeY;
			minZ = posZ - rangeZ;
		} else {
			for (PlacementPart part : allPlacementsTouchingSubChunk) {
				IntBoundingBox pbox = part.getBox();
				if (pbox.contains(tracePos)) {

					ImmutableMap<String, Box> boxes = part.getPlacement()
						.getSubRegionBoxes(RequiredEnabled.PLACEMENT_ENABLED);

					for (Box box : boxes.values()) {

						final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX());
						final int boxYMin = Math.min(box.getPos1().getY(), box.getPos2().getY());
						final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
						final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX());
						final int boxYMax = Math.max(box.getPos1().getY(), box.getPos2().getY());
						final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ());
						if (posX < boxXMin || posX > boxXMax || posY < boxYMin || posY > boxYMax || posZ < boxZMin
							|| posZ > boxZMax) {
							continue;
						}
						minX = boxXMin;
						maxX = boxXMax;
						minY = boxYMin;
						maxY = boxYMax;
						minZ = boxZMin;
						maxZ = boxZMax;
						foundBox = true;
						selectedBox = box;
						CURRENT_BOX = box;
						break;
					}

					break;
				}
			}
		}

		if (!foundBox) {
			if (bedrockBreaking && runBedrockBreaker(mc, null, maxInteract)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}
		LayerRange range = DataManager.getRenderLayerRange(); //add range following
		int MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);
		double maxReachSquared = (double) MaxReach * MaxReach;
		boolean breakBlocks = PRINTER_BREAK_BLOCKS.getBooleanValue();
		boolean ExplicitObserver = PRINTER_OBSERVER_AVOID_ALL.getBooleanValue();
		boolean flipBlocks = player.getMainHandItem().getItem().equals(Items.CACTUS) && PRINTER_FLIPPINCACTUS.getBooleanValue(); //if true, will flip blocks with cactus
		boolean smartRedstone = PRINTER_SMART_REDSTONE_AVOID.getBooleanValue();
		Direction[] facingSides = Direction.orderedByNearest(player);
		Direction primaryFacing = facingSides[0];
		Direction horizontalFacing = primaryFacing; // For use in blocks with only horizontal rotation

		int index = 0;
		while (horizontalFacing.getAxis() == Direction.Axis.Y && index < facingSides.length) {
			horizontalFacing = facingSides[index++];
		}

		Level world = SchematicWorldHandler.getSchematicWorld();

		/*
		 * TODO: THIS IS REALLY BAD IN TERMS OF EFFICIENCY. I suggest using some form of
		 * search with a built in datastructure first Maybe quadtree? (I dont know how
		 * MC works)
		 */

		int interact = 0;

		int fromX = Math.max(posX - rangeX, minX);
		int fromY = Math.max(posY - rangeY, minY);
		int fromZ = Math.max(posZ - rangeZ, minZ);

		int toX = Math.min(posX + rangeX, maxX);
		int toY = Math.min(posY + rangeY, maxY);
		int toZ = Math.min(posZ + rangeZ, maxZ);

		int maxBuildY = worldTopY - 1;
		toY = Math.max(Math.min(toY, maxBuildY), worldBottomY);
		fromY = Math.max(Math.min(fromY, maxBuildY), worldBottomY);

		double playerX = player.getX();
		double playerY = player.getY();
		double playerZ = player.getZ();
		fromX = Math.max(fromX, (int) playerX - rangeX);
		fromY = Math.max(fromY, (int) playerY - rangeY);
		fromZ = Math.max(fromZ, (int) playerZ - rangeZ);

		toX = Math.min(toX, (int) playerX + rangeX);
		toY = Math.min(toY, (int) playerY + rangeY);
		toZ = Math.min(toZ, (int) playerZ + rangeZ);
		for (int y = fromY; y <= toY; y++) {
			for (int x = fromX; x <= toX; x++) {
				for (int z = fromZ; z <= toZ; z++) {
					if (interact >= maxInteract) {
						updateLastPlacedAfterInteraction();
						return InteractionResult.SUCCESS;
					}
					if (FakeAccurateBlockPlacement.emptyWaitingQueue()) {
						interact++;
					}
					if (FakeAccurateBlockPlacement.shouldReturnValue) {
						FakeAccurateBlockPlacement.shouldReturnValue = false;
						return InteractionResult.SUCCESS;
					}
					if (interact >= maxInteract) {
						updateLastPlacedAfterInteraction();
						return InteractionResult.SUCCESS;
					}
					double dx = playerX - x - 0.5;
					double dy = playerY - y - 0.5;
					double dz = playerZ - z - 0.5;

					if (dx * dx + dy * dy + dz * dz > maxReachSquared) {
						continue;
					}

					BlockPos pos = new BlockPos(x, y, z);
					if (!ClearArea && !range.isPositionWithinRange(pos)) {
						continue;
					}
					BlockState stateSchematic = world.getBlockState(pos);
					BlockState stateClient = clientWorld.getBlockState(pos);
					Block schematicBlock = stateSchematic.getBlock();
					Block clientBlock = stateClient.getBlock();
					updateSignText(mc, world, pos);
					if (!ClearArea && !flipBlocks && tryFixChiseledBookshelf(mc, world, clientWorld, pos, stateSchematic, stateClient, isCreative)) {
						return InteractionResult.SUCCESS;
					}
					if (!ClearArea && !flipBlocks && tryFixShelf(mc, world, clientWorld, pos, stateSchematic, stateClient, isCreative)) {
						return InteractionResult.SUCCESS;
					}
					if (!ClearArea && !flipBlocks && hasChiseledBookshelfContentsMismatch(world, clientWorld, pos, stateSchematic, stateClient)) {
						recordChiseledBookshelfMismatchIfNeeded(pos, stateSchematic);
						MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
						continue;
					}
					if (!ClearArea && !flipBlocks && hasShelfContentsMismatch(world, clientWorld, pos, stateSchematic, stateClient)) {
						recordShelfMismatchIfNeeded(pos, stateSchematic);
						MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
						continue;
					}
					if (!breakBlocks && !ClearArea && !flipBlocks && !bedrockBreaking) {
						if (stateSchematic.isAir()) {
							continue;
						} else if (isPrinterStateSatisfied(world, clientWorld, pos, stateSchematic, stateClient)) {
							causeMap.remove(pos.asLong());
							continue;
						}
					}
					if (breakBlocks) {
						if (PRINTER_BREAK_IGNORE_EXTRA.getBooleanValue() && stateSchematic.isAir()) {
							continue;
						}
					}
					if (!ClearArea) {
						if (breakBlocks && !(clientBlock instanceof SnowLayerBlock) &&
							!isBambooSaplingAwaitingStalk(world, clientWorld, pos, stateSchematic, stateClient) &&
							!stateClient.isAir() &&
							!(stateClient.is(Blocks.WATER) || stateClient.is(Blocks.LAVA) || stateClient.is(Blocks.BUBBLE_COLUMN)) &&
							!stateClient.is(Blocks.PISTON_HEAD) && !stateClient.is(Blocks.MOVING_PISTON)) {
							if (clientBlock != schematicBlock ||
								(clientBlock instanceof SlabBlock && schematicBlock instanceof SlabBlock && stateClient.getValue(SlabBlock.TYPE) != stateSchematic.getValue(SlabBlock.TYPE))
									&& dx * dx + (dy + 1.5) * (dy + 1.5) + dz * dz <= maxReachSquared) {

								if (isCreative(mc.player)) {
									if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
										interact++;
									}

									if (interact >= maxInteract) {
										updateLastPlacedAfterInteraction();
										return InteractionResult.SUCCESS;
									}
								} else if (BedrockBreaker.isBlockNotInstantBreakable(clientBlock) && bedrockBreaking) {
									MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling printerBedrockBreaking!"), true);
									interact += runBedrockBreakerInteractions(mc, pos, maxInteract - interact);
									continue;
								} else if (bedrockBreaking) {
									MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling printerBedrockBreaking!"), true);
									interact += runBedrockBreakerInteractions(mc, null, maxInteract - interact);
									continue;
								} else if (!positionStorage.hasPos(mc.level, pos)) { // For survival
									boolean replaceable = isReplaceable(stateClient);
									float destroySpeed = stateClient.getDestroySpeed(world, pos);
									if (!replaceable && destroySpeed == -1) {
										continue;
									}
									if (replaceable || destroySpeed == 0) {
										if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
											updateLastPlacedAfterInteraction();
											return InteractionResult.SUCCESS;
										}
										continue;
									}
									if (!replaceable) {
										if (breaker.startBreakingBlock(pos, mc)) {
											updateLastPlacedAfterInteraction();
											return InteractionResult.SUCCESS;
										}
									} // it needs to avoid unbreakable blocks and just added and lava, but it's not block so somehow made it work
									continue;
								}
							}
						}
						if (!flipBlocks && isBambooSaplingAwaitingStalk(world, clientWorld, pos, stateSchematic, stateClient)) {
							continue;
						}
						// Abort if there is already a block in the target position
						if (flipBlocks || requiresMoreAction(stateSchematic, stateClient)) {
							/*
							 * Sometimes, blocks have other states like the delay on a repeater. So, this
							 * code clicks the block until the state is the same I don't know if Schematica
							 * does this too, I just did it because I work with a lot of redstone
							 */
							if (!flipBlocks && !stateClient.isAir() && !mc.player.isShiftKeyDown() && !isPositionCached(pos, true)) {
								Block cBlock = stateClient.getBlock();
								Block sBlock = stateSchematic.getBlock();
								// Blocks are equal, but have different states
								if (cBlock == sBlock) {
									Direction facingSchematic = getSimplifiedFirstPropertyFacingValue(stateSchematic);
									Direction facingClient = getSimplifiedFirstPropertyFacingValue(stateClient);

									if (hasSegmentedHorizontalPlacement(stateSchematic) && hasSegmentedHorizontalPlacement(stateClient)) {
										if (breakBlocks) {
											if (breaker.startBreakingBlock(pos, mc)) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
											continue;
										}
										MessageHolder.sendUniqueMessage(mc.player, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has incompatible segment state");
										continue;
									}

									if (sBlock instanceof SeaPickleBlock) {
										int clientPickles = stateClient.getValue(SeaPickleBlock.PICKLES);
										int schematicPickles = stateSchematic.getValue(SeaPickleBlock.PICKLES);
										boolean waterloggedMismatch = stateClient.getValue(SeaPickleBlock.WATERLOGGED) != stateSchematic.getValue(SeaPickleBlock.WATERLOGGED);
										if ((clientPickles > schematicPickles || waterloggedMismatch) && breakBlocks) {
											if (isCreative(mc.player)) {
												if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
													interact++;
													updateLastPlacedAfterInteraction();
													if (interact >= maxInteract) {
														return InteractionResult.SUCCESS;
													}
													continue;
												}
											} else if (breaker.startBreakingBlock(pos, mc)) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
										}
										String reason = waterloggedMismatch
											? " requires waterlogged state " + stateSchematic.getValue(SeaPickleBlock.WATERLOGGED)
											: " has too many pickles";
										recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + reason, pos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}

									if (sBlock instanceof SnowLayerBlock) {
										int clientLayers = stateClient.getValue(SnowLayerBlock.LAYERS);
										int schematicLayers = stateSchematic.getValue(SnowLayerBlock.LAYERS);
										if (clientLayers > schematicLayers && breakBlocks) {
											if (isCreative(mc.player)) {
												if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
													interact++;
													updateLastPlacedAfterInteraction();
													if (interact >= maxInteract) {
														return InteractionResult.SUCCESS;
													}
													continue;
												}
											} else if (breaker.startBreakingBlock(pos, mc)) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
										}
										recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has too many layers", pos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}

									if (sBlock instanceof TurtleEggBlock) {
										int clientEggs = stateClient.getValue(TurtleEggBlock.EGGS);
										int schematicEggs = stateSchematic.getValue(TurtleEggBlock.EGGS);
										if (clientEggs <= schematicEggs) {
											continue;
										}
										if (breakBlocks) {
											if (isCreative(mc.player)) {
												if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
													interact++;
													updateLastPlacedAfterInteraction();
													if (interact >= maxInteract) {
														return InteractionResult.SUCCESS;
													}
													continue;
												}
											} else if (breaker.startBreakingBlock(pos, mc)) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
										}
										recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has too many eggs", pos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}

									if (facingSchematic == facingClient) {
										int clickTimes = 0;
										Direction side = Direction.NORTH;
										if (sBlock instanceof RepeaterBlock && !PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
											int clientDelay = stateClient.getValue(RepeaterBlock.DELAY);
											int schematicDelay = stateSchematic.getValue(RepeaterBlock.DELAY);
											if (clientDelay != schematicDelay) {

												if (clientDelay < schematicDelay) {
													clickTimes = schematicDelay - clientDelay;
												} else if (clientDelay > schematicDelay) {
													clickTimes = schematicDelay + (4 - clientDelay);
												}
											}
											side = Direction.UP;
										} else if (sBlock instanceof ComparatorBlock && !PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
											if (stateSchematic.getValue(ComparatorBlock.MODE) != stateClient
												.getValue(ComparatorBlock.MODE)) {
												clickTimes = 1;
											}
											side = Direction.UP;
										} else if (sBlock instanceof LeverBlock) {
											if (stateSchematic.getValue(LeverBlock.POWERED) != stateClient
												.getValue(LeverBlock.POWERED)) {
												clickTimes = 1;
											}

											/*
											 * I don't know if this direction code is needed. I am just doing it anyway to
											 * make it "make sense" to the server (I am emulating what the client does so
											 * the server isn't confused)
											 */
											if (stateClient.getValue(LeverBlock.FACE) == AttachFace.CEILING) {
												side = Direction.DOWN;
											} else if (stateClient.getValue(LeverBlock.FACE) == AttachFace.FLOOR) {
												side = Direction.UP;
											} else {
												side = stateClient.getValue(LeverBlock.FACING);
											}

										} else if (sBlock instanceof TrapDoorBlock) {
											if (!stateSchematic.is(Blocks.IRON_TRAPDOOR) && stateSchematic
												.getValue(TrapDoorBlock.OPEN) != stateClient.getValue(TrapDoorBlock.OPEN)) {
												clickTimes = 1;
											}
										} else if (sBlock instanceof FenceGateBlock) {
											if (stateSchematic.getValue(FenceGateBlock.OPEN) != stateClient
												.getValue(FenceGateBlock.OPEN)) {
												clickTimes = 1;
											}
										} else if (sBlock instanceof DoorBlock) {
											if (!stateSchematic.is(Blocks.IRON_DOOR) && stateSchematic
												.getValue(DoorBlock.OPEN) != stateClient.getValue(DoorBlock.OPEN)) {
												clickTimes = 1;
											}
										} else if (sBlock instanceof DaylightDetectorBlock) {
											if (stateSchematic.getValue(DaylightDetectorBlock.INVERTED) != stateClient.getValue(DaylightDetectorBlock.INVERTED)) {
												clickTimes = 1;
											}
											side = Direction.UP;
										} else if (sBlock instanceof NoteBlock) {
											int note = stateClient.getValue(NoteBlock.NOTE);
											int targetNote = stateSchematic.getValue(NoteBlock.NOTE);
											if (note != targetNote) {
												if (note < targetNote) {
													clickTimes = targetNote - note;
												} else if (note > targetNote) {
													clickTimes = targetNote + (25 - note);
												}
											}
										} else if (sBlock instanceof CopperGolemStatueBlock) {
											clickTimes = getCopperGolemStatuePoseClicks(stateClient, stateSchematic);
											if (clickTimes > 0 && !selectNonAxeHotbarSlot(mc)) {
												recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires a non-axe hand item to change pose", pos);
												MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
												continue;
											}
										} else if (sBlock instanceof CandleBlock) {
											int clientCandles = stateClient.getValue(CandleBlock.CANDLES);
											int schematicCandles = stateSchematic.getValue(CandleBlock.CANDLES);
											if (clientCandles < schematicCandles) {
												if (mc.player.isSecondaryUseActive()) {
													recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " can't add candles while secondary use is active", pos);
													MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
													continue;
												}
												if (!FakeAccurateBlockPlacement.canHandleOther(sBlock.asItem())) {
													continue;
												}
												if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
													BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
													if (interactBlockConsumed(mc, hitResult)) {
														io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
														cacheEasyPlacePosition(pos, true, 400);
														sleepWhenRequired(mc);
														updateLastPlacedAfterInteraction(200L);
														return InteractionResult.SUCCESS;
													}
												}
												continue;
											}
											if (clientCandles > schematicCandles) {
												if (breakBlocks) {
													if (isCreative(mc.player)) {
														if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
															interact++;
															updateLastPlacedAfterInteraction();
															if (interact >= maxInteract) {
																return InteractionResult.SUCCESS;
															}
															continue;
														}
													} else if (breaker.startBreakingBlock(pos, mc)) {
														updateLastPlacedAfterInteraction();
														return InteractionResult.SUCCESS;
													}
												}
												recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has too many candles", pos);
												MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
												continue;
											}
											if (tryApplyLightableState(mc, stateClient, stateSchematic, pos, isCreative)) {
												return InteractionResult.SUCCESS;
											}
											if (hasLightableStateMismatch(stateClient, stateSchematic)) {
												recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires light state change", pos);
												MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
												continue;
											}
										} else if (sBlock instanceof CakeBlock) {
											int clientBites = stateClient.getValue(CakeBlock.BITES);
											int schematicBites = stateSchematic.getValue(CakeBlock.BITES);
											if (clientBites != schematicBites && breakBlocks && schematicBites == 0) {
												if (isCreative(mc.player)) {
													if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
														interact++;
														updateLastPlacedAfterInteraction();
														if (interact >= maxInteract) {
															return InteractionResult.SUCCESS;
														}
														continue;
													}
												} else if (breaker.startBreakingBlock(pos, mc)) {
													updateLastPlacedAfterInteraction();
													return InteractionResult.SUCCESS;
												}
											}
											recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires bite state " + schematicBites, pos);
											MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
											continue;
										} else if (sBlock instanceof BeehiveBlock) {
											int clientHoneyLevel = stateClient.getValue(BeehiveBlock.HONEY_LEVEL);
											int schematicHoneyLevel = stateSchematic.getValue(BeehiveBlock.HONEY_LEVEL);
											if (clientHoneyLevel != schematicHoneyLevel && breakBlocks && schematicHoneyLevel == 0) {
												if (isCreative(mc.player)) {
													if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
														interact++;
														updateLastPlacedAfterInteraction();
														if (interact >= maxInteract) {
															return InteractionResult.SUCCESS;
														}
														continue;
													}
												} else if (breaker.startBreakingBlock(pos, mc)) {
													updateLastPlacedAfterInteraction();
													return InteractionResult.SUCCESS;
												}
											}
											recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires honey level " + schematicHoneyLevel, pos);
											MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
											continue;
										} else if (sBlock instanceof RespawnAnchorBlock) {
											int clientCharge = stateClient.getValue(RespawnAnchorBlock.CHARGE);
											int schematicCharge = stateSchematic.getValue(RespawnAnchorBlock.CHARGE);
											if (clientCharge > schematicCharge) {
												if (breakBlocks) {
													if (isCreative(mc.player)) {
														if (mc.gameMode.startDestroyBlock(pos, Direction.DOWN)) {
															interact++;
															updateLastPlacedAfterInteraction();
															if (interact >= maxInteract) {
																return InteractionResult.SUCCESS;
															}
															continue;
														}
													} else if (breaker.startBreakingBlock(pos, mc)) {
														updateLastPlacedAfterInteraction();
														return InteractionResult.SUCCESS;
													}
												}
												recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has too much charge", pos);
												MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
												continue;
											}
											if (clientCharge < schematicCharge) {
												ItemStack glowstoneStack = Items.GLOWSTONE.getDefaultInstance();
												if (!FakeAccurateBlockPlacement.canHandleOther(glowstoneStack.getItem())) {
													continue;
												}
												if (doSchematicWorldPickBlock(mc, glowstoneStack)) {
													Vec3 hitPos = Vec3.atCenterOf(pos);
													BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
													if (interactBlockConsumed(mc, hitResult)) {
														io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
														cacheEasyPlacePosition(pos, true, 400);
														updateLastPlacedAfterInteraction(200L);
														return InteractionResult.SUCCESS;
													}
												}
											}
										} else if (hasLightableStateMismatch(stateClient, stateSchematic)) {
											if (tryApplyLightableState(mc, stateClient, stateSchematic, pos, isCreative)) {
												return InteractionResult.SUCCESS;
											}
											recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires light state change", pos);
											MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
											continue;
										} else if (sBlock instanceof ComposterBlock && FillInventory) {
											if (!FakeAccurateBlockPlacement.canHandleOther(composableItem.getItem())) {
												continue;
											}
											int level = stateClient.getValue(ComposterBlock.LEVEL);
											int Schematiclevel = stateSchematic.getValue(ComposterBlock.LEVEL);
											if (level != Schematiclevel && !(level == 7 && Schematiclevel == 8)) {
												if (doSchematicWorldPickBlock(mc, composableItem)) {
													Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
													BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);
													if (interactBlockConsumed(mc, hitResult)) { //COMPOSTER
														io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
														cacheEasyPlacePosition(pos, false);
														updateLastPlacedAfterInteraction(200L);
														return InteractionResult.SUCCESS;
													}
												}
											} else {
												cacheEasyPlacePosition(pos, true);
											}
										} else if (!isPositionCached(pos, false) && PRINTER_PLACE_MINECART.getBooleanValue() && sBlock instanceof DetectorRailBlock && cBlock instanceof DetectorRailBlock) {
											if (!shouldAvoidPlaceCart(pos, world) && placeCart(stateSchematic, mc, pos)) {
												interact++;
												continue;
											}
										}
										boolean clicked = false;
										for (int i = 0; i < clickTimes && interact < maxInteract; i++) // Click on the block a few times
										{
											Vec3 hitPos = Vec3.atCenterOf(pos);

											BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);

											if (interactBlockConsumed(mc, hitResult)) { //NOTEBLOCK, REPEATER...
												interact++;
												clicked = true;
											}
										}

										if (clicked) {
											cacheEasyPlacePosition(pos, true, 3600);
											if (interact >= maxInteract) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
										}

									} //can place vanilla
								}
								// Blocks are not equal, but can be converted. example: dirt -> dirt path
								if (stateClient.is(Blocks.DIRT)) {
									if (stateSchematic.is(Blocks.DIRT_PATH) && PRINTER_PRINT_DIRT_VARIANTS.getBooleanValue()) {
										if (pickShovel(mc)) {
											Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
											BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
											if (interactBlockConsumed(mc, hitResult)) {
												cacheEasyPlacePosition(pos, true);
												interact++;
												if (interact >= maxInteract) {
													updateLastPlacedAfterInteraction();
													return InteractionResult.SUCCESS;
												}
											}
										}
									}
									// farmland
									else if (stateSchematic.is(Blocks.FARMLAND) && PRINTER_PRINT_DIRT_VARIANTS.getBooleanValue()) {
										if (pickHoe(mc)) {
											Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
											BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
											if (interactBlockConsumed(mc, hitResult)) {
												cacheEasyPlacePosition(pos, true);
												interact++;
												if (interact >= maxInteract) {
													updateLastPlacedAfterInteraction();
													return InteractionResult.SUCCESS;
												}
											}
										}
									}
								}
							} else if (!ClearArea && flipBlocks) {
								// Flip the block
								Block cBlock = stateClient.getBlock();
								Block sBlock = stateSchematic.getBlock();
								if (cBlock == sBlock) {
									boolean ShapeBoolean = false;
									boolean ShouldFix = false;
									if (sBlock instanceof BaseRailBlock) {
										RailShape SchematicRailShape;
										RailShape ClientRailShape;
										if (sBlock instanceof RailBlock) {
											SchematicRailShape = stateSchematic.getValue(RailBlock.SHAPE);
											ClientRailShape = stateClient.getValue(RailBlock.SHAPE);
											ShouldFix = SchematicRailShape != ClientRailShape;
											ShapeBoolean = ShouldFix && (isCornerRailShape(SchematicRailShape) && isCornerRailShape(ClientRailShape) ||
												isStraightRailShape(SchematicRailShape) && isStraightRailShape(ClientRailShape));
										} else {
											SchematicRailShape = stateSchematic.getValue(PoweredRailBlock.SHAPE);
											ClientRailShape = stateClient.getValue(PoweredRailBlock.SHAPE);
											ShouldFix = SchematicRailShape != ClientRailShape;
											ShapeBoolean = ShouldFix && isStraightRailShape(SchematicRailShape) && isStraightRailShape(ClientRailShape);
										}
									} else if (sBlock instanceof ObserverBlock || sBlock instanceof PistonBaseBlock || sBlock instanceof RepeaterBlock || sBlock instanceof ComparatorBlock || sBlock instanceof FenceGateBlock || sBlock instanceof TrapDoorBlock) {
										Direction facingSchematic = getSimplifiedFirstPropertyFacingValue(stateSchematic);
										Direction facingClient = getSimplifiedFirstPropertyFacingValue(stateClient);
										ShouldFix = facingSchematic != facingClient;
										ShapeBoolean = facingClient.getOpposite().equals(facingSchematic);
									}
									Direction side = Direction.UP;
									if (ShapeBoolean) {
										Vec3 hitPos = Vec3.atCenterOf(pos);
										BlockHitResult hitResult = new BlockHitResult(hitPos, side, pos, false);
										if (interactBlockConsumed(mc, hitResult)) { //CACTUS
											cacheEasyPlacePosition(pos, true);
											interact++;
											if (interact >= maxInteract) {
												updateLastPlacedAfterInteraction();
												return InteractionResult.SUCCESS;
											}
										}
									} else if (breakBlocks && ShouldFix) { //cannot fix via flippincactus
										if (breaker.startBreakingBlock(pos, mc)) {
											updateLastPlacedAfterInteraction();
											return InteractionResult.SUCCESS;
										}
										continue;
									}
									continue;
								}
							} //flip
							continue;
						} //cancel normal placing
					}
					if (!ClearArea && flipBlocks) {
						MessageHolder.sendClientMessage(mc.player, Component.nullToEmpty("Handling printerFlippinCactus!"), true);
						continue;
					}
					if (isPositionCached(pos, false) || bedrockBreaking || (!(schematicBlock instanceof NetherPortalBlock) && stateSchematic.isAir() && !ClearArea)) {
						continue;
					}
					Block cBlock = clientBlock;
					Block sBlock = schematicBlock;
					if (ClearArea) {
						ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(stateSchematic);
						MessageHolder.sendUniqueMessageActionBar(mc.player, "Handling printerClearArea!");
						if (isReplaceableWaterFluidSource(stateClient)) {
							if (!UseCobble) {
								stack = Items.SPONGE.getDefaultInstance();
							} else {
								stack = Items.COBBLESTONE.getDefaultInstance();
							}
						} else if (stateClient.getFluidState().getType() instanceof LavaFluid && stateClient.hasProperty(LiquidBlock.LEVEL) && stateClient.getValue(LiquidBlock.LEVEL) == 0) {
							if (!UseCobble) {
								stack = Items.SLIME_BLOCK.getDefaultInstance();
							} else {
								stack = Items.COBBLESTONE.getDefaultInstance();
							}
						} else if (ClearSnow && cBlock instanceof SnowLayerBlock) {
							stack = Items.STRING.getDefaultInstance();
						} else {
							continue;
						}
						if (doSchematicWorldPickBlock(mc, stack)) {
							Vec3 hitPos = Vec3.atCenterOf(pos).add(0, 0.5, 0);
							BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
							if (interactBlockConsumed(mc, hitResult)) { //FLUID REMOVAL
								io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
								interact++;
								cacheEasyPlacePosition(pos, false);
								sleepWhenRequired(mc);
								if (isReplaceableFluidSource(stateClient) || cBlock instanceof SnowLayerBlock) {
									updateLastPlacedAfterInteraction(200L);
								}
							}
						}
						continue;
					}
					if (isPrinterStateSatisfied(world, mc.level, pos, stateSchematic, stateClient)) {
						// Right block is in place, no need to place it again
						causeMap.remove(pos.asLong());
						continue;
					}
					if (sBlock instanceof PistonHeadBlock || stateSchematic.is(Blocks.MOVING_PISTON)) {
						continue;
					}
					if (isAutoPlacedUpperHalf(stateSchematic)) {
						continue;
					}
					if (sBlock instanceof CandleCakeBlock && cBlock instanceof CakeBlock) {
						if (tryPlaceCandleOnCake(mc, stateSchematic, stateClient, pos, isCreative)) {
							return InteractionResult.SUCCESS;
						}
						recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " requires matching candle on an uneaten cake", pos);
						MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
						continue;
					}
					if (!FakeAccurateBlockPlacement.canPlace(stateSchematic, pos)) {
						continue;
					}
					if (cBlock != sBlock && !isReplaceable(stateClient)) {
						// Wrong block is in place, requires player action to fix
						MessageHolder.sendUniqueMessage(mc.player, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is blocking placement of " + cBlock.getDescriptionId() + "!!");
						continue;
					}
					{
						// Delay inventory scans and swaps until cheap no-place checks pass.
						if (willFall(stateSchematic, mc.level, pos)) {
							// Block will fall, don't place it
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " is Falling block", pos.below());
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						} else if (!PRINTER_PLACE_ICE.getBooleanValue() && stateSchematic.is(Blocks.WATER)) {
							// Block is water, don't place it
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " is water", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						} else if (PRINTER_AVOID_BLOCKING_BEACONS.getBooleanValue() && isBlockingBeacon(stateSchematic, pos, mc.level)) {
							// Block is above beacon, don't place it
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " is above beacon", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						} else if (!PRINTER_PLACE_ICE.getBooleanValue() && stateSchematic.is(Blocks.LAVA)) {
							// Block is lava, don't place it
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " is lava", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						} else if (sBlock instanceof FallingBlock) {
							// Falling blocks, check if they have support
							BlockPos Offsetpos = pos.below();
							BlockState OffsetstateSchematic = world.getBlockState(Offsetpos);
							BlockState OffsetstateClient = mc.level.getBlockState(Offsetpos);
							if (OffsetstateClient.isAir() || (breakBlocks && OffsetstateClient.getBlock() != OffsetstateSchematic.getBlock())) {
								recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " is Falling block", pos.below());
								MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
								continue;
							}
						}
						if (!canPickBlock(mc, stateSchematic, pos)) {
							MessageHolder.sendUniqueMessage(mc.player, sBlock.getDescriptionId() + " can't be picked !!");
							continue;
						}
						boolean markObserverToBePlaced = false; // If this flag is true, we will place observer ignoring checks
						// BUD, for positions near piston with BUD, place block first.
						if (smartRedstone) {
							if (sBlock instanceof PoweredBlock) {
								if (isQCable(mc, world, pos)) {
									recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + "will QC, waiting other block at ", isQCablePos(mc, world, pos));
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
							} else if (sBlock instanceof TntBlock) {
								if (mc.level.hasNeighborSignal(pos)) {
									recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is now receiving power!", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
							} else if (sBlock instanceof PistonBaseBlock) {
								if (!shouldExtendQC(mc, world, pos)) {
									recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is QC", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								} else {
									BlockPos redirectDustPos = hasNearbyRedirectDustPos(mc, world, pos);
									if (redirectDustPos != null) {
										recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " has redirectable dust nearby at " + redirectDustPos.toShortString(), redirectDustPos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}
								}
								if (cantAvoidExtend(mc.level, pos, world)) {
									recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " will unexpectedly extend", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
								if (shouldSuppressExtend(world, pos)) {
									BlockPos wrongStatePos = hasWrongStateNearbyPos(mc, world, pos);
									if (wrongStatePos != null) {
										recordCause(pos, sBlock.getDescriptionId() + " at " + " is BUD but has wrong state nearby \n" + wrongStateNearbyReason(mc, world, wrongStatePos), wrongStatePos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}
								}
								if (willExtendInWorld(world, pos, stateSchematic.getValue(PistonBaseBlock.FACING)) != stateSchematic.getValue(PistonBaseBlock.EXTENDED) && directlyPowered(world, pos, stateSchematic.getValue(PistonBaseBlock.FACING))) {
									if (PRINTER_SUPPRESS_PUSH_LIMIT.getBooleanValue()) {
										recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " should respect push limit because its directly powered", pos);
										MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
										continue;
									}
									MessageHolder.sendUniqueMessage(mc.player, sBlock.getDescriptionId() + " at " + " is placed ignoring push limit checks, check printerSuppressPushLimitPistons option.");
								}
							} else if (sBlock instanceof ObserverBlock) {
								BlockPos causedPos = ObserverUpdateOrderPos(mc, world, pos, selectedBox);
								if (causedPos != null) {
									if (PRINTER_FLIPPINCACTUS.getBooleanValue() && canBypass(mc, world, pos)) {
										stateSchematic = stateSchematic.setValue(ObserverBlock.FACING, stateSchematic.getValue(ObserverBlock.FACING).getOpposite());
									} else {
										if (causedPos.asLong() == pos.asLong()) {
											MessageHolder.sendUniqueMessage(mc.player, "Observer at " + pos.toShortString() + " is causing self-blocking, check manually");
										}
										//TODO : if causedPos is not placeable by observer, then it will be stuck in loop.
										//Thus if observer's output is 'safe', then we will force place it.
										if (containsPositionAsReason(causedPos, pos)) {
											// We have to place observer first, then place the block.
											MessageHolder.sendUniqueMessage(mc.player, "Observer at " + pos.toShortString() + " is causing self-blocking, checking if it can be placed");
											if (checkObserverOutputs(mc, world, pos)) {
												MessageHolder.sendUniqueMessage(mc.player, "Observer at " + pos.toShortString() + " can be placed, placing it");
												// mark observer output positions as cached
												markObserverOutputs(mc, world, pos);
												markObserverToBePlaced = true;
											}
											else {
												recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is waiting for ", causedPos);
												MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
												continue;
											}
										}
										else {
											recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is waiting for ", causedPos);
											MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
											continue;
										}
									}
								}
							}
						}
						if (smartRedstone && ExplicitObserver) {
							BlockPos observerPos = isObserverCantAvoidOutput(mc, world, pos);
							if (observerPos != null) {
								recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " is waiting for preceded observer at " + observerPos.toShortString(), observerPos);
								MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
								cacheEasyPlacePosition(pos, false, 400); // cache for 400 ms
								continue;
							}
							if (sBlock instanceof ObserverBlock && !markObserverToBePlaced) {
								Tuple<Boolean, BlockPos> value = isWatchingCorrectState(mc, world, pos, null, true);
								if (!value.getA()) {
									recordCause(pos, sBlock.getDescriptionId() + " at " + pos.toShortString() + " can't be placed due to " + value.getB().toShortString(), value.getB());
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
							}
						}
						if (sBlock instanceof NetherPortalBlock && sBlock != cBlock &&
							//#if MC>=11700
							PortalShape.findEmptyPortalShape(mc.level, pos, Direction.Axis.X).isPresent()
							//#else
							//$$ AreaHelper.method_30485(mc.world, pos, Direction.Axis.X).isPresent()
							//#endif
						) {
							ItemStack lightStack = Items.FIRE_CHARGE.getDefaultInstance();
							if (getSlotWithStack(mc.player, lightStack) == -1) {
								lightStack = Items.FLINT_AND_STEEL.getDefaultInstance();
							}
							BlockPos offsetPos = pos.below();
							BlockState offsetStateSchematic = world.getBlockState(offsetPos);
							BlockState offsetStateClient = mc.level.getBlockState(offsetPos);
							if (getSlotWithStack(mc.player, lightStack) == -1 || offsetStateClient.isAir() || offsetStateClient.getBlock() != offsetStateSchematic.getBlock()) {
								continue;
							}
							if (doSchematicWorldPickBlock(mc, lightStack)) {
								Vec3 hitPos = Vec3.atCenterOf(offsetPos).add(0, 0.5, 0);
								BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, offsetPos, false);
								if (interactBlockConsumed(mc, hitResult)) { //LIGHT
									cacheEasyPlacePosition(pos, false);
									boolean wasSleepingLonger = sleepWhenRequired(mc);
									updateLastPlacedAfterInteraction(200L);
									if (!wasSleepingLonger) {
										interact++;
									}
									return InteractionResult.SUCCESS;
								}
							}
						}
						Direction facing = getSimplifiedFirstPropertyFacingValue(stateSchematic);
						if (facing != null) {
							facing = facing.getOpposite();
						}
						if (stateSchematic.getBlock() instanceof BaseRailBlock) {
							facing = convertRailShapetoFace(stateSchematic);
						}
						if (hasFrontAndTopOrientation(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceFrontAndTopOrientation(stateSchematic, primaryFacing, horizontalFacing)) {
							continue;
						}
						if (stateSchematic.getBlock() instanceof WallHangingSignBlock
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()) {
							Direction hangingSignSide = getWallHangingSignPlacementSide(mc.level, pos, stateSchematic);
							if (!canPlaceWallHangingSignFromPlayer(stateSchematic, facingSides, hangingSignSide)) {
								continue;
							}
						}
						if (stateSchematic.getBlock() instanceof CeilingHangingSignBlock
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceCeilingHangingSign(mc.level, pos, stateSchematic, getYaw(mc.player), mc.player.isSecondaryUseActive())) {
							continue;
						}
						if (hasSegmentedHorizontalPlacement(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceSegmentedHorizontalBlock(mc.level, pos, stateSchematic, horizontalFacing, mc.player.isSecondaryUseActive())) {
							continue;
						}
						if (hasDripleafPlacementFacing(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceDripleaf(mc.level, pos, stateSchematic, horizontalFacing)) {
							continue;
						}
						if (hasSpeleothemPlacement(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceSpeleothem(mc.level, pos, stateSchematic, getNearestLookingVerticalDirection(mc.player), mc.player.isSecondaryUseActive())) {
							continue;
						}
						if (hasMossyCarpetPlacement(stateSchematic)
							&& !canPlaceMossyCarpet(mc.level, pos, stateSchematic)) {
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't be placed as the requested moss carpet derived state", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						}
						if (hasLanternHangingPlacement(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceLantern(mc.level, pos, stateSchematic)) {
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't be placed with the requested hanging state", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						}
						if (hasMultifaceOrVinePlacement(stateSchematic)
							&& !(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock()))
							&& !PRINTER_FAKE_ROTATION.getBooleanValue()
							&& !canPlaceMultifaceOrVine(mc.level, pos, stateSchematic, facingSides)) {
							recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't place a requested face from the current look direction", pos);
							MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
							continue;
						}
						if (facing != null) {
							FacingData facedata = FacingData.getFacingData(stateSchematic);
							if (facedata == null && !(stateSchematic.getBlock() instanceof BaseRailBlock) && !hasDripleafPlacementFacing(stateSchematic) && !hasSpeleothemPlacement(stateSchematic) && !simulateFacingData(stateSchematic, pos, Vec3.atCenterOf(pos)) ) {
								MessageHolder.sendMessageUncheckedUnique(mc.player, stateSchematic.getBlock() + " does not have facing data, please add this!");
								if (PRINTER_SKIP_UNKNOWN_BLOCKSTATE.getBooleanValue()) continue;

							}
							boolean manualTrapDoorPlacement = stateSchematic.getBlock() instanceof TrapDoorBlock && !CanUseProtocol && !PRINTER_FAKE_ROTATION.getBooleanValue();
							boolean manualSegmentedPlacement = hasSegmentedHorizontalPlacement(stateSchematic) && !CanUseProtocol && !PRINTER_FAKE_ROTATION.getBooleanValue();
							if (!(CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock())) && !PRINTER_FAKE_ROTATION.getBooleanValue() && !manualTrapDoorPlacement && !manualSegmentedPlacement && !canPlaceFace(facedata, stateSchematic, primaryFacing, horizontalFacing)) {
								continue;
							}

							if ((stateSchematic.getBlock() instanceof DoorBlock
								&& stateSchematic.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
								|| (stateSchematic.getBlock() instanceof BedBlock
								&& stateSchematic.getValue(BedBlock.PART) == BedPart.HEAD)) {
								continue;
							}
						}

						// Exception for yaw-only rotation blocks (signs, banners, skulls).
						if (hasYawRotation16Placement(stateSchematic) && !PRINTER_FAKE_ROTATION.getBooleanValue()) {
							if (!canPlaceYawRotation16(stateSchematic, getYaw(mc.player))) {
								continue;
							}
						}
						Direction sideOrig = Direction.NORTH;
						BlockPos npos = pos;
						Direction side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
						Block blockSchematic = stateSchematic.getBlock();
						if (blockSchematic instanceof WallHangingSignBlock) {
							side = getWallHangingSignPlacementSide(mc.level, pos, stateSchematic);
						} else if (blockSchematic instanceof LanternBlock) {
							side = getLanternPlacementSide(stateSchematic);
						} else if (hasMultifaceOrVinePlacement(stateSchematic)) {
							Direction face = getMultifaceOrVinePlacementLookDirection(mc.level, pos, stateSchematic, facingSides);
							if (face == null) {
								continue;
							}
							side = getMultifaceOrVineClickSide(face);
						}
						//Don't place waterlogged block's original block before fluid since its painful
						// 1. if
						if (PRINTER_PLACE_ICE.getBooleanValue() &&
							(isReplaceableWaterFluidSource(stateSchematic) && isReplaceable(stateClient) && !isReplaceableWaterFluidSource(stateClient) && !stateClient.is(Blocks.LAVA) ||
								PRINTER_WATERLOGGED_WATER_FIRST.getBooleanValue() && isReplaceable(stateClient) && containsWaterloggable(stateSchematic))
						) {
							ItemStack iceStack = Items.ICE.getDefaultInstance();
							if (!FakeAccurateBlockPlacement.canHandleOther(iceStack.getItem())) {
								continue;
							}
							if (doSchematicWorldPickBlock(mc, iceStack)) {
								BlockHitResult hitResult = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), Direction.DOWN, pos, false);
								if (interactBlockConsumed(mc, hitResult)) {
									io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
									cacheEasyPlacePosition(pos, false);
									sleepWhenRequired(mc);
									interact++;
								}
							} //ICE
							else {
								recordCause(pos, "Can't pick item " + Items.ICE.getDescriptionId() + " at " + pos.toShortString(), pos);
							}
							continue;
						}
						//#if MC >= 12006
						if (!stateSchematic.canSurvive(mc.level, pos)
							&& !canPlaceBigDripleafStemAsLeaf(mc.level, pos, stateSchematic, horizontalFacing)) {
						//#else
						//$$ if (!blockSchematic.canPlaceAt(stateSchematic, mc.world, pos)) {
						//#endif
							recordCause(pos, stateSchematic.getBlock().toString() + "(" + pos.toShortString() + ", can't be placed)", pos);
							MessageHolder.sendUniqueMessage(mc.player, stateSchematic.getBlock().getDescriptionId() + " can't be placed at " + pos.toShortString());
							continue;
						}
						if (blockSchematic instanceof GrindstoneBlock) {
							FakePlacementResult grindstoneResult = placeGrindStone(stateSchematic, mc, pos);
							if (grindstoneResult == FakePlacementResult.QUEUED) {
								return queuedFakePlacementSuccess(interact);
							}
							if (grindstoneResult == FakePlacementResult.PLACED) {
								interact++;
							}
							continue;
						}
						if (blockSchematic instanceof TrapDoorBlock && !CanUseProtocol && !PRINTER_FAKE_ROTATION.getBooleanValue()) {
							if (placeTrapDoor(stateSchematic, mc, pos)) {
								interact++;
							}
							continue;
						}
						int miliseconds = EASY_PLACE_CACHE_TIME.getIntegerValue();
						if (blockSchematic instanceof FaceAttachedHorizontalDirectionalBlock || blockSchematic instanceof BellBlock || blockSchematic instanceof TorchBlock || blockSchematic instanceof WallSkullBlock
							|| blockSchematic instanceof LadderBlock
							|| blockSchematic instanceof TripWireHookBlock || blockSchematic instanceof WallSignBlock || blockSchematic instanceof WallBannerBlock ||
							blockSchematic instanceof WallHangingSignBlock || blockSchematic instanceof CeilingHangingSignBlock ||
							blockSchematic instanceof EndRodBlock || blockSchematic instanceof BaseCoralFanBlock ||
							blockSchematic instanceof AmethystClusterBlock || blockSchematic instanceof CocoaBlock ||
							blockSchematic instanceof LanternBlock || hasMultifaceOrVinePlacement(stateSchematic) ||
							hasDripleafPlacementFacing(stateSchematic) || blockSchematic instanceof SpeleothemBlock) {

							/*
							 * Some blocks, especially wall mounted blocks must be placed on another for
							 * directionality to work Basically, the block pos sent must be a "clicked"
							 * block.
							 */
							if (blockSchematic instanceof ButtonBlock || blockSchematic instanceof LeverBlock) {
								npos = getFaceAttachedSupportPos(pos, stateSchematic);
							} else if (blockSchematic instanceof BellBlock) {
								npos = getBellSupportPos(pos, stateSchematic);
							} else if (blockSchematic instanceof WallHangingSignBlock) {
								side = getWallHangingSignPlacementSide(mc.level, pos, stateSchematic);
								npos = pos.relative(side.getOpposite());
							} else if (blockSchematic instanceof CeilingHangingSignBlock) {
								side = Direction.DOWN;
								npos = pos.above();
							} else if (blockSchematic instanceof TorchBlock) {
								if (blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock) {
									npos = pos.relative(stateSchematic.getValue(WallTorchBlock.FACING).getOpposite());
								} else {
									npos = pos.below();
								}
								BlockState attachedSupportState = mc.level.getBlockState(npos);
								if (!mc.player.isSecondaryUseActive() && hasGui(attachedSupportState.getBlock())) {
									if (PRINTER_FAKE_ROTATION.getBooleanValue() && interact < maxInteract) {
										FakePlacementResult fakeResult = requestFakePlacement(stateSchematic, pos);
										if (fakeResult == FakePlacementResult.QUEUED) {
											return queuedFakePlacementSuccess(interact);
										}
										if (fakeResult == FakePlacementResult.PLACED) {
											interact++;
										}
										continue;
									}
									recordCause(pos, "Torch at " + pos.toShortString() + " can't be placed due to " + attachedSupportState.getBlock().getDescriptionId() + "at " + npos.toShortString() + " has GUI", npos);
									MessageHolder.sendUniqueMessage(mc.player, "Torch at " + pos.toShortString() + " can't be placed due to " + attachedSupportState.getBlock().getDescriptionId() + "at " + npos.toShortString() + " has GUI");
									continue;
								}
							} else if (blockSchematic instanceof BaseCoralFanBlock) {
								if (blockSchematic instanceof BaseCoralWallFanBlock) {
									npos = pos.relative(stateSchematic.getValue(BaseCoralWallFanBlock.FACING).getOpposite());
								} else {
									npos = pos.below();
								}
							} else {
								npos = pos.relative(side.getOpposite()); //offset block for 'side'
							}
							//Any : if we have block in testPos, then we can place with wanted direction.
							//Trapdoors : it can be placed in air with player direction's opposite.
							//Else : can't be placed except End Rod.
							BlockState attachedSupportState = mc.level.getBlockState(npos);
							if (!isReplaceable(attachedSupportState)) {
								if (wouldEndRodReverseOnPlacement(stateSchematic, attachedSupportState)) {
									recordCause(pos, "End rod at " + pos.toShortString() + " would be reversed by end rod support at " + npos.toShortString(), npos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
								//npos is blockPos to be hit.
								//instead, hitVec should have 1 corresponding to direction property.
								//but First check if its block with GUI*
								Block checkGui = attachedSupportState.getBlock();
								if (!mc.player.isSecondaryUseActive() && hasGui(checkGui) && !isHangingSignChainClick(blockSchematic, attachedSupportState, side)) {
									if (PRINTER_FAKE_ROTATION.getBooleanValue() && interact < maxInteract) {
										FakePlacementResult fakeResult = requestFakePlacement(stateSchematic, pos);
										if (fakeResult == FakePlacementResult.QUEUED) {
											return queuedFakePlacementSuccess(interact);
										}
										if (fakeResult == FakePlacementResult.PLACED) {
											interact++;
										}
										continue;
									}
									//Has GUI so clickPos can't be clicked.
									recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " can't be placed at " + pos.toShortString() + "because " + npos.toShortString() + " has GUI", npos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								} else if (blockSchematic instanceof TorchBlock) {
									//no gui, just place
									if (blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock) {
										MessageHolder.sendDebugMessage(mc.player, "placing wall torch clicking " + npos.toShortString() + " torch facing : " + stateSchematic.getValue(WallTorchBlock.FACING).toString());
										Direction required = stateSchematic.getValue(WallTorchBlock.FACING);
										Vec3 hitVec = hitVecOnSide(npos, required);
										if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
											BlockHitResult blockHitResult = new BlockHitResult(hitVec, required, npos, false);
											if (interactBlockConsumed(mc, blockHitResult)) { //place block
												cacheEasyPlacePosition(pos, false);
												interact++;
												if (stateSchematic.hasProperty(RedstoneTorchBlock.LIT) && !stateSchematic.getValue(RedstoneTorchBlock.LIT)) {
													cacheEasyPlacePosition(pos.above(), false, miliseconds);
												}
												io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
												sleepWhenRequired(mc);
											}
										}
										continue;
									}
									Vec3 hitVec = hitVecOnSide(npos, Direction.UP);
									if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
										MessageHolder.sendDebugMessage(mc.player, "Placing torch clicking " + npos.toShortString());
										MessageHolder.sendDebugMessage(mc.player, "\t Wanted torch pos : " + pos.toShortString());
										MessageHolder.sendDebugMessage(mc.player, "\t HitVec applied : " + hitVec);
										MessageHolder.sendDebugMessage(mc.player, "\t Side applied : " + Direction.UP);
										BlockHitResult blockHitResult = new BlockHitResult(hitVec, Direction.UP, npos, false);
										if (interactBlockConsumed(mc, blockHitResult)) { //place block
											cacheEasyPlacePosition(pos, false);
											interact++;
											if (stateSchematic.hasProperty(RedstoneTorchBlock.LIT) && !stateSchematic.getValue(RedstoneTorchBlock.LIT)) {
												cacheEasyPlacePosition(pos.above(), false, miliseconds);
											}
											io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
											sleepWhenRequired(mc);
										}
									}
									continue;
								} else if ((blockSchematic instanceof WallHangingSignBlock && canPlaceWallHangingSignFromPlayer(stateSchematic, facingSides, side))
									|| (blockSchematic instanceof CeilingHangingSignBlock && canPlaceCeilingHangingSign(mc.level, pos, stateSchematic, getYaw(mc.player), mc.player.isSecondaryUseActive()))
									|| (!(blockSchematic instanceof WallHangingSignBlock) && !(blockSchematic instanceof CeilingHangingSignBlock)
									&& canPlaceFace(FacingData.getFacingData(stateSchematic), stateSchematic, primaryFacing, horizontalFacing))) { // no gui
									Direction required = side;
									if (!(blockSchematic instanceof WallHangingSignBlock) && !(blockSchematic instanceof CeilingHangingSignBlock)) {
										required = getSimplifiedFirstPropertyFacingValue(stateSchematic);
										required = applyPlacementFacing(stateSchematic, required, stateClient);
									}
									Vec3 hitVec = applySupportClickHitVec(npos, required);
									if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
										BlockHitResult blockHitResult = new BlockHitResult(hitVec, required, npos, false);
										if (interactBlockConsumed(mc, blockHitResult)) { //place block
											cacheEasyPlacePosition(pos, false);
											interact++;
											if (stateSchematic.hasProperty(RedstoneTorchBlock.LIT) && !stateSchematic.getValue(RedstoneTorchBlock.LIT)) {
												cacheEasyPlacePosition(pos.above(), false, 700);
											}
											io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
											sleepWhenRequired(mc);
										}
									}
									continue;
								}
							} else if (blockSchematic instanceof TrapDoorBlock) { //check direction is opposite of player's
								Direction trapdoor = stateSchematic.getValue(TrapDoorBlock.FACING);
								if (horizontalFacing.getOpposite() == trapdoor) {
									if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
										BlockHitResult blockHitResult = new BlockHitResult(Vec3.atLowerCornerOf(pos),
											stateSchematic.getValue(TrapDoorBlock.FACING).getOpposite(), pos, false);
										if (interactBlockConsumed(mc, blockHitResult)) { //place block
											cacheEasyPlacePosition(pos, false);
											io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
											sleepWhenRequired(mc);
											interact++;
										}
									}
									continue;
								}
							} else if (blockSchematic instanceof GrindstoneBlock) {
								Direction direction = stateSchematic.getValue(GrindstoneBlock.FACING);
								if ((primaryFacing.getAxis() == Direction.Axis.Y && horizontalFacing == direction) || (primaryFacing.getAxis() != Direction.Axis.Y && horizontalFacing == direction.getOpposite())) {
									if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
										BlockHitResult blockHitResult = new BlockHitResult(Vec3.atLowerCornerOf(pos),
											stateSchematic.getValue(GrindstoneBlock.FACING).getOpposite(), pos, false);
										if (interactBlockConsumed(mc, blockHitResult)) { //place block
											cacheEasyPlacePosition(pos, false);
											io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
											sleepWhenRequired(mc);
											interact++;
										}
									}
								}
								continue;
							} else { //Only end rod.
								if (blockSchematic instanceof EndRodBlock) {
									if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
										BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(pos),
											stateSchematic.getValue(EndRodBlock.FACING), pos, false);
										if (interactBlockConsumed(mc, blockHitResult)) { //place block
											cacheEasyPlacePosition(pos, false);
											io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
											interact++;
											sleepWhenRequired(mc);
										}
									}
								}
								continue;
							}

						} //End of trapdoor / wall mounted blocks

						Vec3 hitPos;
						// Carpet Accurate Placement protocol support, plus BlockSlab support
						if (CanUseProtocol && IsBlockSupportedCarpet(stateSchematic.getBlock())) {
							hitPos = applyCarpetProtocolHitVec(npos, stateSchematic);
						} else {
							hitPos = applyHitVec(npos, stateSchematic, side);
						}

						// Mark that this position has been handled (use the non-offset position that is
						// checked above)
						BlockHitResult hitResult = new BlockHitResult(hitPos, side, npos, false);

						//System.out.printf("pos: %s side: %s, hit: %s\n", pos, side, hitPos);
						// pos, side, hitPos
						if (stateSchematic.getBlock() instanceof SnowLayerBlock) {
							stateClient = mc.level.getBlockState(npos);
							if (stateClient.isAir() || stateClient.getBlock() instanceof SnowLayerBlock
								&& stateClient.getValue(SnowLayerBlock.LAYERS) < stateSchematic.getValue(SnowLayerBlock.LAYERS)) {
								side = Direction.UP;
								hitPos = hitVecOnSide(npos, side);
								hitResult = new BlockHitResult(hitPos, side, npos, false);
								if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
									if (interactBlockConsumed(mc, hitResult)) { //SNOW LAYERS
										cacheEasyPlacePosition(pos, false);
										interact++;
										io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
										sleepWhenRequired(mc);
									}
								}
							}
							continue;
						}
						//finally places block
						if (!PRINTER_FAKE_ROTATION.getBooleanValue() || PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) { //Accurateblockplacement, or vanilla but no fake
							if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
								MessageHolder.sendOrderMessage("Places block " + blockSchematic + " at " + pos.toShortString());
								if (!canPlaceSourceDerivedState(mc.player, stateSchematic, hitResult)) {
									recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't be placed as the requested source placement state", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
								if (interactBlockConsumed(mc, hitResult)) { //PLACE BLOCK
									io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
									cacheEasyPlacePosition(pos, false);
									if (smartRedstone) {
										cacheSmartRedstoneAfterPlacement(mc, world, stateSchematic, pos);
									}
									sleepWhenRequired(mc);
									interact++;
								}
							}
							continue;
						} else {
							if (!(sBlock instanceof LiquidBlock)) {
								if (interact < maxInteract) {
									FakePlacementResult fakeResult = requestFakePlacement(stateSchematic, pos);
									if (fakeResult == FakePlacementResult.QUEUED) {
										return queuedFakePlacementSuccess(interact);
									}
									if (fakeResult == FakePlacementResult.PLACED) {
										if (smartRedstone) {
											cacheSmartRedstoneAfterPlacement(mc, world, stateSchematic, pos);
										}
										interact++;
										continue;
									}
								}
							}
						}
						if (interact >= maxInteract) {
							updateLastPlacedAfterInteraction();
							return InteractionResult.SUCCESS;
						}
						if (stateSchematic.getBlock() instanceof SlabBlock
							&& stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
							stateClient = mc.level.getBlockState(npos);

							if (stateClient.getBlock() instanceof SlabBlock
								&& stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
								side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
								hitPos = hitVecOnSide(npos, side);
								hitResult = new BlockHitResult(hitPos, side, npos, false);
								if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
									if (interactBlockConsumed(mc, hitResult)) { //double slab
										io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
										cacheEasyPlacePosition(pos, false);
										sleepWhenRequired(mc);
										interact++;
									}
								}
								continue;
							}
						}
						if (stateSchematic.getBlock() instanceof SeaPickleBlock
							&& stateSchematic.getValue(SeaPickleBlock.PICKLES) > 1) {
							stateClient = mc.level.getBlockState(npos);
							if (stateClient.getBlock() instanceof SeaPickleBlock
								&& stateClient.getValue(SeaPickleBlock.PICKLES) < stateSchematic.getValue(SeaPickleBlock.PICKLES)) {
								if (mc.player.isSecondaryUseActive()) {
									recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't add pickles while secondary use is active", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
								side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
								hitPos = hitVecOnSide(npos, side);
								hitResult = new BlockHitResult(hitPos, side, npos, false);
								if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
									if (interactBlockConsumed(mc, hitResult)) { //sea pickle
										io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
										cacheEasyPlacePosition(pos, false);
										sleepWhenRequired(mc);
										interact++;
									}
								}
								continue;
							}
						}
						if (stateSchematic.getBlock() instanceof TurtleEggBlock
							&& stateSchematic.getValue(TurtleEggBlock.EGGS) > 1) {
							stateClient = mc.level.getBlockState(npos);
							if (stateClient.getBlock() instanceof TurtleEggBlock
								&& stateClient.getValue(TurtleEggBlock.EGGS) < stateSchematic.getValue(TurtleEggBlock.EGGS)) {
								if (mc.player.isSecondaryUseActive()) {
									recordCause(pos, stateSchematic.getBlock().getDescriptionId() + " at " + pos.toShortString() + " can't add eggs while secondary use is active", pos);
									MessageHolder.sendUniqueMessage(mc.player, getReason(pos.asLong()));
									continue;
								}
								side = Direction.UP;
								hitPos = hitVecOnSide(npos, side);
								hitResult = new BlockHitResult(hitPos, side, npos, false);
								if (doSchematicWorldPickBlock(mc, stateSchematic, pos)) {
									if (interactBlockConsumed(mc, hitResult)) {
										io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative);
										cacheEasyPlacePosition(pos, false);
										sleepWhenRequired(mc);
										interact++;
									}
								}
								continue;
							}
						}

						if (interact >= maxInteract) {
							updateLastPlacedAfterInteraction();
							return InteractionResult.SUCCESS;
						}

					}
				}
			}

		}

		if (interact > 0) {
			updateLastPlacedAfterInteraction();
			return InteractionResult.SUCCESS;
		}
		if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem) && !(mc.player.getOffhandItem().getItem() instanceof BlockItem)) {
			return InteractionResult.PASS;
		}
		return InteractionResult.FAIL;
	}

	/**
	 * Checks if the block being placed is blocking a beacon
	 * @param stateSchematic the block being placed
	 * @param pos the position in the world
	 * @param world the world the block is being placed in
	 * @return true if the block is blocking a beacon
	 */
	private static boolean isBlockingBeacon(BlockState stateSchematic, BlockPos pos, ClientLevel world) {
		//#if MC>=12102
		if(stateSchematic.propagatesSkylightDown()) {
		//#else
		//$$ if(stateSchematic.isTransparent(world, pos)) {
		//#endif
			return false;
		}
		int minY = world.getMinY();
		int maxY = pos.getY();
		BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos(pos.getX(), minY, pos.getZ());
		for(int y = minY; y < maxY; y++) {
			bp.setY(y);
			BlockState state = world.getBlockState(bp);
			if(!state.is(Blocks.BEACON) ) {
				continue;
			}
			BlockEntity blockEntity = world.getBlockEntity(bp);
			if (!(blockEntity instanceof BeaconBlockEntity)) {
				continue;
			}
			BeaconBlockEntity beacon = (BeaconBlockEntity) blockEntity;
			if (beacon.getBeamSections().isEmpty()) {
				continue;
			}
			return true;
		}
		return false;
	}

	private static void markObserverOutputs(Minecraft mc, Level world, BlockPos pos) {
		// Caches all observer outputs
		// using cacheEasyPlacePosition to cache the position
		// default : 400ms
		BlockPos observerOutput = pos.relative(world.getBlockState(pos).getValue(ObserverBlock.FACING));
		BlockPos observerOutputDown = observerOutput.below();
		cacheEasyPlacePosition(observerOutput, false, 400);
		cacheEasyPlacePosition(observerOutputDown, false, 400);
	}

	private static boolean checkObserverOutputs(Minecraft mc, Level world, BlockPos pos) {
		// check observer's behind, and its behind's down
		BlockState observerState = world.getBlockState(pos);
		Direction facing = observerState.getValue(ObserverBlock.FACING);
		BlockPos behindPos = pos.relative(facing.getOpposite());
		BlockPos behindDownPos = behindPos.below();
		// check client world, if both are air, then it's safe to place
		BlockState behindStateClient = mc.level.getBlockState(behindPos);
		BlockState behindDownStateClient = mc.level.getBlockState(behindDownPos);
		if (behindStateClient.isAir() && behindDownStateClient.isAir()) {
			return true;
		}
		// if block behind is powerable block in client world, we have to check recursively
		//#if MC>=12102
		if(!behindStateClient.propagatesSkylightDown()) {
		//#else
		//$$ if (!behindStateClient.isTransparent(world, behindPos)) {
		//#endif
			return false;
		}
		// if block behind-down is QCable, we have to check recursively
		if (behindDownStateClient.is(Blocks.PISTON) || behindDownStateClient.is(Blocks.STICKY_PISTON)) {
			return false;
		}
		return true;
	}

	private static boolean willFall(BlockState stateSchematic, Level clientWorld, BlockPos pos) {
		if (stateSchematic.getBlock() instanceof ScaffoldingBlock) {
			//#if MC >= 12006
			return !stateSchematic.canSurvive(clientWorld, pos);
			//#else
			//$$ return !stateSchematic.getBlock().canPlaceAt(stateSchematic, clientWorld, pos);
			//#endif
		}
		return false;
	}

	/*
		returns if redstone block should not be placed (before piston)
	 */
	private static boolean isQCable(Minecraft mc, Level schematicWorld, BlockPos pos) {
		BlockPos downPos = pos.below();
		for (Direction direction : BedrockBreaker.HORIZONTAL) {
			BlockPos offsetPos = downPos.relative(direction);
			BlockState stateClient = mc.level.getBlockState(offsetPos);
			BlockState stateSchematic = schematicWorld.getBlockState(offsetPos);
			if (!(stateSchematic.getBlock() instanceof PistonBaseBlock)) {
				continue;
			}
			if (stateSchematic.getValue(PistonBaseBlock.EXTENDED)) {
				continue;
			}
			if (stateClient.isAir()) { //very basic qc
				return true;
			} else if (!hasNoUpdatableState(mc, schematicWorld, offsetPos)) {
				return true;
			} else if (stateClient.getBlock() instanceof PistonBaseBlock && stateSchematic.getValue(PistonBaseBlock.FACING) == Direction.UP) {
				BlockPos aboveOffsetPos = offsetPos.above();
				BlockState aboveSchematicState = schematicWorld.getBlockState(aboveOffsetPos);
				BlockState aboveClientState = mc.level.getBlockState(aboveOffsetPos);
				if (aboveSchematicState.getBlock() != aboveClientState.getBlock()) {
					return true;
				}
			}
		}
		BlockState pistonBelowState = schematicWorld.getBlockState(downPos.below());
		if (!isPositionWithinBox(downPos)) {
			return false;
		}
		BlockState downSchematicState = schematicWorld.getBlockState(downPos);
		BlockState downClientState = mc.level.getBlockState(downPos);
		return pistonBelowState.getBlock() instanceof PistonBaseBlock
			&& !pistonBelowState.getValue(PistonBaseBlock.EXTENDED)
			&& downSchematicState.getBlock() != downClientState.getBlock();
	}

	private static BlockPos isQCablePos(Minecraft mc, Level world, BlockPos pos) {
		BlockPos downPos = pos.below();
		for (Direction direction : BedrockBreaker.HORIZONTAL) {
			BlockPos offsetPos = downPos.relative(direction);
			BlockState stateClient = mc.level.getBlockState(offsetPos);
			BlockState stateSchematic = world.getBlockState(offsetPos);
			if (!(stateSchematic.getBlock() instanceof PistonBaseBlock)) {
				continue;
			}
			if (stateSchematic.getValue(PistonBaseBlock.EXTENDED)) {
				continue;
			}
			if (stateClient.isAir()) { //very basic qc
				return offsetPos;
			} else if (!hasNoUpdatableState(mc, world, offsetPos)) {
				return hasNoUpdatableStatePos(mc, world, offsetPos);
			} else if (stateClient.getBlock() instanceof PistonBaseBlock && stateSchematic.getValue(PistonBaseBlock.FACING) == Direction.UP) {
				BlockPos aboveOffsetPos = offsetPos.above();
				BlockState aboveSchematicState = world.getBlockState(aboveOffsetPos);
				BlockState aboveClientState = mc.level.getBlockState(aboveOffsetPos);
				if (aboveSchematicState.getBlock() != aboveClientState.getBlock()) {
					return offsetPos;
				}
			}
		}
		BlockState pistonBelowState = world.getBlockState(downPos.below());
		if (!isPositionWithinBox(downPos)) {
			return null;
		}
		BlockState downSchematicState = world.getBlockState(downPos);
		BlockState downClientState = mc.level.getBlockState(downPos);
		if (pistonBelowState.getBlock() instanceof PistonBaseBlock
			&& !pistonBelowState.getValue(PistonBaseBlock.EXTENDED)
			&& downSchematicState.getBlock() != downClientState.getBlock()) {
			return downPos;
		}
		return null;
	}

	private static boolean hasNoUpdatableState(Minecraft mc, Level world, BlockPos pos) {
		for (Direction direction : ALL_DIRECTIONS) {
			BlockPos offsetPos = pos.relative(direction);
			BlockState schematicState = world.getBlockState(offsetPos);
			BlockState clientState = mc.level.getBlockState(offsetPos);
			if (!sameBlockState(schematicState, clientState)) {
				if (!isNoteBlockInstrumentError(mc, world, offsetPos) && !isDoorHingeError(mc, world, offsetPos)) {
					if (!isPositionWithinBox(offsetPos) || schematicState.isAir() && clientState.isAir()) {
						continue;
					}
					return false;
				}
			}
		}
		return true;
	}

	private static BlockPos hasNoUpdatableStatePos(Minecraft mc, Level world, BlockPos pos) {
		for (Direction direction : ALL_DIRECTIONS) {
			BlockPos offsetPos = pos.relative(direction);
			BlockState schematicState = world.getBlockState(offsetPos);
			BlockState clientState = mc.level.getBlockState(offsetPos);
			if (!sameBlockState(schematicState, clientState)) {
				if (!isNoteBlockInstrumentError(mc, world, offsetPos) && !isDoorHingeError(mc, world, offsetPos)) {
					if (!isPositionWithinBox(offsetPos) || schematicState.isAir() && clientState.isAir()) {
						continue;
					}
					return offsetPos;
				}
			}
		}
		return null;
	}

	private static BlockPos hasNearbyRedirectDustPos(Minecraft mc, Level world, BlockPos pos) { //temporary code, just direct redirection check nearby
		for (Direction direction : ALL_DIRECTIONS) {
			BlockPos oneAway = pos.relative(direction);
			if (!isPositionWithinBox(oneAway)) {
				continue;
			}
			if (!isCorrectDustState(mc, world, oneAway)) {
				return oneAway;
			}
			BlockPos oneAwayAbove = oneAway.above();
			if (!isCorrectDustState(mc, world, oneAwayAbove)) {
				return oneAwayAbove;
			}
			if (direction.getAxis() == Direction.Axis.Y) {
				continue;
			}
			BlockPos twoAway = pos.relative(direction, 2);
			if (!isCorrectDustState(mc, world, twoAway)) {
				return twoAway;
			}
			BlockPos twoAwayAbove = twoAway.above();
			if (!isCorrectDustState(mc, world, twoAwayAbove)) {
				return twoAwayAbove;
			}
		}
		return null;
	}

	private static boolean cantAvoidExtend(Level world, BlockPos pos, Level schematicWorld) {
		BlockState schematicState = schematicWorld.getBlockState(pos);
		if (!schematicState.getValue(PistonBaseBlock.EXTENDED)) {
			return willExtendInWorld(world, pos, schematicState.getValue(PistonBaseBlock.FACING));
		}
		return false;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isCorrectDustState(Minecraft mc, Level world, BlockPos pos) {
		BlockState ClientState = mc.level.getBlockState(pos);
		BlockState SchematicState = world.getBlockState(pos);
		if (!SchematicState.is(Blocks.REDSTONE_WIRE)) {
			return true;
		}
		if (!ClientState.is(Blocks.REDSTONE_WIRE)) {
			return false;
		}
		return SchematicState.getValue(RedStoneWireBlock.EAST) == ClientState.getValue(RedStoneWireBlock.EAST) &&
			SchematicState.getValue(RedStoneWireBlock.WEST) == ClientState.getValue(RedStoneWireBlock.WEST) &&
			SchematicState.getValue(RedStoneWireBlock.SOUTH) == ClientState.getValue(RedStoneWireBlock.SOUTH) &&
			SchematicState.getValue(RedStoneWireBlock.NORTH) == ClientState.getValue(RedStoneWireBlock.NORTH) &&
			(SchematicState.getValue(RedStoneWireBlock.POWER) == 0) == (ClientState.getValue(RedStoneWireBlock.POWER) == 0);
	}

	private static boolean shouldExtendQC(Minecraft mc, Level world, BlockPos pos) {
		BlockState schematicState = world.getBlockState(pos);
		return willExtendInWorld(mc.level, pos, schematicState.getValue(PistonBaseBlock.FACING)) == schematicState.getValue(PistonBaseBlock.EXTENDED);
	}

	/*
		returns if piston is powered + but it's not extended in schematic, can be BUD or direct power
	 */
	private static boolean shouldSuppressExtend(Level world, BlockPos pos) {
		BlockState schematicState = world.getBlockState(pos);
		return willExtendInWorld(world, pos, schematicState.getValue(PistonBaseBlock.FACING)) && !schematicState.getValue(PistonBaseBlock.EXTENDED);
	}

	/*
		returns if piston is DIRECTLY powered by redstone block, can't be solved via QC references.
	 */
	private static boolean directlyPowered(Level schematicWorld, BlockPos pos, Direction pistonFace) {
		for (Direction lv : ALL_DIRECTIONS) {
			if (lv == pistonFace) {
				continue;
			}
			if (schematicWorld.getBlockState(pos.relative(lv)).is(Blocks.REDSTONE_BLOCK)) {
				return true;
			}
		}
		return false;
	}

	private static boolean willExtendInWorld(Level world, BlockPos pos, Direction pistonFace) {
		for (Direction lv : ALL_DIRECTIONS) {
			BlockPos signalPos = pos.relative(lv);
			if (lv == pistonFace || !world.hasSignal(signalPos, lv)) {
				continue;
			}
			//Observer client error wtf?
			boolean hasObserver = false;
			for (Direction dir : ALL_DIRECTIONS) {
				BlockState observerState = world.getBlockState(signalPos.relative(dir));
				if (observerState.is(Blocks.OBSERVER)) {
					if (observerState.getValue(ObserverBlock.POWERED)) {
						hasObserver = true;
						break;
					}
				}
			}
			BlockState adjState = world.getBlockState(signalPos);
			if (adjState.is(Blocks.OBSERVER)) {
				if (adjState.getValue(ObserverBlock.POWERED)) {
					hasObserver = true;
				}
			}
			if (hasObserver) {
				continue;
			}
			return true;
		}
		if (world.hasSignal(pos, Direction.DOWN)) {
			return true;
		}
		BlockPos lv2 = pos.above();
		for (Direction lv3 : ALL_DIRECTIONS) {
			BlockPos qcPowerPos = lv2.relative(lv3);
			if (lv3 == Direction.DOWN || !world.hasSignal(qcPowerPos, lv3)) {
				continue;
			}
			BlockState qcState = world.getBlockState(qcPowerPos);
			if (qcState.is(Blocks.OBSERVER) && qcState.getValue(ObserverBlock.FACING) == lv3 && qcState.getValue(ObserverBlock.POWERED)) {
				continue;
			}
			return true;
		}
		return false;
	}

	/* * *
	returns if block is observer output but observer can't avoid update
	If its true, then block should be placed after observer update is done
	Case A : Observer is facing wall attached : observer - wall - output
	Case B : Observer is facing Noteblock from horizontal : observer - block below noteblock - noteblock - output
	Case C : Observer is facing wire connected to observer's up offset
	 * * */
	@SuppressWarnings({"ConstantConditions"})
	private static BlockPos isObserverCantAvoidOutput(Minecraft mc, Level schematicWorld, BlockPos pos) {
		BlockState schematicState = schematicWorld.getBlockState(pos);
		boolean centerIsQCable = isQCableBlock(schematicState);
		if (centerIsQCable) {
			BlockPos observerPos = pos.above(2);
			BlockState observerState = schematicWorld.getBlockState(observerPos);
			if (observerState.is(Blocks.OBSERVER) && observerState.getValue(ObserverBlock.FACING) == Direction.UP) {
				BlockPos outputPos = pos.above(3);
				BlockState clientOutputState = mc.level.getBlockState(outputPos);
				BlockState schematicOutputState = schematicWorld.getBlockState(outputPos);
				BlockState clientObserverState = mc.level.getBlockState(observerPos);
				if (!sameBlockState(clientOutputState, schematicOutputState) || clientObserverState.hasProperty(ObserverBlock.POWERED) && clientObserverState.getValue(ObserverBlock.POWERED)) {
					MessageHolder.sendDebugMessage("Position at " + pos.toShortString() + " has observer that will QC, but not watching correct state");
					return outputPos;
				}
			}
		}
		for (Direction direction : ALL_DIRECTIONS) {
			BlockPos offsetPos = pos.relative(direction);
			BlockState offsetState = schematicWorld.getBlockState(offsetPos);
			if (offsetState.getBlock() instanceof ObserverBlock && offsetState.getValue(ObserverBlock.FACING) == direction) {
				Tuple<Boolean, BlockPos> value = isWatchingCorrectState(mc, schematicWorld, offsetPos, null, false);
				if (!value.getA()) {
					return offsetPos;
				}
			}
			//QC
			if (direction == Direction.UP || direction == Direction.DOWN || !centerIsQCable) {
				continue;
			}
			//Horizontal,
			BlockPos qcPos = pos.relative(direction).above();
			BlockState qcState = schematicWorld.getBlockState(qcPos);
			BlockState existingState = mc.level.getBlockState(qcPos);
			if (qcState.getBlock() instanceof ObserverBlock && !existingState.is(Blocks.OBSERVER) && qcState.getValue(ObserverBlock.FACING) == direction) {
				Tuple<Boolean, BlockPos> value = isWatchingCorrectState(mc, schematicWorld, qcPos, null, false);
				if (!value.getA()) {
					return offsetPos;
				}
			}
			// again, QC + powerable block uhh
			else if (qcState.isRedstoneConductor(schematicWorld, qcPos)) {
				qcPos = qcPos.relative(direction);
				qcState = schematicWorld.getBlockState(qcPos);
				existingState = mc.level.getBlockState(qcPos);
				if (qcState.getBlock() instanceof ObserverBlock && !existingState.is(Blocks.OBSERVER) && qcState.getValue(ObserverBlock.FACING) == direction) {
					Tuple<Boolean, BlockPos> value = isWatchingCorrectState(mc, schematicWorld, qcPos, null, false);
					if (!value.getA()) {
						return offsetPos;
					}
				}
			}
		}
		return null;
	}

	private static boolean sleepWhenRequired(Minecraft mc) {
		if (!USE_INVENTORY_CACHE.getBooleanValue()) {
			return false;
		}
		if (PRINTER_SLEEP_STACK_EMPTIED.getIntegerValue() > 0 && io.github.eatmyvenom.litematicin.utils.InventoryUtils.lastCount <= 0) {
			recordExternalStackEmptiedSleep();
			MessageHolder.sendUniqueMessageActionBar(mc.player, "Sleeping because stack is emptied!");
			isSleeping = true;
			return true;
		}
		return false;
	}

	private static boolean isQCableBlock(Level world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return (!PRINTER_AVOID_CHECK_ONLY_PISTONS.getBooleanValue() && block instanceof DispenserBlock) || block instanceof PistonBaseBlock;
	}

	private static boolean isQCableBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		return (!PRINTER_AVOID_CHECK_ONLY_PISTONS.getBooleanValue() && block instanceof DispenserBlock) || block instanceof PistonBaseBlock;
	}

	/***
	 *
	 * @param mc : client
	 * @param schematicWorld : schematic world
	 * @param pos : BlockPos
	 * @param recursive : Sets of position checked
	 * @param allowFirst : direct search of wallmount / walls / etc. at first
	 * @return Entry : correct / position caused
	 */
	private static Tuple<Boolean, BlockPos> isWatchingCorrectState(Minecraft mc, Level schematicWorld, BlockPos pos, Set<Long> recursive, boolean allowFirst) {
		//observer, then recursive
		if (recursive == null) {
			recursive = new HashSet<>();
		}
		if (recursive.contains(pos.asLong())) {
			return new Tuple<>(true, pos);
		}
		BlockState clientState = mc.level.getBlockState(pos);
		BlockState schematicState = schematicWorld.getBlockState(pos);
		if (schematicState.getBlock() instanceof ObserverBlock) {
			Direction facing = schematicState.getValue(ObserverBlock.FACING);
			recursive.add(pos.asLong());
			if (allowFirst && ObserverCantAvoid(mc, schematicWorld, facing, pos)) {
				return new Tuple<>(true, pos);
			} else {
				Tuple<Boolean, BlockPos> entry = isWatchingCorrectState(mc, schematicWorld, pos.relative(facing), recursive, allowFirst);
				if (entry.getA()) {
					return entry;
				} else {
					return new Tuple<>(false, pos);
				}
			}
		}// virtual observers then go recursive
		else {
			if (schematicState.is(Blocks.VOID_AIR) || schematicState.is(Blocks.BARRIER) || schematicState.isAir()) {
				return new Tuple<>(true, pos);
			} else if (!sameBlockState(clientState, schematicState)) {
				//but check wire...
				if (isNoteBlockInstrumentError(mc, schematicWorld, pos) || isDoorHingeError(mc, schematicWorld, pos)) {
					return new Tuple<>(true, pos);
				}
				if (isClientPowerError(mc, schematicWorld, clientState, schematicState, pos)) {
					return new Tuple<>(true, pos);
				}
				return new Tuple<>(false, pos);
			}
		}
		return new Tuple<>(true, pos);
	}

	private static boolean isClientPowerError(Minecraft mc, Level world, BlockState clientState, BlockState schematicState, BlockPos pos) {
		//handles client error, mostly, dropper being powered directly, hopper being powered etc
		if (clientState.getBlock() != schematicState.getBlock()) {
			return false;
		}
		if (schematicState.is(Blocks.DROPPER) || schematicState.is(Blocks.DISPENSER)) {
			if (schematicState.getValue(DropperBlock.TRIGGERED) != clientState.getValue(DropperBlock.TRIGGERED)) {
				boolean isReceiving = mc.level.hasNeighborSignal(pos) || mc.level.hasNeighborSignal(pos.above());
				if (isReceiving != clientState.getValue(DropperBlock.TRIGGERED)) {
					mc.level.setBlockAndUpdate(pos, clientState.setValue(DropperBlock.TRIGGERED, isReceiving));
				}
				return sameBlockState(mc.level.getBlockState(pos), schematicState);
			}
		} else if (schematicState.is(Blocks.NOTE_BLOCK)) { //special case
			if (schematicState.getValue(NoteBlock.POWERED) != clientState.getValue(NoteBlock.POWERED)) {
				boolean isReceiving = mc.level.hasNeighborSignal(pos);
				mc.level.setBlockAndUpdate(pos, clientState.setValue(NoteBlock.POWERED, isReceiving)); //lets fix
				return isNoteBlockInstrumentError(mc, world, pos);
			}
		} else if (schematicState.is(Blocks.HOPPER)) {
			if (schematicState.getValue(HopperBlock.ENABLED) != clientState.getValue(HopperBlock.ENABLED)) {
				boolean isReceiving = mc.level.hasNeighborSignal(pos);
				mc.level.setBlockAndUpdate(pos, clientState.setValue(HopperBlock.ENABLED, !isReceiving));
				return sameBlockState(mc.level.getBlockState(pos), schematicState);
			}
		} else if (schematicState.getBlock() instanceof CrafterBlock) {
			if (schematicState.getValue(CrafterBlock.TRIGGERED) != clientState.getValue(CrafterBlock.TRIGGERED)) {
				boolean isReceiving = mc.level.hasNeighborSignal(pos);
				if (isReceiving != clientState.getValue(CrafterBlock.TRIGGERED)) {
					mc.level.setBlockAndUpdate(pos, clientState.setValue(CrafterBlock.TRIGGERED, isReceiving));
				}
				return sameBlockState(mc.level.getBlockState(pos), schematicState);
			}
		}
		return false;
	}

	private static boolean ObserverCantAvoid(Minecraft mc, Level world, Direction facingSchematic, BlockPos pos) {
		//returns true if observer should be placed regardless of state
		BlockPos posOffset = pos.relative(facingSchematic);
		BlockState OffsetStateSchematic = world.getBlockState(posOffset);
		Block offsetBlock = OffsetStateSchematic.getBlock();
		if (OffsetStateSchematic.is(Blocks.NOTE_BLOCK)) {
			if (isNoteBlockInstrumentError(mc, world, posOffset) || isDoorHingeError(mc, world, posOffset)) {
				//everything is correct but litematica error
				return true;
			}
		}
		if (facingSchematic == Direction.UP) {
			return offsetBlock instanceof WallBlock || offsetBlock instanceof ComparatorBlock || offsetBlock instanceof DoorBlock ||
				offsetBlock instanceof RepeaterBlock || offsetBlock instanceof FallingBlock ||
				offsetBlock instanceof BaseRailBlock || offsetBlock instanceof NoteBlock ||
				offsetBlock instanceof BubbleColumnBlock || offsetBlock instanceof RedStoneWireBlock ||
				((offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock) && OffsetStateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.FLOOR);
		} else if (facingSchematic == Direction.DOWN) {
			return offsetBlock instanceof WallBlock || offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock && OffsetStateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING;
		} else {
			return offsetBlock instanceof WallBlock || offsetBlock instanceof IronBarsBlock || offsetBlock instanceof FenceBlock || OffsetStateSchematic.is(Blocks.IRON_BARS) || offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock &&
				OffsetStateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL && OffsetStateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACING) == facingSchematic || hasDustOrAscendingRails(world, facingSchematic, pos);
		}
	}

	private static boolean hasDustOrAscendingRails(Level schematicWorld, Direction watching, BlockPos observerPos) {
		BlockPos possible = observerPos.relative(watching);
		BlockState state = schematicWorld.getBlockState(possible);
		if (state.is(Blocks.REDSTONE_WIRE)) {
			//ascending_'opposite' directions
			//watching.getOpposite should have 'up'
			RedstoneSide connection = state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(watching.getOpposite()));
			return connection == RedstoneSide.UP;

		} else if (state.getBlock() instanceof PoweredRailBlock) {
			switch (watching) {
				case NORTH : {
					return state.getValue(PoweredRailBlock.SHAPE) == RailShape.ASCENDING_SOUTH;
				}
				case SOUTH : {
					return state.getValue(PoweredRailBlock.SHAPE) == RailShape.ASCENDING_NORTH;
				}
				case EAST : {
					return state.getValue(PoweredRailBlock.SHAPE) == RailShape.ASCENDING_WEST;
				}
				case WEST : {
					return state.getValue(PoweredRailBlock.SHAPE) == RailShape.ASCENDING_EAST;
				}
				default : {
					return false;
				}
			}
		}
		return false;
	}

	private static List<BlockPos> getNeighborsExcept(BlockPos pos, Direction except) {
		List<BlockPos> retVal = new ArrayList<>(5);
		for (Direction direction : ALL_DIRECTIONS) {
			if (direction == except.getOpposite()) {
				continue;
			}
			retVal.add(pos.relative(direction));
		}
		return retVal;
	}

	/*
		if block is observer updating block, then return position to not place until its finished
	 */
	private static Set<BlockPos> ObserverCantAvoidPos(Minecraft mc, Level world, BlockPos pos) {
		//returns true if observer should be placed regardless of state
		BlockPos posOffset;
		Set<BlockPos> relatedPos = new HashSet<>(6);
		BlockState offsetStateSchematic;
		BlockState targetState = world.getBlockState(pos);
		Block block = targetState.getBlock();
		if (block instanceof ComparatorBlock || block instanceof RepeaterBlock || block instanceof FallingBlock ||
			block instanceof BaseRailBlock || block instanceof RedStoneWireBlock || block instanceof DoorBlock ||
			((block instanceof FaceAttachedHorizontalDirectionalBlock) && targetState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.FLOOR)) {
			//check downward
			BlockPos observerPos = pos.below();
			BlockState observerState = world.getBlockState(observerPos);
			if (observerState.is(Blocks.OBSERVER) && observerState.getValue(ObserverBlock.FACING) == Direction.UP) {
				relatedPos.add(pos.below(2));
				relatedPos.addAll(getNeighborsExcept(pos, Direction.DOWN));
				BlockPos pistonPos = pos.below(3);
				if (world.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock) {
					relatedPos.add(pistonPos);
				}
				return relatedPos;
			}
		}
		if (block instanceof NoteBlock) {
			if (!isNoteBlockInstrumentError(mc, world, pos)) {
				relatedPos.add(pos.below(2));
				relatedPos.addAll(getNeighborsExcept(pos, Direction.DOWN));
				BlockPos pistonPos = pos.below(3);
				if (world.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock) {
					relatedPos.add(pistonPos);
				}
				return relatedPos;
			}
		} else if (block instanceof DoorBlock) {
			if (!isDoorHingeError(mc, world, pos)) {
				relatedPos.add(pos.below(2));
				relatedPos.addAll(getNeighborsExcept(pos, Direction.DOWN));
				BlockPos pistonPos = pos.below(3);
				if (world.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock) {
					relatedPos.add(pistonPos);
				}
				return relatedPos;
			}
		}
		for (Direction direction : ALL_DIRECTIONS) {
			posOffset = pos.relative(direction);
			offsetStateSchematic = world.getBlockState(posOffset);
			if (offsetStateSchematic.is(Blocks.OBSERVER) && offsetStateSchematic.getValue(ObserverBlock.FACING) == direction.getOpposite()) {
				if (block instanceof WallBlock) {
					BlockPos related = pos.relative(direction, 2);
					relatedPos.add(related);
					relatedPos.addAll(getNeighborsExcept(pos, direction));
					BlockPos pistonPos = related.below();
					if (world.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock) {
						relatedPos.add(pistonPos);
					}
				} else if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
					if (targetState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING && direction == Direction.UP ||
						targetState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.FLOOR && direction == Direction.DOWN ||
						targetState.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL && targetState.getValue(FaceAttachedHorizontalDirectionalBlock.FACING) == direction.getOpposite()
					) {
						BlockPos related = pos.relative(direction, 2);
						relatedPos.add(related);
						relatedPos.addAll(getNeighborsExcept(pos, direction));
						BlockPos pistonPos = related.below();
						if (world.getBlockState(pistonPos).getBlock() instanceof PistonBaseBlock) {
							relatedPos.add(pistonPos);
						}
					}
				} else if (block instanceof PoweredRailBlock || block instanceof RedStoneWireBlock) {
					if (hasDustOrAscendingRails(world, direction.getOpposite(), pos)) {
						relatedPos.add(pos.relative(direction, 2));
						relatedPos.addAll(getNeighborsExcept(pos, direction));
					}
				}
			}
		}
		return relatedPos;
	}


	private static boolean shouldAvoidPlaceCart(BlockPos pos, Level schematicWorld) {
		//avoids TNT priming
		for (Direction direction : ALL_DIRECTIONS) {
			if (schematicWorld.getBlockState(pos.below().relative(direction)).is(Blocks.TNT)) {
				return true;
			}
		}
		return false;
	}

	// returns should call continue in loop
	@SuppressWarnings({"ConstantConditions"})
	private static boolean placeCart(BlockState state, Minecraft client, BlockPos pos) {
		if (state.is(Blocks.DETECTOR_RAIL) && state.getValue(DetectorRailBlock.POWERED) != client.level.getBlockState(pos).getValue(DetectorRailBlock.POWERED) && canPickItem(client, Items.MINECART.getDefaultInstance()) && client.player.position().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 20.25) {
			Vec3 clickPos = Vec3.atLowerCornerOf(pos).add(0.5, 0.125, 0.5);
			if (!FakeAccurateBlockPlacement.canHandleOther(Items.MINECART)) {
				return false;
			}
			if (doSchematicWorldPickBlock(client, Items.MINECART.getDefaultInstance())) {
				if (interactBlockConsumed(client, new BlockHitResult(clickPos, Direction.UP, pos, false))) { //place block
					cacheEasyPlacePosition(pos, false, 600);
					io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative(client.player));
					sleepWhenRequired(client);
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@SuppressWarnings({"ConstantConditions"})
	private static FakePlacementResult placeGrindStone(BlockState state, Minecraft client, BlockPos pos) {
		if (PRINTER_FAKE_ROTATION.getBooleanValue()) {
			return requestFakePlacement(state, pos);
			//place in air
		}
		boolean canAttach = canAttachGrindstone(state, client, pos);
		boolean facingCorrectly = isFacingCorrectly(state, client.player);
		if (!canAttach && !facingCorrectly) {
			return FakePlacementResult.NONE;
		}
		Direction side = getFaceAttachedPlacementSide(state);
		BlockPos clickPos;
		Vec3 hitVec;
		if (canAttach) {
			clickPos = getFaceAttachedSupportPos(pos, state);
			hitVec = hitVecOnSide(clickPos, side);
			if (doSchematicWorldPickBlock(client, state, pos)) {
				if (interactBlockConsumed(client, new BlockHitResult(hitVec, side, clickPos, false))) { //place block
					cacheEasyPlacePosition(pos, false);
					io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative(client.player));
					sleepWhenRequired(client);
					return FakePlacementResult.PLACED;
				}
			}
		} else {
			if (facingCorrectly) {
				hitVec = Vec3.atCenterOf(pos);
				clickPos = pos;
				if (doSchematicWorldPickBlock(client, state, pos)) {
					if (interactBlockConsumed(client, new BlockHitResult(hitVec, side, clickPos, false))) { //place block
						cacheEasyPlacePosition(pos, false);
						io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative(client.player));
						sleepWhenRequired(client);
						return FakePlacementResult.PLACED;
					}
				}
			}
		}
		return FakePlacementResult.NONE;
	}

	@SuppressWarnings({"ConstantConditions"})
	private static boolean canAttachGrindstone(BlockState state, Minecraft client, BlockPos pos) {
		if (PRINTER_FAKE_ROTATION.getBooleanValue()) {
			return true;
			//place in air.
		}
		BlockPos supportPos = getFaceAttachedSupportPos(pos, state);
		BlockState supportState = client.level.getBlockState(supportPos);
		return !isReplaceable(supportState)
			&& canPlaceFaceAttachedFromSupport(state, client.player.getDirection())
			&& (!hasGui(supportState.getBlock()) || client.player.isSecondaryUseActive());
	}

	private static boolean isFacingCorrectly(BlockState state, LocalPlayer player) {
		//if we can't attach, use player's directions
		Direction[] facingOrder = Direction.orderedByNearest(player);
		return canPlaceFaceAttachedFromPlayer(state, facingOrder[0], player.getDirection());
	}

	private static boolean placeTrapDoor(BlockState state, Minecraft client, BlockPos pos) {
		//check if it can be clicked on face, then place inside block
		Direction side = state.getValue(TrapDoorBlock.FACING);
		BlockPos clickPos;
		Vec3 hitVec;
		if (isReplaceable(client.level.getBlockState(pos.relative(side.getOpposite())))) {
			//place inside block
			clickPos = pos;
			if (client.player.getDirection().getOpposite() == side) {
				side = state.getValue(TrapDoorBlock.HALF) == Half.TOP ? Direction.DOWN : Direction.UP;
			} else {
				return false;
			}
			hitVec = Vec3.atLowerCornerOf(clickPos).add(0.5, 0.5, 0.5);
		} else {
			clickPos = pos.relative(side.getOpposite());
			double hitY = state.getValue(TrapDoorBlock.HALF) == Half.TOP ? 0.75 : 0.25;
			hitVec = Vec3.atLowerCornerOf(clickPos).add(0.5, hitY, 0.5).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));
		}
		if (doSchematicWorldPickBlock(client, state, pos)) {
			if (interactBlockConsumed(client, new BlockHitResult(hitVec, side, clickPos, false))) { //place block
				cacheEasyPlacePosition(pos, false);
				io.github.eatmyvenom.litematicin.utils.InventoryUtils.decrementCount(isCreative(client.player));
				sleepWhenRequired(client);
				return true;
			}
		}
		return false;
	}

	private static boolean isNoteBlockInstrumentError(Minecraft mc, Level world, BlockPos pos) {
		BlockState stateA = world.getBlockState(pos);
		BlockState stateB = mc.level.getBlockState(pos);
		return stateA.is(Blocks.NOTE_BLOCK) && stateB.is(Blocks.NOTE_BLOCK) &&
			stateA.getValue(NoteBlock.POWERED) == stateB.getValue(NoteBlock.POWERED) &&
			isReplaceable(world.getBlockState(pos.below())) == isReplaceable(mc.level.getBlockState(pos.relative(Direction.DOWN)));
	}

	private static boolean isDoorHingeError(Minecraft mc, Level world, BlockPos pos) {
		BlockState stateA = world.getBlockState(pos);
		BlockState stateB = mc.level.getBlockState(pos);
		return stateA.hasProperty(DoorBlock.HINGE) && stateB.hasProperty(DoorBlock.HINGE) &&
			stateA.getValue(DoorBlock.POWERED) == stateB.getValue(DoorBlock.POWERED) &&
			stateA.getValue(DoorBlock.FACING) == stateB.getValue(DoorBlock.FACING) &&
			stateA.getValue(DoorBlock.OPEN) == stateB.getValue(DoorBlock.OPEN) &&
			stateA.getValue(DoorBlock.HALF) == stateB.getValue(DoorBlock.HALF);
	}

	private static BlockPos ObserverUpdateOrderPos(Minecraft mc, Level world, BlockPos pos, Box selectedBox) {
		//returns position if observer should not be placed
		boolean ExplicitObserver = PRINTER_OBSERVER_AVOID_ALL.getBooleanValue();
		BlockState stateSchematic = world.getBlockState(pos);
		BlockPos posOffset;
		BlockState OffsetStateSchematic;
		BlockState OffsetStateClient;
		if (stateSchematic.getValue(ObserverBlock.POWERED)) {
			return null;
		}
		Direction facingSchematic = getSimplifiedFirstPropertyFacingValue(stateSchematic);
		assert facingSchematic != null;
		boolean observerCantAvoid = ObserverCantAvoid(mc, world, facingSchematic, pos);
		if (observerCantAvoid) {
			return null;
		}
		posOffset = pos.relative(facingSchematic);
		if (!isPositionWithinBox(selectedBox, posOffset)) {
			return null;
		}
		OffsetStateSchematic = world.getBlockState(posOffset);
		OffsetStateClient = mc.level.getBlockState(posOffset);
		if (OffsetStateSchematic.is(Blocks.BARRIER)) {
			return null;
		} else if (OffsetStateSchematic.is(Blocks.OBSERVER) && OffsetStateSchematic.getValue(ObserverBlock.FACING) == facingSchematic.getOpposite()) {
			return null;
		} else if (OffsetStateSchematic.getBlock() instanceof DoorBlock && OffsetStateClient.getBlock() instanceof DoorBlock &&
			OffsetStateSchematic.getValue(DoorBlock.POWERED) == OffsetStateClient.getValue(DoorBlock.POWERED) &&
			OffsetStateSchematic.getValue(DoorBlock.FACING) == OffsetStateClient.getValue(DoorBlock.FACING)) //hinge error
		{
			return null;
		}
		if (ExplicitObserver) {
			if (OffsetStateSchematic.is(Blocks.BARRIER) || OffsetStateClient.isAir() && OffsetStateSchematic.isAir() || OffsetStateSchematic.is(Blocks.VOID_AIR)) {
				return null;
			} //cave air wtf
			if (!sameBlockState(OffsetStateSchematic, OffsetStateClient)) {
				if (isClientPowerError(mc, world, OffsetStateClient, OffsetStateSchematic, posOffset)) {
					return null;
				}
				return posOffset;
			}
		}

		if (OffsetStateClient.getBlock() != OffsetStateSchematic.getBlock()) {
			return posOffset;
		}
		return null;
	}

	/*
	 * Checks if the block can be placed in the correct orientation if player is
	 * facing a certain direction Don't place block if orientation will be wrong
	 */
	private static boolean canPlaceFace(FacingData facedata, BlockState stateSchematic,
	                                    Direction primaryFacing, Direction horizontalFacing) {
		if (stateSchematic.getBlock() instanceof WallHangingSignBlock || stateSchematic.getBlock() instanceof CeilingHangingSignBlock) {
			return true;
		}
		if (stateSchematic.getBlock() instanceof BellBlock) {
			return canPlaceBell(stateSchematic, primaryFacing, horizontalFacing);
		}
		if (stateSchematic.getBlock() instanceof CocoaBlock) {
			return true;
		}
		Direction facing = getSimplifiedFirstPropertyFacingValue(stateSchematic);
		if (stateSchematic.getBlock() instanceof BaseRailBlock) {
			facing = convertRailShapetoFace(stateSchematic);
		}
		if (facing != null && facedata != null) {
			// backward compatibility, JAVA_8 can't use enhanced switch
			switch (facedata.type) {
				case 0: // All directions (ie, observers and pistons)
					if (facedata.isReversed) {
						return facing.getOpposite() == primaryFacing;
					} else {
						return facing == primaryFacing;
					}

				case 1: // Only Horizontal directions (ie, repeaters and comparators)
					if (facedata.isReversed) {
						return facing.getOpposite() == horizontalFacing;
					} else {
						return facing == horizontalFacing;
					}
				case 2: // Face-attached blocks use clicked support side first, then player horizontal direction for floor/ceiling.
					return canPlaceFaceAttachedFromSupport(stateSchematic, horizontalFacing);
				case 3: //rotated, why, anvil, WNES order
					return horizontalFacing.getClockWise() == facing;
				case 4: //rails
					return facing == horizontalFacing || facing == horizontalFacing.getOpposite();
				//return facing == horizontalFacing || facing == horizontalFacing.getOpposite();
				default: // Ignore rest -> TODO: Other blocks like anvils, etc...
					return true;
			}
		} else {
			if (stateSchematic.getBlock() instanceof TorchBlock && !(stateSchematic.getBlock() instanceof WallTorchBlock) && !(stateSchematic.getBlock() instanceof RedstoneWallTorchBlock)) {
				return Direction.DOWN == primaryFacing;
			}
			return true;
		}
	}

	private static boolean canPlaceBell(BlockState stateSchematic, Direction primaryFacing, Direction horizontalFacing) {
		BellAttachType attachment = stateSchematic.getValue(BellBlock.ATTACHMENT);
		if (attachment == BellAttachType.FLOOR || attachment == BellAttachType.CEILING) {
			return horizontalFacing == stateSchematic.getValue(BellBlock.FACING);
		}
		return true;
	}

	private static boolean canPlaceFaceAttachedFromSupport(BlockState stateSchematic, Direction horizontalFacing) {
		AttachFace face = stateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
		if (face == AttachFace.WALL) {
			return true;
		}
		return horizontalFacing == stateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
	}

	private static boolean canPlaceFaceAttachedFromPlayer(BlockState stateSchematic, Direction primaryFacing, Direction horizontalFacing) {
		AttachFace face = stateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
		Direction facing = stateSchematic.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
		if (face == AttachFace.WALL) {
			return primaryFacing.getAxis().isHorizontal() && primaryFacing.getOpposite() == facing;
		}
		return primaryFacing == (face == AttachFace.CEILING ? Direction.UP : Direction.DOWN) && horizontalFacing == facing;
	}

	private static Direction getFaceAttachedPlacementSide(BlockState state) {
		AttachFace face = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
		if (face == AttachFace.CEILING) {
			return Direction.DOWN;
		}
		if (face == AttachFace.FLOOR) {
			return Direction.UP;
		}
		return state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
	}

	private static BlockPos getFaceAttachedSupportPos(BlockPos pos, BlockState state) {
		return pos.relative(getFaceAttachedPlacementSide(state).getOpposite());
	}

	private static Direction getBellPlacementSide(BlockState state) {
		BellAttachType attachment = state.getValue(BellBlock.ATTACHMENT);
		if (attachment == BellAttachType.CEILING) {
			return Direction.DOWN;
		}
		if (attachment == BellAttachType.FLOOR) {
			return Direction.UP;
		}
		return state.getValue(BellBlock.FACING).getOpposite();
	}

	private static BlockPos getBellSupportPos(BlockPos pos, BlockState state) {
		return pos.relative(getBellPlacementSide(state).getOpposite());
	}

	static boolean wouldEndRodReverseOnPlacement(BlockState state, BlockState supportState) {
		return state.getBlock() instanceof EndRodBlock
			&& supportState.getBlock() instanceof EndRodBlock
			&& supportState.getValue(EndRodBlock.FACING) == state.getValue(EndRodBlock.FACING);
	}

	@Nullable
	static PlacementClick createVanillaPlacementClick(Level clientWorld, BlockPos targetPos, BlockState state,
	                                                  BlockState clientState, Direction fallbackSide, boolean secondaryUseActive,
	                                                  Direction[] facingSides) {
		Direction side = applyPlacementFacing(state, fallbackSide, clientState);
		if (state.getBlock() instanceof WallHangingSignBlock) {
			side = getWallHangingSignPlacementSide(clientWorld, targetPos, state);
		} else if (hasMultifaceOrVinePlacement(state)) {
			Direction face = getMultifaceOrVinePlacementLookDirection(clientWorld, targetPos, state, facingSides);
			if (face == null) {
				return null;
			}
			side = getMultifaceOrVineClickSide(face);
		}
		BlockPos clickPos = getSupportClickPos(targetPos, state, side);
		if (!clickPos.equals(targetPos)) {
			BlockState supportState = clientWorld.getBlockState(clickPos);
			if (isReplaceable(supportState)) {
				if (state.getBlock() instanceof EndRodBlock) {
					clickPos = targetPos;
				} else {
					return null;
				}
			} else if (!secondaryUseActive && hasGui(supportState.getBlock()) && !isHangingSignChainClick(state.getBlock(), supportState, side)) {
				return null;
			} else if (wouldEndRodReverseOnPlacement(state, supportState)) {
				return null;
			}
		}
		Vec3 hitVec = clickPos.equals(targetPos) ? applyHitVec(targetPos, state, side) : applySupportClickHitVec(clickPos, side);
		return new PlacementClick(clickPos, side, hitVec);
	}

	private static BlockPos getSupportClickPos(BlockPos pos, BlockState state, Direction side) {
		Block block = state.getBlock();
		if (block instanceof BellBlock) {
			return getBellSupportPos(pos, state);
		}
		if (block instanceof CeilingHangingSignBlock) {
			return pos.above();
		}
		if (block instanceof LanternBlock) {
			return pos.relative(getLanternPlacementSide(state).getOpposite());
		}
		if (hasDripleafPlacementFacing(state)) {
			return pos.below();
		}
		if (block instanceof SpeleothemBlock) {
			return pos.relative(state.getValue(SpeleothemBlock.TIP_DIRECTION).getOpposite());
		}
		if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
			return getFaceAttachedSupportPos(pos, state);
		}
		if (block instanceof TorchBlock) {
			if (block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock) {
				return pos.relative(state.getValue(WallTorchBlock.FACING).getOpposite());
			}
			return pos.below();
		}
		if (block instanceof BaseCoralFanBlock) {
			if (block instanceof BaseCoralWallFanBlock) {
				return pos.relative(state.getValue(BaseCoralWallFanBlock.FACING).getOpposite());
			}
			return pos.below();
		}
		if (side != null && (block instanceof WallSkullBlock || block instanceof LadderBlock
			|| block instanceof TripWireHookBlock || block instanceof WallSignBlock || block instanceof WallBannerBlock || block instanceof WallHangingSignBlock || block instanceof EndRodBlock
			|| block instanceof AmethystClusterBlock || block instanceof CocoaBlock || hasMultifaceOrVinePlacement(state))) {
			return pos.relative(side.getOpposite());
		}
		return pos;
	}

	private static Vec3 applySupportClickHitVec(BlockPos clickedPos, Direction side) {
		return hitVecOnSide(clickedPos, side);
	}

	private static Vec3 hitVecOnSide(BlockPos pos, Direction side) {
		return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.5));
	}

	private static Vec3 hitVecAtY(BlockPos pos, double yOffset) {
		return Vec3.atLowerCornerOf(pos).add(0.5, yOffset, 0.5);
	}

	private static boolean isReplaceableFluidSource(BlockState checkState) {
		return checkState.getBlock() instanceof LiquidBlock && checkState.getValue(LiquidBlock.LEVEL) == 0 ||
			checkState.getBlock() instanceof BubbleColumnBlock ||
			checkState.is(Blocks.SEAGRASS) || checkState.is(Blocks.TALL_SEAGRASS) ||
			checkState.getBlock() instanceof SimpleWaterloggedBlock && checkState.getValue(BlockStateProperties.WATERLOGGED) && isReplaceable(checkState);
	}

	static boolean isReplaceableWaterFluidSource(BlockState checkState) {
		return checkState.is(Blocks.SEAGRASS) || checkState.is(Blocks.TALL_SEAGRASS) ||
			checkState.is(Blocks.WATER) && checkState.hasProperty(LiquidBlock.LEVEL) && checkState.getValue(LiquidBlock.LEVEL) == 0 ||
			checkState.getBlock() instanceof BubbleColumnBlock ||
			checkState.getBlock() instanceof SimpleWaterloggedBlock && checkState.hasProperty(BlockStateProperties.WATERLOGGED) && checkState.getValue(BlockStateProperties.WATERLOGGED) && isReplaceable(checkState);
	}

	private static boolean containsWaterloggable(BlockState state) {
		return state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED);
	}

	private static boolean isBambooSaplingAwaitingStalk(Level schematicWorld, Level clientWorld, BlockPos pos, BlockState schematicState, BlockState clientState) {
		if (!(schematicState.getBlock() instanceof BambooStalkBlock) || !clientState.is(Blocks.BAMBOO_SAPLING)) {
			return false;
		}
		BlockPos abovePos = pos.above();
		if (!(schematicWorld.getBlockState(abovePos).getBlock() instanceof BambooStalkBlock)) {
			return false;
		}
		BlockState clientAbove = clientWorld.getBlockState(abovePos);
		return clientAbove.isAir() || isReplaceable(clientAbove) || clientAbove.is(Blocks.BAMBOO);
	}

	private static boolean requiresMoreAction(BlockState stateSchematic, BlockState stateClient) {
		// Return true if current state requires more action to be taken
		Block blockSchematic = stateSchematic.getBlock();
		if (hasSegmentedHorizontalPlacement(stateSchematic) && stateClient.is(blockSchematic) && hasSegmentedHorizontalPlacement(stateClient)) {
			return stateClient.getValue(BlockStateProperties.HORIZONTAL_FACING) != stateSchematic.getValue(BlockStateProperties.HORIZONTAL_FACING)
				|| getSegmentAmount(stateClient) > getSegmentAmount(stateSchematic);
		}
		if (blockSchematic instanceof SeaPickleBlock && stateClient.getBlock() instanceof SeaPickleBlock) {
			int clientPickles = stateClient.getValue(SeaPickleBlock.PICKLES);
			int schematicPickles = stateSchematic.getValue(SeaPickleBlock.PICKLES);
			if (clientPickles < schematicPickles) {
				return false;
			}
			return clientPickles > schematicPickles
				|| stateClient.getValue(SeaPickleBlock.WATERLOGGED) != stateSchematic.getValue(SeaPickleBlock.WATERLOGGED);
		}
		if (blockSchematic instanceof SnowLayerBlock) {
			Block blockClient = stateClient.getBlock();

			if (blockClient instanceof SnowLayerBlock && stateClient.getValue(SnowLayerBlock.LAYERS) < stateSchematic.getValue(SnowLayerBlock.LAYERS)) {
				return false;
			}
			if (blockClient instanceof SnowLayerBlock && stateClient.getValue(SnowLayerBlock.LAYERS) > stateSchematic.getValue(SnowLayerBlock.LAYERS)) {
				return true;
			}
		}
		if (blockSchematic instanceof TurtleEggBlock && stateClient.getBlock() instanceof TurtleEggBlock) {
			int clientEggs = stateClient.getValue(TurtleEggBlock.EGGS);
			int schematicEggs = stateSchematic.getValue(TurtleEggBlock.EGGS);
			if (clientEggs < schematicEggs) {
				return false;
			}
			return clientEggs > schematicEggs;
		}
		if (blockSchematic instanceof SlabBlock && stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
			Block blockClient = stateClient.getBlock();

			if (blockClient instanceof SlabBlock && stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
				return blockSchematic != blockClient;
			}
		}
		if (blockSchematic instanceof ComposterBlock && stateSchematic.getValue(ComposterBlock.LEVEL) > 0 && stateClient.getBlock() instanceof ComposterBlock) {
			return stateClient.getValue(ComposterBlock.LEVEL) != stateSchematic.getValue(ComposterBlock.LEVEL);
		}
		Block blockClient = stateClient.getBlock();
		if (blockClient instanceof SnowLayerBlock && stateClient.getValue(SnowLayerBlock.LAYERS) < 3 && !(stateSchematic.getBlock() instanceof SnowLayerBlock)) {
			return false;
		}
		if (stateClient.is(Blocks.DIRT) && stateSchematic.is(Blocks.DIRT_PATH)) {
			return true;
		}
		// finally
		return !stateClient.isAir() && !isReplaceable(stateClient);
	}

	/**
	 * Apply hit vectors (used to be Carpet hit vec protocol, but I think it is
	 * uneccessary now with orientation/states programmed in)
	 *
	 * @param pos   BlockPos
	 * @param state BlockState
	 * @param side  random side
	 * @return Vec3d
	 */
	public static Vec3 applyHitVec(BlockPos pos, BlockState state, Direction side) {

		double dx;
		double dy;
		double dz;
		Block block = state.getBlock();

		if (block instanceof BellBlock) {
			return hitVecOnSide(pos, getBellPlacementSide(state));
		}
		if (block instanceof CeilingHangingSignBlock) {
			return hitVecOnSide(pos.above(), Direction.DOWN);
		}
		if (hasDripleafPlacementFacing(state)) {
			return hitVecOnSide(pos.below(), Direction.UP);
		}
		if (block instanceof SpeleothemBlock) {
			return hitVecOnSide(pos.relative(state.getValue(SpeleothemBlock.TIP_DIRECTION).getOpposite()), state.getValue(SpeleothemBlock.TIP_DIRECTION));
		}
		if (hasFrontAndTopOrientation(state)) {
			return hitVecOnSide(pos, getFrontAndTopPlacementSide(state));
		}

		/*
		 * I don't know if this is needed, just doing to mimic client According to the
		 * MC protocol wiki, the protocol expects a 1 on a side that is clicked
		 */
		Vec3 clickPos = Vec3.atLowerCornerOf(pos);
		if (!(block instanceof GrindstoneBlock) && block instanceof FaceAttachedHorizontalDirectionalBlock || block instanceof TorchBlock || block instanceof WallSkullBlock
			|| block instanceof LadderBlock
			|| block instanceof TripWireHookBlock || block instanceof WallSignBlock || block instanceof WallBannerBlock || block instanceof WallHangingSignBlock ||
			block instanceof EndRodBlock || block instanceof BaseCoralFanBlock ||
			block instanceof AmethystClusterBlock || block instanceof CocoaBlock) {
			if (block instanceof BaseCoralFanBlock && !(block instanceof BaseCoralWallFanBlock)) {
				side = Direction.UP;
				clickPos = hitVecOnSide(pos.below(), side);
			} else if (block instanceof TorchBlock && !(block instanceof WallTorchBlock) && !(block instanceof RedstoneWallTorchBlock)) {
				side = Direction.UP;
				clickPos = hitVecOnSide(pos.below(), side);
			} else if (side == null || state.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) != AttachFace.WALL) {
				if (state.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING) {
					side = Direction.DOWN;
				} else {
					side = Direction.UP;
				}
				clickPos = hitVecOnSide(pos, side);
			} else {
				clickPos = hitVecOnSide(pos, side);
			}
			//We are here because we can't use protocol.
		}
		dx = clickPos.x;
		dy = clickPos.y;
		dz = clickPos.z;
		if (block instanceof StairBlock) {
			return hitVecAtY(pos, state.getValue(StairBlock.HALF) == Half.TOP ? 0.75 : 0.25);
		} else if (block instanceof SlabBlock) {
			SlabType type = state.getValue(SlabBlock.TYPE);
			if (type == SlabType.DOUBLE && side != null && side.getAxis() == Direction.Axis.Y) {
				return hitVecOnSide(pos, side);
			}
			if (type != SlabType.DOUBLE) {
				return hitVecAtY(pos, type == SlabType.TOP ? 0.75 : 0.25);
			}
		} else if (block instanceof TrapDoorBlock) {
			return hitVecAtY(pos, state.getValue(TrapDoorBlock.HALF) == Half.TOP ? 0.75 : 0.25);
		} else if (block instanceof DoorBlock) {
			return getDoorHingeClickHitVec(pos, state);
		}
		return new Vec3(dx, dy, dz);
	}

	private static Vec3 getDoorHingeClickHitVec(BlockPos pos, BlockState state) {
		Direction facing = state.getValue(DoorBlock.FACING);
		boolean rightHinge = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
		double x = 0.5;
		double z = 0.5;
		if (facing == Direction.EAST) {
			z = rightHinge ? 0.75 : 0.25;
		} else if (facing == Direction.WEST) {
			z = rightHinge ? 0.25 : 0.75;
		} else if (facing == Direction.SOUTH) {
			x = rightHinge ? 0.25 : 0.75;
		} else if (facing == Direction.NORTH) {
			x = rightHinge ? 0.75 : 0.25;
		}
		return Vec3.atLowerCornerOf(pos).add(x, 0.5, z);
	}

	private static boolean canBypass(Minecraft mc, Level world, BlockPos pos) {
		BlockState observerState = world.getBlockState(pos);
		Direction direction = observerState.getValue(ObserverBlock.FACING);
		BlockPos posOffset = pos.relative(direction.getOpposite());
		BlockState schematicOffsetState = world.getBlockState(posOffset);
		BlockState clientOffsetState = mc.level.getBlockState(posOffset);
		return schematicOffsetState.isAir()
			|| !hasPowerRelatedState(clientOffsetState.getBlock()) && clientOffsetState.getBlock() == schematicOffsetState.getBlock();

	}

	private static boolean hasGui(Block checkGui) {
		return checkGui instanceof CraftingTableBlock || checkGui instanceof GrindstoneBlock || checkGui instanceof LeverBlock || checkGui instanceof TrapDoorBlock ||
			checkGui instanceof ButtonBlock || checkGui instanceof DoorBlock || checkGui instanceof FenceGateBlock ||
			checkGui instanceof BedBlock || checkGui instanceof NoteBlock || checkGui instanceof BaseEntityBlock;
	}

	private static boolean hasPowerRelatedState(Block block) {
		return block instanceof LeavesBlock || block instanceof LiquidBlock || block instanceof ObserverBlock || block instanceof PistonBaseBlock || block instanceof PoweredRailBlock || block instanceof DetectorRailBlock ||
			block instanceof DispenserBlock || block instanceof DiodeBlock || block instanceof LeverBlock || block instanceof TrapDoorBlock || block instanceof RedstoneTorchBlock ||
			block instanceof DoorBlock || block instanceof RedStoneWireBlock || block instanceof RedStoneOreBlock || block instanceof RedstoneLampBlock || block instanceof NoteBlock || block instanceof FenceGateBlock ||
			block instanceof ScaffoldingBlock || block instanceof CopperBulbBlock || block instanceof ShelfBlock || block instanceof LightningRodBlock ||
			block instanceof HopperBlock || block instanceof CrafterBlock || block instanceof TripWireBlock || block instanceof TripWireHookBlock || block instanceof DaylightDetectorBlock ||
			block instanceof BellBlock || block instanceof AbstractSkullBlock || block instanceof LecternBlock || block instanceof TargetBlock ||
			block instanceof ButtonBlock || block instanceof PressurePlateBlock || block instanceof WeightedPressurePlateBlock ||
			block instanceof BeehiveBlock || block instanceof CakeBlock || block instanceof ComposterBlock || block instanceof RespawnAnchorBlock;
	}

	/*
		returns if its block that can update neighbors
	 */
	private static BlockPos hasWrongStateNearbyPos(Minecraft mc, Level schematicWorld, BlockPos pos) {
		for (Direction direction : ALL_DIRECTIONS) {
			BlockPos checkPos = pos.relative(direction);
			BlockState schematicState = schematicWorld.getBlockState(checkPos);
			BlockState clientState = mc.level.getBlockState(checkPos);
			if (hasPowerRelatedState(schematicState.getBlock()) && !sameBlockState(schematicState, clientState)) {
				return checkPos;
			}
		}
		return null;
	}

	private static String wrongStateNearbyReason(Minecraft mc, Level schematicWorld, BlockPos checkPos) {
		BlockState schematicState = schematicWorld.getBlockState(checkPos);
		BlockState clientState = mc.level.getBlockState(checkPos);
		return "!" + checkPos.toShortString() + " STATE " + schematicState + " does not match with current state : " + clientState + "!";
	}

	public static Vec3 applyTorchHitVec(BlockPos pos, Vec3 hitVecIn, Direction side) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		double dx = hitVecIn.x();
		double dy = hitVecIn.y();
		double dz = hitVecIn.z();
		if (side == Direction.UP) {
			dy = 1;
		} else if (side == Direction.DOWN) {
			dy = -1;
		} else if (side == Direction.EAST) {
			dx = 1;
		} else if (side == Direction.WEST) {
			dx = -1;
		} else if (side == Direction.SOUTH) {
			dz = 1;
		} else if (side == Direction.NORTH) {
			dz = -1;
		}
		return new Vec3(x + dx, y + dy, z + dz);
	}

	private static void updateSignText(Minecraft mc, Level schematicWorld, BlockPos pos) {
		if (isPositionCached(pos, false)) {
			return;
		}
		if (mc.gui.screen() instanceof SignEditScreen || !schematicWorld.getBlockState(pos).is(BlockTags.SIGNS) || signCache.contains(pos.asLong())) {
			return;
		}
		BlockEntity entity = schematicWorld.getBlockEntity(pos);
		if (entity == null) {
			return;
		}
		BlockEntity clientEntity = mc.level.getBlockEntity(pos);
		if (clientEntity == null) {
			return;
		}
		//#if MC>=12000
		if (entity instanceof SignBlockEntity signBlockEntity && clientEntity instanceof SignBlockEntity clientSignEntity) {
			if (clientSignEntity.getText(false).getMessage(0, false).getContents() != PlainTextContents.EMPTY ||
				clientSignEntity.getText(false).getMessage(1, false).getContents() != PlainTextContents.EMPTY ||
				clientSignEntity.getText(false).getMessage(2, false).getContents() != PlainTextContents.EMPTY ||
				clientSignEntity.getText(false).getMessage(3, false).getContents() != PlainTextContents.EMPTY ) {
				MessageHolder.sendDebugMessage("Text already exists in " + pos.toShortString());
				signCache.add(pos.asLong());
				return;
			}
			MessageHolder.sendDebugMessage("Tries to copy sign text in " + pos.toShortString());
			signCache.add(pos.asLong());
			mc.getConnection().send(
				new ServerboundSignUpdatePacket(
					signBlockEntity.getBlockPos(),
					true,
					signBlockEntity.getText(false).getMessage(0, false).getString(),
					signBlockEntity.getText(false).getMessage(1, false).getString(),
					signBlockEntity.getText(false).getMessage(2, false).getString(),
					signBlockEntity.getText(false).getMessage(3, false).getString()
				)
			);
		}
		//#elseif MC>=11900
		//$$ if (entity instanceof SignBlockEntity signBlockEntity && clientEntity instanceof SignBlockEntity clientSignEntity) {
		//$$ 	if (clientSignEntity.getTextOnRow(0, false).getContent() != TextContent.EMPTY || clientSignEntity.getTextOnRow(1, false).getContent() != TextContent.EMPTY ||
		//$$ 		clientSignEntity.getTextOnRow(2, false).getContent() != TextContent.EMPTY ||
		//$$ 		clientSignEntity.getTextOnRow(3, false).getContent() != TextContent.EMPTY) {
		//$$ 		MessageHolder.sendDebugMessage("Text already exists in " + pos.toShortString());
		//$$ 		signCache.add(pos.asLong());
		//$$ 		return;
		//$$ 	}
		//$$ 	MessageHolder.sendDebugMessage("Tries to copy sign text in " + pos.toShortString());
		//$$ 	signCache.add(pos.asLong());
		//$$ 	mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(signBlockEntity.getPos(), signBlockEntity.getTextOnRow(0, false).getString(), signBlockEntity.getTextOnRow(1, false).getString(), signBlockEntity.getTextOnRow(2, false).getString(), signBlockEntity.getTextOnRow(3, false).getString()));
		//$$ }
		//#elseif MC>=11700
		//$$if (entity instanceof SignBlockEntity signBlockEntity && clientEntity instanceof SignBlockEntity clientSignEntity) {
		//$$	if (clientSignEntity.getTextOnRow(0, false) != LiteralText.EMPTY || clientSignEntity.getTextOnRow(1, false) != LiteralText.EMPTY ||
		//$$		clientSignEntity.getTextOnRow(2, false) != LiteralText.EMPTY ||
		//$$		clientSignEntity.getTextOnRow(3, false) != LiteralText.EMPTY) {
		//$$		MessageHolder.sendDebugMessage("Text already exists in " + pos.toShortString());
		//$$		signCache.add(pos.asLong());
		//$$		return;
		//$$	}
		//$$	MessageHolder.sendDebugMessage("Tries to copy sign text in " + pos.toShortString());
		//$$	signCache.add(pos.asLong());
		//$$	mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(signBlockEntity.getPos(), signBlockEntity.getTextOnRow(0, false).getString(), signBlockEntity.getTextOnRow(1, false).getString(), signBlockEntity.getTextOnRow(2, false).getString(), signBlockEntity.getTextOnRow(3, false).getString()));
		//$$}
		//#else
		//$$if (entity instanceof SignBlockEntity && clientEntity instanceof SignBlockEntity) {
		//$$	SignBlockEntity signBlockEntity = (SignBlockEntity) entity;
		//$$	SignBlockEntity clientSignEntity = (SignBlockEntity) clientEntity;
		//$$	if (clientSignEntity.getTextOnRow(0) != LiteralText.EMPTY || clientSignEntity.getTextOnRow(1) != LiteralText.EMPTY ||
		//$$		clientSignEntity.getTextOnRow(2) != LiteralText.EMPTY ||
		//$$		clientSignEntity.getTextOnRow(3) != LiteralText.EMPTY) {
		//$$		MessageHolder.sendDebugMessage("Text already exists in " + pos.toShortString());
		//$$		signCache.add(pos.asLong());
		//$$		return;
		//$$	}
		//$$	MessageHolder.sendDebugMessage("Tries to copy sign text in " + pos.toShortString());
		//$$	signCache.add(pos.asLong());
		//$$	mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(signBlockEntity.getPos(), signBlockEntity.getTextOnRow(0).getString(), signBlockEntity.getTextOnRow(1).getString(), signBlockEntity.getTextOnRow(2).getString(), signBlockEntity.getTextOnRow(3).getString()));
		//$$ }
		//#endif
	}

	/*
	 * Gets the direction necessary to build the block oriented correctly. TODO:
	 * Need a better way to do this.
	 */
	private static Boolean IsBlockSupportedCarpet(Block SchematicBlock) {
		if (SchematicBlock instanceof FaceAttachedHorizontalDirectionalBlock || SchematicBlock instanceof WallSkullBlock ||
			SchematicBlock instanceof BaseRailBlock || SchematicBlock instanceof TorchBlock || SchematicBlock instanceof BaseCoralFanBlock) {
			return false;
		}
		//#if MC>=12000
		return true;
		//#else
		//$$ return ADVANCED_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() || SchematicBlock instanceof GlazedTerracottaBlock || SchematicBlock instanceof ObserverBlock || SchematicBlock instanceof RepeaterBlock || SchematicBlock instanceof TrapdoorBlock ||
		//$$ 	SchematicBlock instanceof ComparatorBlock || SchematicBlock instanceof DispenserBlock || SchematicBlock instanceof PistonBlock || SchematicBlock instanceof StairsBlock;
		//#endif
	} //Current carpet extra does not handle other facingBlocks, gnembon please update it

	static Direction applyPlacementFacing(BlockState stateSchematic, Direction side, BlockState stateClient) {
		Block blockSchematic = stateSchematic.getBlock();
		Block blockClient = stateClient.getBlock();

		if (blockSchematic instanceof SlabBlock) {
			if (stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE && blockClient instanceof SlabBlock
				&& stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
				if (stateClient.getValue(SlabBlock.TYPE) == SlabType.TOP) {
					return Direction.DOWN;
				} else {
					return Direction.UP;
				}
			}
			// Single slab
			else {
				return Direction.NORTH;
			}
		} else if (/*blockSchematic instanceof LogBlock ||*/ blockSchematic instanceof RotatedPillarBlock) {
			return getPlacementSideForAxis(stateSchematic.getValue(RotatedPillarBlock.AXIS));
		} else if (stateSchematic.hasProperty(BlockStateProperties.AXIS)) {
			return getPlacementSideForAxis(stateSchematic.getValue(BlockStateProperties.AXIS));
		} else if (blockSchematic instanceof WallSignBlock) {
			return stateSchematic.getValue(WallSignBlock.FACING);
		} else if (blockSchematic instanceof WallBannerBlock) {
			return stateSchematic.getValue(WallBannerBlock.FACING);
		} else if (blockSchematic instanceof WallHangingSignBlock) {
			return getWallHangingSignPreferredSide(stateSchematic);
		} else if (blockSchematic instanceof CeilingHangingSignBlock) {
			return Direction.DOWN;
		} else if (hasDripleafPlacementFacing(stateSchematic)) {
			return Direction.UP;
		} else if (blockSchematic instanceof SpeleothemBlock) {
			return stateSchematic.getValue(SpeleothemBlock.TIP_DIRECTION);
		} else if (blockSchematic instanceof WallSkullBlock) {
			return stateSchematic.getValue(WallSkullBlock.FACING);
		} else if (blockSchematic instanceof StandingSignBlock) {
			return Direction.UP;
		} else if (hasFrontAndTopOrientation(stateSchematic)) {
			return getFrontAndTopPlacementSide(stateSchematic);
		} else if (blockSchematic instanceof BellBlock) {
			return getBellPlacementSide(stateSchematic);
		} else if (blockSchematic instanceof LanternBlock) {
			return getLanternPlacementSide(stateSchematic);
		} else if (blockSchematic instanceof FaceAttachedHorizontalDirectionalBlock) {
			return getFaceAttachedPlacementSide(stateSchematic);
		} else if (blockSchematic instanceof BaseCoralWallFanBlock) {
			return stateSchematic.getValue(BaseCoralWallFanBlock.FACING);
		} else if (blockSchematic instanceof BaseCoralFanBlock) {
			return Direction.UP;
		} else if (blockSchematic instanceof AmethystClusterBlock) {
			return stateSchematic.getValue(AmethystClusterBlock.FACING);
		} else if (blockSchematic instanceof CocoaBlock) {
			return stateSchematic.getValue(CocoaBlock.FACING).getOpposite();
		} else if (blockSchematic instanceof HopperBlock) {
			return stateSchematic.getValue(HopperBlock.FACING).getOpposite();
		//#if MC>=11700
		} else if (blockSchematic instanceof LightningRodBlock) {
			return stateSchematic.getValue(LightningRodBlock.FACING);
		//#endif
		}  else if (stateSchematic.is(BlockTags.SHULKER_BOXES)) {
			return stateSchematic.getValue(ShulkerBoxBlock.FACING);
		} else if (blockSchematic instanceof TorchBlock) {
			if (blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock) {
				return stateSchematic.getValue(WallTorchBlock.FACING);
			} else {
				return Direction.UP;
			}
		} else if (blockSchematic instanceof LadderBlock) {
			return stateSchematic.getValue(LadderBlock.FACING);
		} else if (blockSchematic instanceof TrapDoorBlock) {
			if (PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
				return Direction.UP; //Placement State fixing first
			}
			return stateSchematic.getValue(TrapDoorBlock.FACING);
		} else if (blockSchematic instanceof TripWireHookBlock) {
			return stateSchematic.getValue(TripWireHookBlock.FACING);
		} else if (blockSchematic instanceof EndRodBlock) {
			return stateSchematic.getValue(EndRodBlock.FACING);
		} else if (blockSchematic instanceof AnvilBlock) {
			if (ADVANCED_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() || PRINTER_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() && IsBlockSupportedCarpet(blockSchematic)) {
				return stateSchematic.getValue(AnvilBlock.FACING);
			}
			return stateSchematic.getValue(AnvilBlock.FACING).getCounterClockWise();
		} else if (blockSchematic instanceof BaseRailBlock) {
			return convertRailShapetoFace(stateSchematic);
		}

		// TODO: Add more for other blocks
		return side;
	}

	public static Direction convertRailShapetoFace(BlockState state) {
		RailShape railShape;
		if (state.getBlock() instanceof RailBlock) {
			railShape = state.getValue(RailBlock.SHAPE);
		} else {
			railShape = state.getValue(PoweredRailBlock.SHAPE);
		}
		return switch (railShape) {
			case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> Direction.EAST;
			default -> Direction.NORTH;
		};
	}

	private static boolean isCornerRailShape(RailShape railShape) {
		return switch (railShape) {
			case NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST -> true;
			default -> false;
		};
	}

	private static boolean isStraightRailShape(RailShape railShape) {
		return railShape == RailShape.EAST_WEST || railShape == RailShape.NORTH_SOUTH;
	}

	public static boolean isPositionCached(BlockPos pos, boolean useClicked) {
		long currentTime = System.nanoTime();
		if (positionCache.size() > 16 || currentTime - lastPositionCachePrune >= POSITION_CACHE_PRUNE_INTERVAL_NANOS) {
			pruneExpiredPositionCache(currentTime);
		}

		long posKey = pos.asLong();
		if (useClicked) {
			return isCachedPositionActive(new Tuple<>(posKey, true), currentTime, true);
		}

		return isCachedPositionActive(new Tuple<>(posKey, false), currentTime, false)
			|| isCachedPositionActive(new Tuple<>(posKey, true), currentTime, false);
	}

	public static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked) {
		cacheEasyPlacePosition(pos, useClicked, useClicked ? EASY_PLACE_CACHE_TIME.getIntegerValue() * 1_000_000L : DEFAULT_POSITION_CACHE_TIMEOUT_NANOS);
	}

	public static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked, int miliseconds) {
		cacheEasyPlacePosition(pos, useClicked, miliseconds * 1_000_000L);
	}

	private static boolean isCachedPositionActive(Tuple<Long, Boolean> entry, long currentTime, boolean requireClicked) {
		PositionCache value = positionCache.get(entry);
		if (value == null) {
			return false;
		}
		if (value.hasExpired(currentTime)) {
			positionCache.remove(entry);
			return false;
		}
		return !requireClicked || value.hasClicked;
	}

	private static void pruneExpiredPositionCache(long currentTime) {
		Iterator<Map.Entry<Tuple<Long, Boolean>, PositionCache>> iterator = positionCache.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue().hasExpired(currentTime)) {
				iterator.remove();
			}
		}
		lastPositionCachePrune = currentTime;
	}

	private static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked, long timeoutNanos) {
		PositionCache item = new PositionCache(pos, System.nanoTime(), timeoutNanos);
		// TODO: Create a separate cache for clickable items, as this just makes
		// duplicates
		if (useClicked) {
			item.hasClicked = true;
		}
		Tuple<Long, Boolean> entry = new Tuple<>(pos.asLong(), useClicked);
		PositionCache value = positionCache.get(entry);
		if (value == null || item.timeout > value.timeout) {
			positionCache.put(entry, item);
		}
	}

	public static Vec3 applyCarpetProtocolHitVec(BlockPos pos, BlockState state) {
		//#if MC>=11700
		if (Configs.Generic.EASY_PLACE_PROTOCOL.getOptionListValue() == EasyPlaceProtocol.V3) {
		//#else
		//$$ if (Configs.Generic.EASY_PLACE_PROTOCOL_V3.getBooleanValue()) {
		//#endif
			return EasyPlaceUtils.applyPlacementProtocolV3(pos, state, new Vec3(pos.getX(), pos.getY(), pos.getZ()));
		}
		Vec3 hitVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
		//#if MC >= 12000
		hitVec = EasyPlaceUtils.applyCarpetProtocolHitVec(pos, state, hitVec);
		return hitVec;
		//#else
		//$$ double code = 0;
		//$$ double y = pos.getY();
		//$$ double z = pos.getZ();
		//$$ Block block = state.getBlock();
		//$$ Direction facing = getSimplifiedFirstPropertyFacingValue(state);
		//$$ int railEnumCode = getRailShapeOrder(state);
		//$$ final int propertyIncrement = 16;
		//$$ if (facing == null && railEnumCode == 32 && !(block instanceof SlabBlock)) {
		//$$ 	return new Vec3d(pos.getX(), y, z);
		//$$ }
		//$$ if (facing != null) {
		//$$ 	code = facing.getId();
		//$$ } else if (railEnumCode != 32) {
		//$$ 	code = railEnumCode;
		//$$ }
		//$$ if (block instanceof RepeaterBlock) {
		//$$ 	code += ((state.get(RepeaterBlock.DELAY))) * (propertyIncrement);
		//$$ } else if (block instanceof TrapdoorBlock && state.get(TrapdoorBlock.HALF) == BlockHalf.TOP) {
		//$$ 	code += propertyIncrement;
		//$$ } else if (block instanceof ComparatorBlock && state.get(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT) {
		//$$ 	code += propertyIncrement;
		//$$ } else if (block instanceof StairsBlock && state.get(StairsBlock.HALF) == BlockHalf.TOP) {
		//$$ 	code += propertyIncrement;
		//$$ } else if (block instanceof SlabBlock && state.get(SlabBlock.TYPE) != SlabType.DOUBLE) {
		//$$ 	if (state.get(SlabBlock.TYPE) == SlabType.TOP) //side should not be down
		//$$ 	{
		//$$ 		y += 0.99;
		//$$ 		//code += propertyIncrement; //slab type by protocol soon?
		//$$ 	}
		//$$ }
		//$$ if (code >= 0) {
		//$$ 	return new Vec3d(code * 2 + 2 + pos.getX(), y, z);
		//$$ }
		//$$ hitVec = new Vec3d(pos.getX(), y, z);
		//$$ return hitVec;
		//#endif
	}

	public static Integer getRailShapeOrder(BlockState state) {
		Block stateBlock = state.getBlock();
		if (stateBlock instanceof BaseRailBlock) {
			if (stateBlock instanceof RailBlock) {
				return state.getValue(RailBlock.SHAPE).ordinal();
			} else if (stateBlock instanceof DetectorRailBlock) {
				return state.getValue(DetectorRailBlock.SHAPE).ordinal();
			} else {
				return state.getValue(PoweredRailBlock.SHAPE).ordinal();
			}
		} else {
			return 32;
		}
	}


	public static class PositionCache {
		private final BlockPos pos;
		private final long timeout;
		public boolean hasClicked = false;

		private PositionCache(BlockPos pos, long time, long timeout) {
			this.pos = pos;
			this.timeout = time + timeout;
		}

		public BlockPos getPos() {
			return this.pos;
		}

		public boolean hasExpired(long currentTime) {
			return currentTime > this.timeout;
		}
	}
}
