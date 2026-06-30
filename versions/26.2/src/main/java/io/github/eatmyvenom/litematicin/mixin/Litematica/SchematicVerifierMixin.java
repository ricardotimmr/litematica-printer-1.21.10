package io.github.eatmyvenom.litematicin.mixin.Litematica;

import java.util.HashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import com.google.common.collect.ArrayListMultimap;
import io.github.eatmyvenom.litematicin.LitematicaMixinMod;
import io.github.eatmyvenom.litematicin.utils.Printer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.WorldSchematic;

@Mixin(SchematicVerifier.class)
public class SchematicVerifierMixin {
	@Shadow
	private WorldSchematic worldSchematic;

	@Shadow
	@Final
	private ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> wrongStatesPositions;

	@Shadow
	@Final
	private static MutablePair<BlockState, BlockState> MUTABLE_PAIR;

	@Shadow
	@Final
	private HashSet<Pair<BlockState, BlockState>> ignoredMismatches;

	@Shadow
	@Final
	private Object2ObjectOpenHashMap<BlockPos, SchematicVerifier.BlockMismatch> blockMismatches;

	@Shadow
	private ClientLevel worldClient;

	@Inject(method = "checkBlockStates", at = @At("HEAD"), cancellable = true)
	private void handleInventory(int x, int y, int z, BlockState stateSchematic, BlockState stateClient, CallbackInfo ci) {
		if (!LitematicaMixinMod.VERIFY_INVENTORY.getBooleanValue()) {
			return;
		}
		MUTABLE_PAIR.setLeft(stateSchematic);
		MUTABLE_PAIR.setRight(stateClient);
		if (!this.ignoredMismatches.contains(MUTABLE_PAIR)) {
			if (Printer.sameBlockState(stateSchematic, stateClient)) {
				WorldSchematic schematic = this.worldSchematic;
				BlockPos pos = new BlockPos(x, y, z);
				BlockEntity entity = schematic.getBlockEntity(pos);
				BlockEntity clientEntity = this.worldClient.getBlockEntity(pos);
				if (hasMismatchingContainerItems(entity, clientEntity)) {
					SchematicVerifier.BlockMismatch mismatch = new SchematicVerifier.BlockMismatch(SchematicVerifier.MismatchType.WRONG_STATE, stateSchematic, stateClient, 1);
					this.wrongStatesPositions.put(Pair.of(stateSchematic, stateClient), new BlockPos(x, y, z));
					this.blockMismatches.put(pos, mismatch);
					ItemUtils.setItemForBlock(this.worldClient, pos, stateClient);
					ItemUtils.setItemForBlock(this.worldSchematic, pos, stateSchematic);
					ci.cancel();
				}
			}
		}
	}

	private static boolean hasMismatchingContainerItems(BlockEntity schematicEntity, BlockEntity clientEntity) {
		if (!(schematicEntity instanceof Container schematicContainer)) {
			return clientEntity instanceof Container clientContainer && !clientContainer.isEmpty();
		}
		if (!(clientEntity instanceof Container clientContainer)) {
			return !schematicContainer.isEmpty();
		}
		if (schematicContainer.getContainerSize() != clientContainer.getContainerSize()) {
			return true;
		}
		for (int i = 0; i < schematicContainer.getContainerSize(); i++) {
			if (!ItemStack.matches(schematicContainer.getItem(i), clientContainer.getItem(i))) {
				return true;
			}
		}
		return false;
	}
}

