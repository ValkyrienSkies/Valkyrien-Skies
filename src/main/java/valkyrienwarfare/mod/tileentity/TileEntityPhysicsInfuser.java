package valkyrienwarfare.mod.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityPhysicsInfuser extends TileEntity implements ITickable, ICapabilityProvider {

    private final ItemStackHandler handler;

    public TileEntityPhysicsInfuser() {
        handler = new ItemStackHandler(5);
    }

    @Override
    public void update() {
        // Check if we have to create a ship
        if (!getWorld().isRemote) {
            IPhysicsChunk chunk = (IPhysicsChunk) getWorld().getChunkFromBlockCoords(getPos());
            Optional<PhysicsObject> parentShip = chunk.getPhysicsObjectOptional();
            // Check the status of the item slots
            if (!parentShip.isPresent() && canMainainShip()) {
                // Create a ship with this physics infuser
                PhysicsWrapperEntity ship = new PhysicsWrapperEntity(this);
                getWorld().spawnEntity(ship);
            }
        }
    }

    public boolean canMainainShip() {
        return handler.getStackInSlot(0) != ItemStack.EMPTY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("item_stack_handler", handler.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        handler.deserializeNBT(compound.getCompoundTag("item_stack_handler"));
        super.readFromNBT(compound);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) handler;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    public boolean isUseableByPlayer(EntityPlayer playerIn) {
        return true;
    }
}
