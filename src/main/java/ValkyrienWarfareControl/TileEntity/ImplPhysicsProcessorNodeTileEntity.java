package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareControl.NodeNetwork.BasicNodeTileEntity;
import ValkyrienWarfareControl.NodeNetwork.IPhysicsProcessorNode;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ImplPhysicsProcessorNodeTileEntity extends BasicNodeTileEntity implements IPhysicsProcessorNode {

    /**
     * If -1, the algorithm will ignore this processor
     */
    private int priority = -1;

    public ImplPhysicsProcessorNodeTileEntity(int processorPriority) {
        this();
        setPriority(processorPriority);
    }

    public ImplPhysicsProcessorNodeTileEntity() {}

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newPriority) {
        priority = newPriority;
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

}
