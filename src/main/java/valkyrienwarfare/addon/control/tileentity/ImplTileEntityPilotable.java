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

package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.network.MessageStartPiloting;
import valkyrienwarfare.addon.control.network.MessageStopPiloting;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.ITileEntityPilotable;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * A basic implementation of the ITileEntityPilotable interface, other tile
 * entities can extend this for easy controls
 *
 * @author thebest108
 */
public abstract class ImplTileEntityPilotable extends BasicNodeTileEntity implements ITileEntityPilotable {

	private EntityPlayer pilotPlayerEntity;

	@Override
	public final void onPilotControlsMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		if (sender == pilotPlayerEntity) {
			processControlMessage(message, sender);
		} else {
			// Wtf is this packet being sent for?
		}
	}

	@Override
	public final EntityPlayer getPilotEntity() {
		return pilotPlayerEntity;
	}

	@Override
	public final void setPilotEntity(EntityPlayer toSet) {
		if (!getWorld().isRemote) {
			sendPilotUpdatePackets((EntityPlayerMP) toSet, (EntityPlayerMP) pilotPlayerEntity);
		}
		pilotPlayerEntity = toSet;
		if (pilotPlayerEntity != null) {
			onStartTileUsage(pilotPlayerEntity);
		} else {
			onStopTileUsage();
		}
	}

	@Override
	public final void playerWantsToStopPiloting(EntityPlayer player) {
		if (player == getPilotEntity()) {
			setPilotEntity(null);
		} else {
			// Wtf happened here?
		}
	}

	@Override
	public final PhysicsWrapperEntity getParentPhysicsEntity() {
		return ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
	}

	// Always call this before setting the pilotPlayerEntity to equal newPilot
	private final void sendPilotUpdatePackets(EntityPlayerMP newPilot, EntityPlayerMP oldPilot) {
		if (oldPilot != null) {
			MessageStopPiloting stopMessage = new MessageStopPiloting(getPos());
			ValkyrienWarfareControl.controlNetwork.sendTo(stopMessage, oldPilot);
		}
		if (newPilot != null) {
			MessageStartPiloting startMessage = new MessageStartPiloting(getPos(), setClientPilotingEntireShip(),
					getControlInputType());
			ValkyrienWarfareControl.controlNetwork.sendTo(startMessage, newPilot);
		}
	}

	/**
	 * Unique for each tileentity type
	 *
	 * @return
	 */
	abstract ControllerInputType getControlInputType();

	/**
	 * Returns true if this control type is piloting the ship.
	 *
	 * @return
	 */
	@Deprecated
	boolean setClientPilotingEntireShip() {
		return false;
	}

	/**
	 * Unique for each tileentity type, only called if the sender player is the same
	 * as the pilotPlayerEntity
	 *
	 * @return
	 */
	abstract void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender);

	final void sendUpdatePacketToAllNearby() {
		SPacketUpdateTileEntity spacketupdatetileentity = getUpdatePacket();
		WorldServer serverWorld = (WorldServer) world;
		Vector pos = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
		if (wrapper != null) {
			wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(pos,
					TransformType.SUBSPACE_TO_GLOBAL);
			// RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
			// pos);
		}
		serverWorld.mcServer.getPlayerList().sendToAllNearExcept(null, pos.X, pos.Y, pos.Z, 128D,
				getWorld().provider.getDimension(), spacketupdatetileentity);
	}

	/**
	 * 
	 * @param player
	 * @param blockFacing
	 * @return true if the passed player is in front of the given blockFacing, false if not.
	 */
	protected boolean isPlayerInFront(EntityPlayer player, EnumFacing blockFacing) {
		Vector tileRelativePos = new Vector(this.getPos().getX() + .5, this.getPos().getY() + .5,
				this.getPos().getZ() + .5);
		if (this.getParentPhysicsEntity() != null) {
			this.getParentPhysicsEntity().getPhysicsObject().getShipTransformationManager().getCurrentTickTransform()
					.transform(tileRelativePos, TransformType.SUBSPACE_TO_GLOBAL);
		}
		tileRelativePos.subtract(player.posX, player.posY, player.posZ);
		Vector normal = new Vector(blockFacing.getDirectionVec().getX() * -1, blockFacing.getDirectionVec().getY(),
				blockFacing.getDirectionVec().getZ());

		if (this.getParentPhysicsEntity() != null) {
			this.getParentPhysicsEntity().getPhysicsObject().getShipTransformationManager().getCurrentTickTransform()
					.rotate(normal, TransformType.SUBSPACE_TO_GLOBAL);
		}

		double dotProduct = tileRelativePos.dot(normal);
		return dotProduct > 0;
	}

}
