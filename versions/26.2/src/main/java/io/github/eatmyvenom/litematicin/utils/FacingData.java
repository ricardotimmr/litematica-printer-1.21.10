package io.github.eatmyvenom.litematicin.utils;


import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DriedGhastBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class FacingData{
	public int type;
	public boolean isReversed;

	public FacingData (int type, boolean isReversed) {
			this.type = type;
			this.isReversed = isReversed;
	}
	private static final Map<Class<? extends Block>, FacingData> facingMap = new LinkedHashMap<>();
	private static final Map<Class<? extends Block>, FacingData> resolvedFacingMap = new LinkedHashMap<>();
	private static final HashSet<Class<? extends Block>> missingFacingData = new HashSet<>();
	private static boolean setupFacing = false;

	private static void setUpFacingData() {
		setupFacing = true;

		/*
		 * 0 = Normal up/down/east/west/south/north directions 1 = Horizontal directions
		 * 2 = Wall Attactchable block
		 * 5 = Bell, horizontal player facing only matters for floor/ceiling attachment
		 *
		 *
		 * TODO: THIS CODE MUST BE CLEANED UP.
		 */

		// All directions, reverse of what player is facing
		addFD(PistonBaseBlock.class, new FacingData(0, true));
		addFD(DispenserBlock.class, new FacingData(0, true));
		addFD(DropperBlock.class, new FacingData(0, true));
		addFD(CommandBlock.class, new FacingData(0, true));

		// All directions, normal direction of player
		addFD(ObserverBlock.class, new FacingData(0, false));
		// Horizontal directions, normal direction
		addFD(StairBlock.class, new FacingData(1, false));
		addFD(DoorBlock.class, new FacingData(1, false));
		addFD(BedBlock.class, new FacingData(1, false));
		addFD(FenceGateBlock.class, new FacingData(1, false));
		addFD(DecoratedPotBlock.class, new FacingData(1, false));
		addFD(CalibratedSculkSensorBlock.class, new FacingData(1, false));
		addFD(CocoaBlock.class, new FacingData(1, false));
		addFD(CampfireBlock.class, new FacingData(1, false));

		// Horizontal directions, reverse of what player is facing
		addFD(TrapDoorBlock.class, new FacingData(1, true)); //actually it is used when side is not horizontal
		addFD(BarrelBlock.class, new FacingData(0, true));
		addFD(ChestBlock.class, new FacingData(1, true));
		addFD(RepeaterBlock.class, new FacingData(1, true));
		addFD(ComparatorBlock.class, new FacingData(1, true));
		addFD(EnderChestBlock.class, new FacingData(1, true));
		addFD(AbstractFurnaceBlock.class, new FacingData(1, true));
		addFD(GlazedTerracottaBlock.class, new FacingData(1, true));
		addFD(LecternBlock.class, new FacingData(1, true));
		addFD(LoomBlock.class, new FacingData(1, true));
		addFD(BeehiveBlock.class, new FacingData(1, true));
		addFD(StonecutterBlock.class, new FacingData(1, true));
		addFD(CarvedPumpkinBlock.class, new FacingData(1, true));
		addFD(PumpkinBlock.class, new FacingData(1, true));
		addFD(EndPortalFrameBlock.class, new FacingData(1, true));
		addFD(VaultBlock.class, new FacingData(1, true));
		addFD(ChiseledBookShelfBlock.class, new FacingData(1, true));
		addFD(ShelfBlock.class, new FacingData(1, true));
		addFD(DriedGhastBlock.class, new FacingData(1, true));
		addFD(CopperGolemStatueBlock.class, new FacingData(1, true));
		addFD(FlowerBedBlock.class, new FacingData(1, true));
		addFD(LeafLitterBlock.class, new FacingData(1, true));
		addFD(LadderBlock.class, new FacingData(1, true));
		addFD(WallSignBlock.class, new FacingData(1, true));
		addFD(WallBannerBlock.class, new FacingData(1, true));
		addFD(WallHangingSignBlock.class, new FacingData(1, true));
		addFD(WallSkullBlock.class, new FacingData(1, true));
		addFD(BaseCoralWallFanBlock.class, new FacingData(1, true));
		addFD(TripWireHookBlock.class, new FacingData(1, true));

		// Top/bottom placable side mountable blocks
		addFD(LeverBlock.class, new FacingData(2, false));
		addFD(ButtonBlock.class, new FacingData(2, false));
		//addFD(WallTorchBlock.class, new FacingData(2, false));
		addFD(BellBlock.class, new FacingData(5, false));
		addFD(GrindstoneBlock.class, new FacingData(2, true));

		// Anvils
		addFD(AnvilBlock.class, new FacingData(3, true));
		// Rails
		addFD(BaseRailBlock.class, new FacingData(4, false));
	}
	private static void addFD(final Class<? extends Block> c, FacingData data) {
		facingMap.put(c, data);
	}

	public static FacingData getFacingData(BlockState state) {
		if (!setupFacing) {
			setUpFacingData();
		}
		Block block = state.getBlock();
		Class<? extends Block> blockClass = block.getClass();
		FacingData cached = resolvedFacingMap.get(blockClass);
		if (cached != null) {
			return cached;
		}
		if (missingFacingData.contains(blockClass)) {
			return null;
		}
		for (Map.Entry<Class<? extends Block>, FacingData> entry : facingMap.entrySet()) {
			if (entry.getKey().isInstance(block)) {
				FacingData data = entry.getValue();
				resolvedFacingMap.put(blockClass, data);
				return data;
			}
		}
		missingFacingData.add(blockClass);
		return null;
	}
}
