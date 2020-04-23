package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.addon.control.nodenetwork.INodeController;

public abstract class TileEntityNodeControllerImpl extends BasicNodeTileEntity implements
    INodeController {

    /**
     * If -1, the algorithm will ignore this processor
     */
    private int priority;

    public TileEntityNodeControllerImpl(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newPriority) {
        priority = newPriority;
    }

    @Override
    public BlockPos getNodePos() {
        return this.getPos();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        priority = compound.getInteger("priority");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setInteger("priority", priority);
        return compound;
    }

    @Override
    public int hashCode() {
        return getNodePos().hashCode();
    }

}
