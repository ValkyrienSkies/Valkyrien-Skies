package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.nodenetwork.BasicNodeTileEntity;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Just a simple implementation of the interfaces.
 * @param <E> The type of schematic for this TileEntity to use.
 * @param <F> The type of class extending this class.
 */
public abstract class TileEntityMultiblockPart<E extends IMulitblockSchematic, F extends TileEntityMultiblockPart> extends BasicNodeTileEntity implements ITileEntityMultiblockPart<E, F> {

	private boolean isAssembled;
	private boolean isMaster;
	// The relative position of this tile to its master.
	private BlockPos offsetPos;
	private E multiblockSchematic;
	
	public TileEntityMultiblockPart() {
		super();
		this.isAssembled = false;
		this.isMaster = false;
		this.offsetPos = BlockPos.ORIGIN;
		this.multiblockSchematic = null;
	}
	
	@Override
	public boolean isPartOfAssembledMultiblock() {
		return isAssembled;
	}

	@Override
	public boolean isMaster() {
		return isMaster;
	}

	@Override
	public F getMaster() {
		// TODO Auto-generated method stub
		TileEntity masterTile = this.getWorld().getTileEntity(this.getMultiblockOrigin());
		if (masterTile instanceof ITileEntityMultiblockPart) {
			return (F) masterTile;
		} else {
			return null;
		}
	}

	@Override
	public BlockPos getMultiblockOrigin() {
		return this.getPos().subtract(offsetPos);
	}

	@Override
	public BlockPos getRelativePos() {
		return offsetPos;
	}

	@Override
	public void dissembleMultiblock() {
		if (multiblockSchematic != null) {
			for (BlockPosBlockPair pair : multiblockSchematic.getStructureRelativeToCenter()) {
				BlockPos posToBreak = pair.getPos().add(getMultiblockOrigin());
				TileEntity tileToBreak = this.getWorld().getTileEntity(posToBreak);
				if (tileToBreak instanceof ITileEntityMultiblockPart) {
					((ITileEntityMultiblockPart) tileToBreak).dissembleMultiblockLocal();
				}
			}
		}
	}

	@Override
	public void dissembleMultiblockLocal() {
		this.isAssembled = false;
		this.isMaster = false;
		this.multiblockSchematic = null;
		this.sendUpdatePacketToAllNearby();
		this.markDirty();
	}

	@Override
	public void assembleMultiblock(E schematic, BlockPos relativePos) {
		this.isAssembled = true;
		this.isMaster = relativePos.equals(BlockPos.ORIGIN);
		this.offsetPos = relativePos;
		this.multiblockSchematic = schematic;
		this.sendUpdatePacketToAllNearby();
		this.markDirty();
	}

	@Override
	public E getMultiBlockSchematic() {
		return this.multiblockSchematic;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		toReturn.setBoolean("isAssembled", isAssembled);
		toReturn.setBoolean("isMaster", isMaster);
		toReturn.setInteger("offsetPosX", offsetPos.getX());
		toReturn.setInteger("offsetPosY", offsetPos.getY());
		toReturn.setInteger("offsetPosZ", offsetPos.getZ());
		if (multiblockSchematic != null) {
			toReturn.setString("multiblockSchematicID", multiblockSchematic.getSchematicID());
		} else {
			toReturn.setString("multiblockSchematicID", "unknown");
		}
		return toReturn;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		isAssembled = compound.getBoolean("isAssembled");
		isMaster = compound.getBoolean("isMaster");
		offsetPos = new BlockPos(compound.getInteger("offsetPosX"), compound.getInteger("offsetPosY"), compound.getInteger("offsetPosZ"));
		this.multiblockSchematic = (E) MultiblockRegistry.getSchematicByID(compound.getString("multiblockSchematicID"));
	}

	protected final void sendUpdatePacketToAllNearby() {
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
	
}
