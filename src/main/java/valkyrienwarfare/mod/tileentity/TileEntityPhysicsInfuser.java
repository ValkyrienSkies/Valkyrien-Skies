package valkyrienwarfare.mod.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.fixes.VWNetwork;
import valkyrienwarfare.mod.block.BlockPhysicsInfuser;
import valkyrienwarfare.mod.container.InfuserButton;
import valkyrienwarfare.mod.gui.IVWTileGui;
import valkyrienwarfare.mod.network.VWGuiButtonMessage;
import valkyrienwarfare.mod.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.physics.collision.polygons.Polygon;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityPhysicsInfuser extends TileEntity implements ITickable, ICapabilityProvider, IVWTileGui {

    private final ItemStackHandler handler;
    private volatile boolean sendUpdateToClients;
    private volatile boolean tryToAssembleShip;
    private volatile boolean tryToDisassembleShip;
    private boolean physicsEnabled;
    private boolean tryingToAlignShip;

    public TileEntityPhysicsInfuser() {
        handler = new ItemStackHandler(5) {
            @Override
            protected void onContentsChanged(int slot) {
                sendUpdateToClients = true;
            }
        };
        sendUpdateToClients = false;
        tryToAssembleShip = false;
        tryToDisassembleShip = false;
        physicsEnabled = false;
        tryingToAlignShip = false;
    }

    @Override
    public void update() {
        // Check if we have to create a ship
        if (!getWorld().isRemote) {
            IPhysicsChunk chunk = (IPhysicsChunk) getWorld().getChunk(getPos());
            Optional<PhysicsObject> parentShip = chunk.getPhysicsObjectOptional();
            // Check the status of the item slots
            if (!parentShip.isPresent() && canMaintainShip() && tryToAssembleShip) {
                // Create a ship with this physics infuser
                // Make sure we don't try to create a ship when we're already in ship space.
                if (!PhysicsChunkManager.isLikelyShipChunk(getPos().getX() >> 4, getPos().getZ() >> 4)) {
                    PhysicsWrapperEntity ship = new PhysicsWrapperEntity(this);
                    getWorld().spawnEntity(ship);
                    // Also tell the watching players to open the new guy
                    // BlockPos newInfuserPos = ship.getPhysicsObject().getPhysicsInfuserPos();
                }
            }
            // Set the physics and align value to false if we're not in a ship
            if (!parentShip.isPresent()) {
                if (physicsEnabled || tryingToAlignShip) {
                    physicsEnabled = false;
                    tryingToAlignShip = false;
                    sendUpdateToClients = true;
                }
            }
            // Send any updates to clients
            if (sendUpdateToClients) {
                VWNetwork.sendTileToAllNearby(this);
                sendUpdateToClients = false;
            }
        }

        // Always set tryToAssembleShip and tryToDisassembleShip to false, they only have 1 tick to try to act.
        tryToAssembleShip = false;
        tryToDisassembleShip = false;
    }

    public boolean canMaintainShip() {
        ItemStack mainStack = handler.getStackInSlot(2);
        return !mainStack.isEmpty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("item_stack_handler", handler.serializeNBT());
        compound.setBoolean("physics_enabled", physicsEnabled);
        compound.setBoolean("try_to_align_ship", tryingToAlignShip);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        handler.deserializeNBT(compound.getCompoundTag("item_stack_handler"));
        physicsEnabled = compound.getBoolean("physics_enabled");
        tryingToAlignShip = compound.getBoolean("try_to_align_ship");
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

    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        IBlockState state = getWorld().getBlockState(getPos());
        if (state.getBlock() instanceof BlockPhysicsInfuser) {
            EnumFacing sideFacing = ((BlockPhysicsInfuser) state.getBlock()).getDummyStateFacing(state);
            // First make the aabb for the main block, then include the dummy block, then include the bits coming out the top.
            AxisAlignedBB renderBB = new AxisAlignedBB(getPos()).expand(-sideFacing.getXOffset(), -sideFacing.getYOffset(), -sideFacing.getZOffset()).expand(0, .3, 0);
            Optional<PhysicsObject> physicsObject = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
            if (physicsObject.isPresent()) {
                // We're in a physics object; convert the bounding box to a polygon; put its coordinates in global space, and then return the bounding box that encloses
                // all the points.
                Polygon bbAsPoly = new Polygon(renderBB, physicsObject.get()
                        .getShipTransformationManager()
                        .getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);
                return bbAsPoly.getEnclosedAABB();
            } else {
                return renderBB;
            }
        }
        // Default in case broken.
        return super.getRenderBoundingBox();
    }

    public boolean isCurrentlyInShip() {
        Optional<PhysicsObject> physicsObject = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
        return physicsObject.isPresent();
    }

    public boolean isPhysicsEnabled() {
        return physicsEnabled;
    }

    public boolean isTryingToAlignShip() {
        return tryingToAlignShip;
    }

    public boolean isTryingToAssembleShip() {
        return tryToAssembleShip;
    }

    public boolean isTryingToDisassembleShip() {
        return tryToDisassembleShip;
    }

    @Override
    public void onButtonPress(int buttonId, EntityPlayer presser) {
        InfuserButton button = InfuserButton.values()[buttonId];
        this.sendUpdateToClients = true;
        // TODO: Check if presser is allowed to press this button first
        /*
        if (presser isn't allowed) {
            return;
        }
         */
        switch (button) {
            case ASSEMBLE_SHIP:
                if (!this.isCurrentlyInShip()) {
                    // Create a ship
                    this.tryToAssembleShip = true;
                } else {
                    // Destroy the ship if possible
                    this.tryToDisassembleShip = true;
                }
                break;
            case ENABLE_PHYSICS:
                this.physicsEnabled = !isPhysicsEnabled();
                break;
            case ALIGN_SHIP:
                this.tryingToAlignShip = !isTryingToAlignShip();
                break;
        }

        if (getWorld().isRemote) {
            // If client, then send a packet telling the server we pressed this button.
            ValkyrienWarfareMod.physWrapperNetwork.sendToServer(new VWGuiButtonMessage(this, button.ordinal()));
        }
    }

    /**
     * If this TileEntity is in a ship then this returns whether that ship can be deconstructed or not, otherwise this returns true.
     *
     * @return
     */
    public boolean canShipBeDeconstructed() {
        Optional<PhysicsObject> physicsObject = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
        if (physicsObject.isPresent()) {
            return physicsObject.get()
                    .canShipBeDeconstructed();
        } else {
            // No ship, so we don't really worry about it; just return true.
            return true;
        }
    }

}
