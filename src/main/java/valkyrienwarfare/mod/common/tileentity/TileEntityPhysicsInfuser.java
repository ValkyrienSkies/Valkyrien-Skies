package valkyrienwarfare.mod.common.tileentity;

import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.fixes.VWNetwork;
import valkyrienwarfare.mod.client.gui.IVWTileGui;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.block.BlockPhysicsInfuser;
import valkyrienwarfare.mod.common.container.EnumInfuserButton;
import valkyrienwarfare.mod.common.network.VWGuiButtonMessage;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TileEntityPhysicsInfuser extends TileEntity implements ITickable, ICapabilityProvider, IVWTileGui {

    private final ItemStackHandler handler;
    private volatile boolean sendUpdateToClients;
    private volatile boolean tryToAssembleShip;
    private volatile boolean tryToDisassembleShip;
    private boolean physicsEnabled;
    private boolean tryingToAlignShip;
    // Used by the client to store the vertical offset of each core
    private Map<EnumInfuserCore, Double> coreOffsets, coreOffsetsPrevTick;

    public TileEntityPhysicsInfuser() {
        handler = new ItemStackHandler(EnumInfuserCore.values().length) {
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
        coreOffsets = new HashMap<>();
        coreOffsetsPrevTick = new HashMap<>();
        for (EnumInfuserCore enumInfuserCore : EnumInfuserCore.values()) {
            coreOffsets.put(enumInfuserCore, 0D);
            coreOffsetsPrevTick.put(enumInfuserCore, 0D);
        }
    }

    @Override
    public void update() {
        // Check if we have to create a ship
        if (!getWorld().isRemote) {
            IPhysicsChunk chunk = (IPhysicsChunk) getWorld().getChunk(getPos());
            Optional<PhysicsObject> parentShip = chunk.getPhysicsObjectOptional();
            // Set the physics and align value to false if we're not in a ship
            if (!parentShip.isPresent()) {
                if (physicsEnabled || tryingToAlignShip) {
                    physicsEnabled = false;
                    tryingToAlignShip = false;
                    sendUpdateToClients = true;
                }
            }

            // Update the blockstate lighting
            IBlockState infuserState = getWorld().getBlockState(getPos());
            if (infuserState.getBlock() == ValkyrienWarfareMod.INSTANCE.physicsInfuser) {
                if (isPhysicsEnabled() && canMaintainShip()) {
                    if (!infuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON)) {
                        IBlockState newState = infuserState.withProperty(BlockPhysicsInfuser.INFUSER_LIGHT_ON, true);
                        getWorld().setBlockState(getPos(), newState);
                    }
                } else {
                    if (infuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON)) {
                        IBlockState newState = infuserState.withProperty(BlockPhysicsInfuser.INFUSER_LIGHT_ON, false);
                        getWorld().setBlockState(getPos(), newState);
                    }
                }
            }
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

            // Send any updates to clients
            if (sendUpdateToClients) {
                VWNetwork.sendTileToAllNearby(this);
                sendUpdateToClients = false;
            }
        } else {
            // Client code to produce the physics infuser core wave effect
            if (this.isPhysicsEnabled()) {
                long worldTime = getWorld().getWorldTime();
                for (EnumInfuserCore enumInfuserCore : EnumInfuserCore.values()) {
                    coreOffsetsPrevTick.put(enumInfuserCore, coreOffsets.get(enumInfuserCore));
                    if (!handler.getStackInSlot(enumInfuserCore.coreSlotIndex)
                            .isEmpty() && canMaintainShip()) {
                        double sinAngle = ((worldTime % 50) * 2 * Math.PI / 50) + (2 * Math.PI / EnumInfuserCore.values().length) * enumInfuserCore.coreSlotIndex;
                        double idealOffset = .025 * (Math.sin(sinAngle) + 1);
                        double lerpedOffset = .9 * coreOffsets.get(enumInfuserCore) + .1 * idealOffset;
                        coreOffsets.put(enumInfuserCore, lerpedOffset);
                    } else {
                        coreOffsets.put(enumInfuserCore, 0D);
                    }
                }
            } else {
                for (EnumInfuserCore enumInfuserCore : EnumInfuserCore.values()) {
                    coreOffsetsPrevTick.put(enumInfuserCore, coreOffsets.get(enumInfuserCore));
                    double lerpedOffset = .95 * coreOffsets.get(enumInfuserCore);
                    coreOffsets.put(enumInfuserCore, lerpedOffset);
                }
            }
        }

        // Always set tryToAssembleShip and tryToDisassembleShip to false, they only have 1 tick to try to act.
        tryToAssembleShip = false;
        tryToDisassembleShip = false;
    }

    public boolean canMaintainShip() {
        ItemStack mainStack = handler.getStackInSlot(EnumInfuserCore.MAIN.coreSlotIndex);
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
        // Use the tile entity specific methods here instead of World for consistency with other tile entities.
        Block blockType = getBlockType();
        int blockMeta = getBlockMetadata();
        // Only check this after we've gotten the block meta to avoid a data race.
        if (blockType instanceof BlockPhysicsInfuser) {
            IBlockState blockState = blockType.getStateFromMeta(blockMeta);
            EnumFacing sideFacing = ((BlockPhysicsInfuser) blockType).getDummyStateFacing(blockState);
            // First make the aabb for the main block, then include the dummy block, then include the bits coming out the top.
            return new AxisAlignedBB(getPos()).expand(-sideFacing.getXOffset(), -sideFacing.getYOffset(), -sideFacing.getZOffset())
                    .expand(0, .3, 0);
        }
        // Default in case broken.
        return super.getRenderBoundingBox();
    }

    public boolean isCurrentlyInShip() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(getWorld(), getPos());
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
        EnumInfuserButton button = EnumInfuserButton.values()[buttonId];
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
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(getWorld(), getPos());
        if (physicsObject.isPresent()) {
            return physicsObject.get()
                    .canShipBeDeconstructed();
        } else {
            // No ship, so we don't really worry about it; just return true.
            return true;
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    public boolean isCenterOfShip() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(getWorld(), getPos());
        if (physicsObject.isPresent()) {
            return physicsObject.get()
                    .getPhysicsInfuserPos()
                    .equals(getPos());
        } else {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    public double getCoreVerticalOffset(EnumInfuserCore enumInfuserCore, float partialTicks) {
        return (1 - partialTicks) * coreOffsetsPrevTick.get(enumInfuserCore) + partialTicks * coreOffsets.get(enumInfuserCore);
    }

    /**
     * An enum representation of the different core slots for the physics infuser.
     */
    public enum EnumInfuserCore {

        ONE("physics_core_small1_geo", 0, 45, 22),
        TWO("physics_core_small2_geo", 1, 45, 54),
        MAIN("physics_core_main_geo", 2, 79, 54),
        FOUR("physics_core_small4_geo", 3, 112, 54),
        FIVE("physics_core_small3_geo", 4, 112, 22);

        // The name of the model this core will render as in the PhysicsInfuserRenderer
        public final String coreModelName;
        // Container and gui values.
        public final int coreSlotIndex, guiXPos, guiYPos;

        EnumInfuserCore(String coreModelName, int coreSlotIndex, int guiXPos, int guiYPos) {
            this.coreModelName = coreModelName;
            this.coreSlotIndex = coreSlotIndex;
            this.guiXPos = guiXPos;
            this.guiYPos = guiYPos;
        }
    }
}
