package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.Sys;
import scala.tools.cmd.Opt;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeProvider;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeWorld;
import valkyrienwarfare.addon.control.block.torque.ImplRotationNode;
import valkyrienwarfare.addon.control.block.torque.custom_torque_functions.EtherEngineTorqueFunction;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public class TileEntityEthereumEnginePart extends TileEntityMultiblockPart<EthereumEngineMultiblockSchematic, TileEntityEthereumEnginePart> implements IRotationNodeProvider<TileEntityEthereumEnginePart> {

	public static final int ROTATION_NODE_SORT_PRIORITY = 10000;
	protected final IRotationNode rotationNode;
	private double prevKeyframe;
	private double currentKeyframe;
	private double nextKeyframe;
	private boolean firstUpdate;

	public TileEntityEthereumEnginePart() {
		super();
		this.prevKeyframe = 0;
		this.currentKeyframe = 0;
		this.rotationNode = new ImplRotationNode<>(this, 50, ROTATION_NODE_SORT_PRIORITY);
		this.firstUpdate = true;
	}

	@Override
	public void update() {
		super.update();
		if (!this.getWorld().isRemote) {
			if (firstUpdate) {
				this.rotationNode.markInitialized();
				firstUpdate = false;
			}

			if (this.isPartOfAssembledMultiblock()) {
				Optional<PhysicsObject> physicsObjectOptional = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
				if (physicsObjectOptional.isPresent() && !rotationNode.hasBeenPlacedIntoNodeWorld() && this.getRelativePos().equals(getMultiBlockSchematic().getTorqueOutputPos())) {
					IRotationNodeWorld nodeWorld = physicsObjectOptional.get().getPhysicsProcessor().getPhysicsRotationNodeWorld();
					if (nodeWorld != null) {
						nodeWorld.enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
					}
				}

				BlockPos torqueOutputPos = this.getMultiBlockSchematic().getTorqueOutputPos().add(this.getPos());
				TileEntity tileEntity = this.getWorld().getTileEntity(torqueOutputPos);
				if (tileEntity instanceof TileEntityEthereumEnginePart) {
					if (((TileEntityEthereumEnginePart) tileEntity).getRotationNode().isPresent()) {
						prevKeyframe = currentKeyframe;
						double radiansRotatedThisTick = ((TileEntityEthereumEnginePart) tileEntity).getRotationNode().get().getAngularVelocityUnsynchronized() / 20D;
						// Thats about right, although the x1.3 multiplier tells me the world node math is wrong.
						currentKeyframe += radiansRotatedThisTick * 99D / (6D * Math.PI);
						currentKeyframe = currentKeyframe % 99;
					}
				}
				sendUpdatePacketToAllNearby();
			}
			this.markDirty();
		} else {
			prevKeyframe = currentKeyframe;
			double increment = nextKeyframe - currentKeyframe;
			if (increment < 0) {
				increment += 99;
			}
			currentKeyframe += (increment * .85);
			currentKeyframe %= 99;
		}
	}

	public double getCurrentKeyframe(double partialTick) {
		double increment = currentKeyframe - prevKeyframe;
		if (increment < 0) {
			increment += 99;
		}
		return ((prevKeyframe + (increment * partialTick)) % 99) + 1;
	}

	@Override
	public void assembleMultiblock(EthereumEngineMultiblockSchematic schematic, BlockPos relativePos) {
		super.assembleMultiblock(schematic, relativePos);
		if (relativePos.equals(schematic.getTorqueOutputPos())) {
			Optional<PhysicsObject> objectOptional = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
			if (objectOptional.isPresent()) {
				IRotationNodeWorld nodeWorld = objectOptional.get().getPhysicsProcessor().getPhysicsRotationNodeWorld();
				EnumFacing facing = EnumFacing.getFacingFromVector(schematic.getTorqueOutputDirection().getX(), schematic.getTorqueOutputDirection().getY(), schematic.getTorqueOutputDirection().getZ());
				assert getRotationNode().isPresent() : "How the heck did we try assembling the multiblock without a rotation node initialized!";
//				System.out.println(rotationNode.getNodePos());
				this.rotationNode.queueTask(() -> {
					rotationNode.setAngularVelocityRatio(facing, Optional.of(-1D));
					rotationNode.setCustomTorqueFunction(new EtherEngineTorqueFunction(rotationNode));
				});
				nodeWorld.enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(pos, this.rotationNode));
			}

		}
	}

	@Override
	public void dissembleMultiblockLocal() {
		super.dissembleMultiblockLocal();
		Optional<PhysicsObject> object = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
		if (object.isPresent()) {
			this.rotationNode.queueTask(() -> rotationNode.resetNodeData());

		}
	}
	// The following methods are basically just here because interfaces can't have fields.
	@Override
	public Optional<IRotationNode> getRotationNode() {
		if (rotationNode.isInitialized()) {
			return Optional.of(rotationNode);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (this.getWorld() == null || !this.getWorld().isRemote) {
			rotationNode.readFromNBT(compound);
		}
//		rotationNode.markInitialized();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		rotationNode.writeToNBT(compound);
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagToSend = super.getUpdateTag();
		tagToSend.setDouble("currentKeyframe", currentKeyframe);
		return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		nextKeyframe = pkt.getNbtCompound().getDouble("currentKeyframe");
	}
}
