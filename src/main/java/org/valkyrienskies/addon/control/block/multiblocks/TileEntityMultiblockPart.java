package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * Just a simple implementation of the interfaces.
 *
 * @param <E> The type of schematic for this TileEntity to use.
 * @param <F> The type of class extending this class.
 */
public abstract class TileEntityMultiblockPart<E extends IMultiblockSchematic, F extends TileEntityMultiblockPart> extends
    BasicNodeTileEntity implements ITileEntityMultiblockPart<E, F> {

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
        TileEntity masterTile = ValkyrienUtils.getTileEntitySafe(getWorld(), getMultiblockOrigin());
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
    public void disassembleMultiblock() {
        if (multiblockSchematic != null) {
            for (BlockPosBlockPair pair : multiblockSchematic.getStructureRelativeToCenter()) {
                BlockPos posToBreak = pair.getPos().add(getMultiblockOrigin());
                TileEntity tileToBreak = this.getWorld().getTileEntity(posToBreak);
                if (tileToBreak instanceof ITileEntityMultiblockPart) {
                    ((ITileEntityMultiblockPart) tileToBreak).disassembleMultiblockLocal();
                }
            }
        }
    }

    @Override
    public void disassembleMultiblockLocal() {
        this.isAssembled = false;
        this.isMaster = false;
        this.multiblockSchematic = null;
        VSNetwork.sendTileToAllNearby(this);
        this.markDirty();
    }

    @Override
    public void assembleMultiblock(E schematic, BlockPos relativePos) {
        this.isAssembled = true;
        this.isMaster = relativePos.equals(BlockPos.ORIGIN);
        this.offsetPos = relativePos;
        this.multiblockSchematic = schematic;
        VSNetwork.sendTileToAllNearby(this);
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
        offsetPos = new BlockPos(compound.getInteger("offsetPosX"),
            compound.getInteger("offsetPosY"), compound.getInteger("offsetPosZ"));
        multiblockSchematic = (E) MultiblockRegistry
            .getSchematicByID(compound.getString("multiblockSchematicID"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (isPartOfAssembledMultiblock()) {
            return getMultiBlockSchematic().getSchematicRenderBB(getMultiblockOrigin());
        } else {
            return super.getRenderBoundingBox();
        }
    }

}
