package org.valkyrienskies.addon.control.tileentity;

import lombok.Getter;
import lombok.Setter;
import mcp.MethodsReturnNonnullByDefault;
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
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.block.BlockPhysicsInfuser;
import org.valkyrienskies.addon.control.container.EnumInfuserButton;
import org.valkyrienskies.addon.control.gui.IVSTileGui;
import org.valkyrienskies.addon.control.network.VSGuiButtonMessage;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.block_relocation.IRelocationAwareTile;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TileEntityPhysicsInfuser extends TileEntity implements ITickable, ICapabilityProvider,
    IVSTileGui, IRelocationAwareTile {

    private final ItemStackHandler handler;
    private volatile boolean sendUpdateToClients;
    @Getter private volatile boolean isTryingToAssembleShip;
    @Getter private volatile boolean isTryingToDisassembleShip;
    @Getter @Setter private boolean isPhysicsEnabled;
    @Getter private boolean isTryingToAlignShip;
    // Used by the client to store the vertical offset of each core
    private Map<EnumInfuserCore, Double> coreOffsets, coreOffsetsPrevTick;
    private int disassembleCounter; // # of ticks to wait before resetting isTryingToDisassembleShip

    public TileEntityPhysicsInfuser() {
        handler = new ItemStackHandler(EnumInfuserCore.values().length) {
            @Override
            protected void onContentsChanged(int slot) {
                sendUpdateToClients = true;
            }
        };
        sendUpdateToClients = false;
        isTryingToAssembleShip = false;
        isTryingToDisassembleShip = false;
        isPhysicsEnabled = false;
        isTryingToAlignShip = false;
        coreOffsets = new HashMap<>();
        coreOffsetsPrevTick = new HashMap<>();
        for (EnumInfuserCore enumInfuserCore : EnumInfuserCore.values()) {
            coreOffsets.put(enumInfuserCore, 0D);
            coreOffsetsPrevTick.put(enumInfuserCore, 0D);
        }
        disassembleCounter = 0;
    }

    @Override
    public void update() {
        // Check if we have to create a ship
        if (!getWorld().isRemote) {
            Optional<PhysicsObject> parentShip = ValkyrienUtils
                .getPhysoManagingBlock(getWorld(), getPos());

            // Update the blockstate lighting
            IBlockState infuserState = getWorld().getBlockState(getPos());
            if (infuserState.getBlock() == ValkyrienSkiesControl.INSTANCE.vsControlBlocks.physicsInfuser) {
                if (isPhysicsEnabled() && canMaintainShip()) {
                    if (!infuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON)) {
                        IBlockState newState = infuserState
                            .withProperty(BlockPhysicsInfuser.INFUSER_LIGHT_ON, true);
                        getWorld().setBlockState(getPos(), newState);
                    }
                } else {
                    if (infuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON)) {
                        IBlockState newState = infuserState
                            .withProperty(BlockPhysicsInfuser.INFUSER_LIGHT_ON, false);
                        getWorld().setBlockState(getPos(), newState);
                    }
                }
            }
            // Check the status of the item slots
            if (!parentShip.isPresent() && canMaintainShip() && isTryingToAssembleShip) {
                // Create a ship with this physics infuser
                // Make sure we don't try to create a ship when we're already in ship space.
                if (!ShipChunkAllocator.isBlockInShipyard(getPos())) {
                    try {
                        ValkyrienUtils.assembleShipAsOrderedByPlayer(getWorld(), null, getPos());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Also tell the watching players to open the new guy
                    // BlockPos newInfuserPos = ship.getPhysicsObject().getPhysicsInfuserPos();
                }
            }

            // Once the middle core is removed, automatically deconstruct the ship if possible.
            if (parentShip.isPresent() && !canMaintainShip()) {
                PhysicsObject physicsObject = parentShip.get();
                this.isTryingToAlignShip = true;
                this.isTryingToDisassembleShip = true;
                physicsObject.setPhysicsEnabled(true);
                physicsObject.setShipAligningToGrid(this.isTryingToAlignShip);
                physicsObject.setAttemptToDeconstructShip(this.isTryingToDisassembleShip);
            }

            // Send any updates to clients
            if (sendUpdateToClients) {
                VSNetwork.sendTileToAllNearby(this);
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
                        double sinAngle = ((worldTime % 50) * 2 * Math.PI / 50)
                            + (2 * Math.PI / EnumInfuserCore.values().length)
                            * enumInfuserCore.coreSlotIndex;
                        double idealOffset = .025 * (Math.sin(sinAngle) + 1);
                        double lerpedOffset =
                            .9 * coreOffsets.get(enumInfuserCore) + .1 * idealOffset;
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
        isTryingToAssembleShip = false;
        if (disassembleCounter == 0) {
            isTryingToDisassembleShip = false;
        }
    }

    public boolean canMaintainShip() {
        ItemStack mainStack = handler.getStackInSlot(EnumInfuserCore.MAIN.coreSlotIndex);
        return !mainStack.isEmpty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("item_stack_handler", handler.serializeNBT());
        compound.setBoolean("physics_enabled", isPhysicsEnabled);
        compound.setBoolean("try_to_align_ship", isTryingToAlignShip);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        handler.deserializeNBT(compound.getCompoundTag("item_stack_handler"));
        isPhysicsEnabled = compound.getBoolean("physics_enabled");
        isTryingToAlignShip = compound.getBoolean("try_to_align_ship");
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
            return player
                .getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
                    (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    @MethodsReturnNonnullByDefault
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
            EnumFacing sideFacing = ((BlockPhysicsInfuser) blockType)
                .getDummyStateFacing(blockState);
            // First make the aabb for the main block, then include the dummy block, then include the bits coming out the top.
            return new AxisAlignedBB(getPos())
                .expand(-sideFacing.getXOffset(), -sideFacing.getYOffset(),
                    -sideFacing.getZOffset())
                .expand(0, .3, 0);
        }
        // Default in case broken.
        return super.getRenderBoundingBox();
    }

    public boolean isCurrentlyInShip() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(getWorld(), getPos());
        return physicsObject.isPresent();
    }

    @Override
    public void onButtonPress(int buttonId, EntityPlayer presser) {
        if (buttonId >= EnumInfuserButton.values().length) {
            // Button not part of the gui, skip it.
            return;
        }
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
                    this.isTryingToAssembleShip = true;
                } else {
                    // Destroy the ship if possible
                    this.isTryingToDisassembleShip = true;
                    this.disassembleCounter = 5;
                }
                break;
            case ENABLE_PHYSICS:
                this.isPhysicsEnabled = !isPhysicsEnabled();
                break;
            case ALIGN_SHIP:
                this.isTryingToAlignShip = !isTryingToAlignShip();
                break;
        }

        if (getWorld().isRemote) {
            // If client, then send a packet telling the server we pressed this button.
            ValkyrienSkiesControl.controlGuiNetwork
                .sendToServer(new VSGuiButtonMessage(this, button.ordinal()));
        } else {
            Optional<PhysicsObject> optionalPhysicsObject = ValkyrienUtils.getPhysoManagingBlock(getWorld(), getPos());
            if (optionalPhysicsObject.isPresent()) {
                PhysicsObject physicsObject = optionalPhysicsObject.get();
                physicsObject.setPhysicsEnabled(this.isPhysicsEnabled);
                physicsObject.setShipAligningToGrid(this.isTryingToAlignShip);
                physicsObject.setAttemptToDeconstructShip(this.isTryingToDisassembleShip);
            }
        }
    }

    /**
     * @return If this TileEntity is in a ship then this returns whvalkyrium that ship can be
     * deconstructed or not, otherwise this returns true.
     */
    public boolean canShipBeDeconstructed() {
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysoManagingBlock(getWorld(), getPos());
        return !physicsObject.isPresent() || physicsObject.get()
            .isShipAlignedToWorld();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState,
        IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    /**
     * If the infuser is in a ship then we return true if this tile is the center of the ship. If the infuser isn't
     * in a ship then we just retard true regardless.
     *
     * @return
     */
    public boolean isCenterOfShip() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public double getCoreVerticalOffset(EnumInfuserCore enumInfuserCore, float partialTicks) {
        return (1 - partialTicks) * coreOffsetsPrevTick.get(enumInfuserCore)
            + partialTicks * coreOffsets.get(enumInfuserCore);
    }

    @Nullable
    @Override
    public TileEntity createRelocatedTile(BlockPos newPos, @Nullable ShipData copiedBy) {
        NBTTagCompound tileEntNBT = writeToNBT(new NBTTagCompound());
        // Change the block position to be inside of the Ship
        tileEntNBT.setInteger("x", newPos.getX());
        tileEntNBT.setInteger("y", newPos.getY());
        tileEntNBT.setInteger("z", newPos.getZ());
        TileEntityPhysicsInfuser relocatedCopy = (TileEntityPhysicsInfuser) TileEntity.create(world, tileEntNBT);
        if (copiedBy == null) {
            // We've been moved from a ship into the world, reset the fields
            relocatedCopy.isTryingToAssembleShip = false;
            relocatedCopy.isTryingToDisassembleShip = false;
            relocatedCopy.isPhysicsEnabled = false;
            relocatedCopy.isTryingToAlignShip = false;
        }
        return relocatedCopy;
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
