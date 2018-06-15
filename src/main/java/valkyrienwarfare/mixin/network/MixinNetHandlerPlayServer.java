/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.network;

import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.VWMath;
import valkyrienwarfare.mod.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.mod.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

//TODO: a lot of these mixins can probably be done using overrides instead of overwrites, i should have a look at some point
@Mixin(value = NetHandlerPlayServer.class, priority = 5)
public abstract class MixinNetHandlerPlayServer {

	@Shadow
	@Final
	public static Logger LOGGER;
	@Shadow
	public EntityPlayerMP player;
	@Shadow
	@Final
	public MinecraftServer serverController;
	@Shadow
	public int networkTickCount;
	@Shadow
	public Vec3d targetPos;
	@Shadow
	public int lastPositionUpdate;
	@Shadow
	public double firstGoodX;
	@Shadow
	public double firstGoodY;
	@Shadow
	public double firstGoodZ;
	@Shadow
	public double lastGoodX;
	@Shadow
	public double lastGoodY;
	@Shadow
	public double lastGoodZ;
	@Shadow
	public int movePacketCounter;
	@Shadow
	public int lastMovePacketCounter;
	@Shadow
	public boolean floating;

	private double dummyBlockReachDist = 9999999999999999999999999999D;
	private double lastGoodBlockReachDist;
	// Thanks java
	private NetHandlerPlayServer thisAsNetHandler = NetHandlerPlayServer.class.cast(this);

	private boolean redirectingSetPlayerLocation = false;

	/**
	 * Inject this before the processPlayer() method gets called to move the player
	 * and to transform the position of the packet as well.
	 * 
	 * @param packetIn
	 * @param info
	 */
	/*
	@Inject(method = "processPlayer", at = @At("HEAD"))
	private void preProcessPlayer(CPacketPlayer packetIn, CallbackInfo info) {
		WorldServer worldserver = this.serverController.getWorld(this.player.dimension);
		IThreadListener listener = worldserver;
		if (listener.isCallingFromMinecraftThread()) {
			PhysicsWrapperEntity worldBelow = IDraggable.class.cast(this.player).getForcedSubspaceBelowFeet();
			if (worldBelow != null) {
				ISubspacedEntityRecord record = worldBelow.getPhysicsObject().getSubspace()
						.getRecordForSubspacedEntity(ISubspacedEntity.class.cast(this.player));
				if (record != null) {
					System.out.println("Test");
					VectorImmutable position = record.getPositionInGlobalCoordinates();
					VectorImmutable lookDirection = record.getPositionInGlobalCoordinates();
					VectorImmutable velocity = record.getVelocityInGlobalCoordinates();
					// ===== HACKY STUFF STARTS HERE =====
					this.firstGoodX = this.lastGoodX = position.getX();
					this.firstGoodY = this.lastGoodY = position.getY();
					this.firstGoodZ = this.lastGoodZ = position.getZ();

					float pitch = (float) VWMath.getPitchFromVectorImmutable(lookDirection);
					float yaw = (float) VWMath.getYawFromVectorImmutable(lookDirection, pitch);

					this.player.setPositionAndRotation(position.getX(), position.getY(), position.getZ(), yaw, pitch);

					this.player.motionX = velocity.getX();
					this.player.motionY = velocity.getY();
					this.player.motionZ = velocity.getZ();

					// ===== NOW FIX THE PACKET =====
					// CPacketPlayer fixed = new CPacketPlayer();
					packetIn.x = position.getX();
					packetIn.y = position.getY();
					packetIn.z = position.getZ();
					packetIn.moving = true;
					packetIn.onGround = true;
					packetIn.pitch = pitch;
					packetIn.yaw = yaw;
					
					IDraggable.class.cast(this.player).setForcedRelativeSubspace(null);
				}
			}
		}
	}
	*/

	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;captureCurrentPosition()V"))
	public void makeIDraggableNotUseless(NetHandlerPlayServer server) {
		IDraggable draggable = EntityDraggable.getDraggableFromEntity(player);
		server.captureCurrentPosition();

		server.firstGoodX += draggable.getVelocityAddedToPlayer().X;
		server.firstGoodY += draggable.getVelocityAddedToPlayer().Y;
		server.firstGoodZ += draggable.getVelocityAddedToPlayer().Z;
		server.lastGoodX += draggable.getVelocityAddedToPlayer().X;
		server.lastGoodY += draggable.getVelocityAddedToPlayer().Y;
		server.lastGoodZ += draggable.getVelocityAddedToPlayer().Z;
	}

	@Shadow
	public void disconnect(final ITextComponent textComponent) {
	}

	@Shadow
	public void captureCurrentPosition() {
	}

	@Shadow
	public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
	}

	// TODO: Theres probably a smarter way of doing this using proxies and
	// callbacks. But ain't nobody got time for that!
	/**
	 * Fixes things such that when mods try to teleport players into the ship space,
	 * VW will either redirect the teleport or block it. Looking at you
	 * SimpleTeleporters mod >:/
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 * @param relativeSet
	 * @param callbackInfo
	 */
	@Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At("HEAD"), cancellable = true)
	public void onSetPlayerLocation(double x, double y, double z, float yaw, float pitch,
			Set<SPacketPlayerPosLook.EnumFlags> relativeSet, CallbackInfo callbackInfo) {
		if (!redirectingSetPlayerLocation) {
			BlockPos pos = new BlockPos(x, y, z);
			// If the player is being teleported to ship space then we have to stop it.
			if (PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
				callbackInfo.cancel();
				redirectingSetPlayerLocation = true;
				World world = player.getEntityWorld();
				PhysicsWrapperEntity physicsEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(world,
						pos);
				if (physicsEntity != null) {
					Vector tpPos = new Vector(x, y, z);
					physicsEntity.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform()
							.transform(tpPos, TransformType.SUBSPACE_TO_GLOBAL);
					// Now call this again with the transformed position.
					// player.sendMessage(new TextComponentString("Transformed the player tp from <"
					// + x + ":" + y + ":" + z + "> to" + tpPos));
					thisAsNetHandler.setPlayerLocation(tpPos.X, tpPos.Y, tpPos.Z, yaw, pitch, relativeSet);
				} else {
					player.sendMessage(new TextComponentString(
							"Tried teleporting you to an unloaded ship; teleportation canceled."));
				}
				redirectingSetPlayerLocation = false;
			}
		}
	}

}
