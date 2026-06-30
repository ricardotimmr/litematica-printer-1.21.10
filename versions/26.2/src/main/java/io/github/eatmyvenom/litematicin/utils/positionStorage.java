package io.github.eatmyvenom.litematicin.utils;

//#if MC<11700
//$$ import java.util.stream.Collectors;
//#endif
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class positionStorage {
	private final static Map<Long, Boolean> positionMap = new LinkedHashMap<>();
	private final static Map<Long, Direction> cleanupDirectionMap = new LinkedHashMap<>();

	public static void clear() {
		positionMap.clear();
		cleanupDirectionMap.clear();
	}

	public static boolean hasPos(Level world, BlockPos pos) {
		long key = pos.asLong();
		if (!Boolean.TRUE.equals(positionMap.get(key))) {
			return false;
		}
		if (!match(world.getBlockState(pos).getBlock())) {
			positionMap.remove(key);
			cleanupDirectionMap.remove(key);
			return false;
		}
		return true;
	}

	public static void registerPos(BlockPos pos, boolean val) {
		registerPos(pos, val, Direction.UP);
	}

	public static void registerPos(BlockPos pos, boolean val, Direction cleanupDirection) {
		long key = pos.asLong();
		if (!val) {
			positionMap.remove(key);
			cleanupDirectionMap.put(key, cleanupDirection);
		} else {
			cleanupDirectionMap.remove(key);
		}
		positionMap.put(key, val);
	}

	public static void refresh(Level world) {
		Iterator<Map.Entry<Long, Boolean>> iterator = positionMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Boolean> entry = iterator.next();
			if (!match(world.getBlockState(BlockPos.of(entry.getKey())).getBlock())) {
				iterator.remove();
				cleanupDirectionMap.remove(entry.getKey());
			}
		}
	}

	public static Direction getCleanupDirection(BlockPos pos) {
		return cleanupDirectionMap.getOrDefault(pos.asLong(), Direction.UP);
	}

	private static boolean match(Block block) {
		return block == Blocks.PISTON || block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_WALL_TORCH || block == Blocks.SLIME_BLOCK;
	}

	public static ArrayList<BlockPos> getFalseMarkedHasBlockPosInAttackRange(Level world, Vec3 pos, int attackRange) {
		return getFalseMarkedHasBlockPosInAttackRange(world, pos, attackRange, Integer.MAX_VALUE);
	}

	public static ArrayList<BlockPos> getFalseMarkedHasBlockPosInAttackRange(Level world, Vec3 pos, int attackRange, int limit) {
		if (limit <= 0 || positionMap.isEmpty()) {
			return new ArrayList<>();
		}
		int initialCapacity = Math.min(Math.min(limit, positionMap.size()), 16);
		ArrayList<BlockPos> falseMarkedList = new ArrayList<>(initialCapacity);
		ArrayList<BlockPos> delayedPistons = null;
		Iterator<Map.Entry<Long, Boolean>> iterator = positionMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, Boolean> entry = iterator.next();
			BlockPos blockPos = BlockPos.of(entry.getKey());
			Block block = world.getBlockState(blockPos).getBlock();
			if (!match(block)) {
				iterator.remove();
				cleanupDirectionMap.remove(entry.getKey());
				continue;
			}
			if (entry.getValue() || falseMarkedList.size() >= limit) {
				continue;
			}
			if (blockPos.closerToCenterThan(pos, attackRange)) {
				if (block == Blocks.PISTON) {
					if (delayedPistons == null) {
						delayedPistons = new ArrayList<>(initialCapacity);
					}
					if (delayedPistons.size() < limit) {
						delayedPistons.add(blockPos);
					}
				} else {
					falseMarkedList.add(blockPos);
				}
			}
		}
		if (delayedPistons != null) {
			for (BlockPos delayedPiston : delayedPistons) {
				if (falseMarkedList.size() >= limit) {
					break;
				}
				falseMarkedList.add(delayedPiston);
			}
		}
		return falseMarkedList;
	}
}
