package io.github.eatmyvenom.litematicin.utils;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//see IMixinConfigPlugin
public class MixinConfigPlugin implements IMixinConfigPlugin {
	private static final String ESSENTIAL_CLIENT_MOD_ID = "essential-client";
	private static final String ESSENTIAL_CLIENT_FAKE_LOOK_TARGET = "me.senseiwells.essentialclient.feature.BetterAccurateBlockPlacement";
	private static final String ESSENTIAL_CLIENT_FAKE_LOOK_RESOURCE = ESSENTIAL_CLIENT_FAKE_LOOK_TARGET.replace('.', '/') + ".class";
	private static final String ACCURATE_BLOCK_PLACEMENT_DESC = "(Lnet/minecraft/client/Minecraft;)V";

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		List<String> fakeLookMixins = Lists.newArrayList(
			"io.github.eatmyvenom.litematicin.mixin.EssentialClient.FakeLookMixin"
		);
		List<String> desyncOptions = Lists.newArrayList(
			"io.github.eatmyvenom.litematicin.mixin.MinecraftClient.AbstractContainerMenuAccessor",
			"io.github.eatmyvenom.litematicin.mixin.MinecraftClient.ClientCommonPacketListenerImplMixin",
			"io.github.eatmyvenom.litematicin.mixin.MinecraftClient.ClientPlayerInteractionManagerMixin",
			"io.github.eatmyvenom.litematicin.mixin.MinecraftClient.ClientPlayNetworkHandlerMixin"
		);

		if (fakeLookMixins.contains(mixinClassName)) {
			return hasEssentialClientFakeLookTarget();
		}
		if (desyncOptions.contains(mixinClassName)) {
			Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("matchrevisions");
			return !container.isPresent(); // Java 8 compatibility
		}
		return true;
	}

	private boolean hasEssentialClientFakeLookTarget() {
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(ESSENTIAL_CLIENT_MOD_ID);
		if (container.isEmpty()) {
			return false;
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = MixinConfigPlugin.class.getClassLoader();
		}

		try (InputStream stream = classLoader.getResourceAsStream(ESSENTIAL_CLIENT_FAKE_LOOK_RESOURCE)) {
			if (stream == null) {
				return false;
			}

			ClassNode classNode = new ClassNode();
			new ClassReader(stream).accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return hasMethod(classNode, "accurateBlockPlacementOnPress", ACCURATE_BLOCK_PLACEMENT_DESC)
				&& hasField(classNode, "fakeDirection")
				&& hasField(classNode, "requestedTicks")
				&& hasField(classNode, "fakeYaw")
				&& hasField(classNode, "fakePitch");
		} catch (IOException | RuntimeException ignored) {
			return false;
		}
	}

	private boolean hasMethod(ClassNode classNode, String name, String descriptor) {
		for (MethodNode method : classNode.methods) {
			if (method.name.equals(name) && method.desc.equals(descriptor)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasField(ClassNode classNode, String name) {
		for (FieldNode field : classNode.fields) {
			if (field.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
