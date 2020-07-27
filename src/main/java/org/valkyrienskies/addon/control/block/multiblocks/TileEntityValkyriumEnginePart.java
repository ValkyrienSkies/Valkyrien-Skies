package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.torque.*;
import org.valkyrienskies.addon.control.block.torque.custom_torque_functions.ValkyriumEngineTorqueFunction;
import org.valkyrienskies.addon.control.util.ValkyrienSkiesControlUtil;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.List;
import java.util.Optional;

public class TileEntityValkyriumEnginePart extends
    TileEntityMultiblockPart<ValkyriumEngineMultiblockSchematic, TileEntityValkyriumEnginePart> implements
    IRotationNodeProvider<TileEntityValkyriumEnginePart> {

    private static final int ROTATION_NODE_SORT_PRIORITY = 10000;
    @SuppressWarnings("WeakerAccess")
    protected final IRotationNode rotationNode;
    private double prevKeyframe;
    private double currentKeyframe;
    private double nextKeyframe;
    private boolean firstUpdate;

    @SuppressWarnings("WeakerAccess")
    public TileEntityValkyriumEnginePart() {
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
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
                    .getPhysoManagingBlock(getWorld(), getPos());

                IRotationNodeWorld nodeWorld;
                if (physicsObjectOptional.isPresent()) {
                    nodeWorld = ValkyrienSkiesControlUtil
                        .getRotationWorldFromShip(physicsObjectOptional.get());
                } else {
                    nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromWorld(getWorld());
                }
                if (physicsObjectOptional.isPresent() && !rotationNode.hasBeenPlacedIntoNodeWorld()
                    && this.getRelativePos()
                    .equals(this.getMultiBlockSchematic().getTorqueOutputPos())) {
                    nodeWorld.enqueueTaskOntoWorld(
                        () -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                }

                BlockPos torqueOutputPos = this.getMultiBlockSchematic().getTorqueOutputPos()
                    .add(this.getPos());
                TileEntity tileEntity = this.getWorld().getTileEntity(torqueOutputPos);
                if (tileEntity instanceof TileEntityValkyriumEnginePart) {
                    if (((TileEntityValkyriumEnginePart) tileEntity).getRotationNode()
                        .isPresent()) {
                        prevKeyframe = currentKeyframe;
                        double radiansRotatedThisTick =
                            ((TileEntityValkyriumEnginePart) tileEntity).getRotationNode().get()
                                .getAngularVelocityUnsynchronized() / 20D;
                        // Thats about right, although the x1.3 multiplier tells me the world node math is wrong.
                        currentKeyframe += radiansRotatedThisTick * 99D / (6D * Math.PI);
                        currentKeyframe = currentKeyframe % 99;
                    }
                }
                VSNetwork.sendTileToAllNearby(this);
            }
            this.markDirty();
        } else {
            // Client keyframe interpolating logic, use .85 to smoothly slide towards actual value
            // to appear more fluid when the server lags.
            prevKeyframe = currentKeyframe;
            currentKeyframe = VSMath
                .interpolateModulatedNumbers(currentKeyframe, nextKeyframe, .85, 99);
        }
    }

    public double getCurrentKeyframe(double partialTick) {
        return VSMath.interpolateModulatedNumbers(prevKeyframe, currentKeyframe, partialTick, 99);
    }

    @Override
    public void assembleMultiblock(ValkyriumEngineMultiblockSchematic schematic,
        BlockPos relativePos) {
        super.assembleMultiblock(schematic, relativePos);
        if (relativePos.equals(schematic.getTorqueOutputPos())) {
            Optional<PhysicsObject> objectOptional = ValkyrienUtils
                .getPhysoManagingBlock(getWorld(), getPos());
            IRotationNodeWorld nodeWorld;
            if (objectOptional.isPresent()) {
                nodeWorld = ValkyrienSkiesControlUtil
                    .getRotationWorldFromShip(objectOptional.get());
            } else {
                nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromWorld(getWorld());
            }
            EnumFacing facing = EnumFacing
                .getFacingFromVector(schematic.getTorqueOutputDirection().getX(),
                    schematic.getTorqueOutputDirection().getY(),
                    schematic.getTorqueOutputDirection().getZ());
            assert getRotationNode()
                .isPresent() : "How the heck did we try assembling the multiblock without a rotation node initialized!";

            this.rotationNode.queueTask(() -> {
                rotationNode.setAngularVelocityRatio(facing, Optional.of(-1D));
                rotationNode
                    .setCustomTorqueFunction(new ValkyriumEngineTorqueFunction(rotationNode));
            });
            nodeWorld
                .enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(pos, this.rotationNode));
        }
    }

    @Override
    public boolean attemptToAssembleMultiblock(World worldIn, BlockPos pos, EnumFacing facing) {
        List<IMultiblockSchematic> schematics = MultiblockRegistry.getSchematicsWithPrefix("multiblock_valkyrium_engine");
        for (IMultiblockSchematic schematic : schematics) {
            if (schematic.attemptToCreateMultiblock(worldIn, pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void disassembleMultiblockLocal() {
        super.disassembleMultiblockLocal();
        Optional<PhysicsObject> object = ValkyrienUtils.getPhysoManagingBlock(getWorld(), getPos());
        if (object.isPresent()) {
            this.rotationNode.queueTask(rotationNode::resetNodeData);
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
//        rotationNode.markInitialized();
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
