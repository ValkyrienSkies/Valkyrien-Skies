package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.addon.control.nodenetwork.INodeController;
import org.valkyrienskies.addon.control.tileentity.behaviour.NodeTEBehaviour;
import org.valkyrienskies.mod.common.tileentity.behaviour.BehaviourControlledTileEntity;

public abstract class TileEntityNodeControllerImpl extends BehaviourControlledTileEntity implements
    INodeController {
    /**
     * If -1, the algorithm will ignore this processor
     */
    private int priority;

    public TileEntityNodeControllerImpl(int priority) {
        super(NodeTEBehaviour.getFactory());
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
