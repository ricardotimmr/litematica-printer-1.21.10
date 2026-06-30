package io.github.eatmyvenom.litematicin.mixin.quasiEssentialClient;

import com.mojang.authlib.GameProfile;
import io.github.eatmyvenom.litematicin.utils.FakeAccurateBlockPlacement;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.github.eatmyvenom.litematicin.utils.FakeAccurateBlockPlacement.shouldModifyValues;

//see https://github.com/senseiwells/EssentialClient/blob/1.19.x/src/main/java/me/senseiwells/essentialclient/mixins/betterAccurateBlockPlacement/ClientPlayerEntityMixin.java for reference!!

@Mixin(value = LocalPlayer.class, priority = 1200)
public abstract class ClientPlayerEntityMixin extends Player {

	//#if MC<11900
	//$$ public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
	//$$ 	super(world, pos, yaw, gameProfile);
	//$$ }
	//#elseif MC==11903
	//$$	public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
	//$$		super(world, pos, yaw, gameProfile);
	//$$	}
	//#elseif MC>= 12000
		public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
			super(world, gameProfile);
			this.setPos(pos.getX(), pos.getY(), pos.getZ());
			this.setYRot(yaw);
		}
	//#else
	//$$public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
	//$$	super(world, pos, yaw, gameProfile, publicKey);
	//$$}
	//#endif


	private boolean canSendPacketNormally() {
		// if FakeAccurateBlockPlacement is active, then return false
		return !shouldModifyValues();
	}

	private ServerboundMovePlayerPacket.PosRot createFakePosRotPacket() {
		return new ServerboundMovePlayerPacket.PosRot(
			this.getX(), this.isPassenger()? -999.0D : this.getY(), this.getZ(),
			FakeAccurateBlockPlacement.fakeYaw,
			FakeAccurateBlockPlacement.fakePitch,
			this.onGround(),
			this.horizontalCollision
		);
	}

	private ServerboundMovePlayerPacket.Rot createFakeRotPacket() {
		return new ServerboundMovePlayerPacket.Rot(
			FakeAccurateBlockPlacement.fakeYaw,
			FakeAccurateBlockPlacement.fakePitch,
			this.onGround(),
			this.horizontalCollision
		);
	}

	//#if MC>=12102
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1), require = 0)
	//#elseif MC>=11904
	//$$ @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 1), require = 0)
	//#else
	//$$@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 2), require = 0)
	//#endif
	private void onSendPacketVehicle(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
		// replaces all packets with a fake packet if PRINTER_SUPPRESS_PACKETS is true
		if (canSendPacketNormally()) {
			clientPlayNetworkHandler.send(packet);
			return;
		}
		clientPlayNetworkHandler.send(createFakeRotPacket());
	}

	//#if MC>=12102
	@Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), require = 0)
	//#elseif MC>=11904
	//$$ @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 2), require = 0)
	//#else
	//$$@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 3), require = 0)
	//#endif
	private void onSendPacketFull(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
		if (canSendPacketNormally()) {
			clientPlayNetworkHandler.send(packet);
			return;
		}
		clientPlayNetworkHandler.send(createFakePosRotPacket());
	}

	//#if MC>=12102
	@Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1), require = 0)
	private void onSendPacketPositionOnly(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
		if (canSendPacketNormally()) {
			clientPlayNetworkHandler.send(packet);
			return;
		}
		clientPlayNetworkHandler.send(createFakePosRotPacket());
	}

	//#if MC>=12102
	@Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 2), require = 0)
	//#elseif MC>=11904
	//$$ @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 4), require = 0)
	//#else
	//$$@Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 5), require = 0)
	//#endif
	private void onSendPacketLookAndOnGround(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
		if (canSendPacketNormally()) {
			clientPlayNetworkHandler.send(packet);
			return;
		}
		clientPlayNetworkHandler.send(createFakeRotPacket());
	}

	//#if MC>=12102
	@Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 3), require = 0)
	private void onSendPacketStatusOnly(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
		if (canSendPacketNormally()) {
			clientPlayNetworkHandler.send(packet);
			return;
		}
		clientPlayNetworkHandler.send(createFakeRotPacket());
	}
}
