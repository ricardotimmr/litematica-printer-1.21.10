package io.github.eatmyvenom.litematicin.mixin.MinecraftClient;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
	@Accessor("stateId")
	void litematicin$setStateId(int stateId);
}
