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

import gigaherz.graph.api.GraphObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.BlocksValkyrienWarfareControl;
import valkyrienwarfare.addon.control.ValkyrienWarfareControl;
import valkyrienwarfare.addon.control.block.BlockShipHelm;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityEthereumCompressorPart;
import valkyrienwarfare.addon.control.block.multiblocks.TileEntityRudderAxlePart;
import valkyrienwarfare.addon.control.nodenetwork.VWNode_TileEntity;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class TileEntityShipHelm extends ImplTileEntityPilotable implements ITickable {

	public double compassAngle = 0;
	public double lastCompassAngle = 0;

	public double wheelRotation = 0;
	public double lastWheelRotation = 0;

	private double nextWheelRotation;

	@Override
	public void update() {
		if (this.getWorld().isRemote) {
			calculateCompassAngle();
			lastWheelRotation = wheelRotation;
			wheelRotation += (nextWheelRotation - wheelRotation) * .25D;
		} else {
			double friction = .05D;
			double toOriginRate = .05D;
			if (Math.abs(wheelRotation) < toOriginRate) {
				wheelRotation = 0;
			} else {
				// wheelRotation -= math.signum(wheelRotation) * wheelRotation;
				double deltaForce = Math.max(Math.abs(wheelRotation * toOriginRate) - friction, 0);
				wheelRotation += deltaForce * -1 * Math.signum(wheelRotation);
			}

			VWNode_TileEntity thisNode = this.getNode();
			double totalMaxUpwardThrust = 0;
			for (GraphObject object : thisNode.getGraph().getObjects()) {
				VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
				TileEntity tile = otherNode.getParentTile();
				if (tile instanceof TileEntityEthereumCompressorPart) {
					BlockPos masterPos = ((TileEntityEthereumCompressorPart) tile).getMultiblockOrigin();
					TileEntityEthereumCompressorPart masterTile = (TileEntityEthereumCompressorPart) tile.getWorld()
							.getTileEntity(masterPos);
					// This is a transient problem that only occurs during world loading.
					if (masterTile != null) {
						totalMaxUpwardThrust += masterTile.getMaxThrust();
					}
					// masterTile.updateTicksSinceLastRecievedSignal();
				}
			}

			PhysicsWrapperEntity parentPhysicsEntity = this.getParentPhysicsEntity();
			VectorImmutable torqueAttemptedNormalImmutable = null;
			if (parentPhysicsEntity != null) {
				Vector torqueAttempted = new Vector(0, Math.signum(wheelRotation), 0);
				// parentPhysicsEntity.getPhysicsObject().getShipTransformationManager().getCurrentPhysicsTransform()
				//		.rotate(torqueAttempted, TransformType.SUBSPACE_TO_GLOBAL);
				torqueAttemptedNormalImmutable = torqueAttempted.toImmutable();
			}
			
			for (GraphObject object : thisNode.getGraph().getObjects()) {
				VWNode_TileEntity otherNode = (VWNode_TileEntity) object;
				TileEntity tile = otherNode.getParentTile();
				if (tile instanceof TileEntityRudderAxlePart) {
					BlockPos masterPos = ((TileEntityRudderAxlePart) tile).getMultiblockOrigin();
					TileEntityRudderAxlePart masterTile = (TileEntityRudderAxlePart) tile.getWorld()
							.getTileEntity(masterPos);
					// This is a transient problem that only occurs during world loading.
					if (masterTile != null) {
						// Wheel rotation is flipped because I'm an idiot
						if (parentPhysicsEntity == null) {
							masterTile.setRudderAngle(-this.wheelRotation / 4D);
						} else {
							masterTile.attemptTorque(parentPhysicsEntity.getPhysicsObject(),
									torqueAttemptedNormalImmutable, -this.wheelRotation / 4D,
									new Vector(EnumFacing.getFront(ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipHelm
											.getMetaFromState(this.getWorld().getBlockState(this.getPos()))).getDirectionVec()));
						}
					}
					// masterTile.updateTicksSinceLastRecievedSignal();
				}

			}

			sendUpdatePacketToAllNearby();
		}
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		nextWheelRotation = pkt.getNbtCompound().getDouble("wheelRotation");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagToSend = new NBTTagCompound();
		tagToSend.setDouble("wheelRotation", wheelRotation);
		return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		toReturn.setDouble("wheelRotation", wheelRotation);
		return toReturn;
	}

	public void calculateCompassAngle() {
		lastCompassAngle = compassAngle;

		IBlockState helmState = getWorld().getBlockState(getPos());
		if (helmState.getBlock() != ValkyrienWarfareControl.INSTANCE.vwControlBlocks.shipHelm) {
			return;
		}
		EnumFacing enumfacing = helmState.getValue(BlockShipHelm.FACING);
		double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

		BlockPos spawnPos = getWorld().getSpawnPoint();
		Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
		compassPoint.add(1D, 2D, 1D);

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(),
				getPos());
		if (wrapper != null) {
			wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(compassPoint,
					TransformType.SUBSPACE_TO_GLOBAL);
			// RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
			// compassPoint);
		}

		Vector compassDirection = new Vector(compassPoint);
		compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

		if (wrapper != null) {
			wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().rotate(compassDirection,
					TransformType.GLOBAL_TO_SUBSPACE);
			// RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.wToLTransform,
			// compassDirection);
		}

		compassDirection.normalize();
		compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z))
				- wheelAndCompassStateRotation;
		compassAngle = (compassAngle + 360D) % 360D;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		lastWheelRotation = wheelRotation = compound.getDouble("wheelRotation");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		compound.setDouble("wheelRotation", wheelRotation);
		return toReturn;
	}

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.ShipHelm;
	}

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		double rotationDelta = 0;
		if (message.airshipLeft_KeyDown) {
			rotationDelta -= 15D;
		}
		if (message.airshipRight_KeyDown) {
			rotationDelta += 15D;
		}
		IBlockState blockState = this.getWorld().getBlockState(getPos());
		if (blockState.getBlock() instanceof BlockShipHelm) {
			EnumFacing facing = blockState.getValue(BlockShipHelm.FACING);
			if (this.isPlayerInFront(sender, facing)) {
				wheelRotation += rotationDelta;
			} else {
				wheelRotation -= rotationDelta;
			}
		}
	}

}
