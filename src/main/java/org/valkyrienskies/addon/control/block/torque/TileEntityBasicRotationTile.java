package org.valkyrienskies.addon.control.block.torque;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import org.valkyrienskies.addon.control.util.ValkyrienSkiesControlUtil;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

/**
 * A simple implementation of an IRotationNodeProvider tile entity.
 */
public class TileEntityBasicRotationTile extends TileEntity implements IRotationNodeProvider,
    ITickable {

    // Maps EnumFacing.Axis to both possible EnumFacing values.
    public static final ImmutableMap<Axis, Tuple<EnumFacing, EnumFacing>> AXIS_TO_FACING_MAP;
    public static final ImmutableMap<EnumFacing, Double> FACING_TO_RATIO_MAP;

    static {
        AXIS_TO_FACING_MAP = ImmutableMap
            .of(EnumFacing.Axis.X, new Tuple<>(EnumFacing.EAST, EnumFacing.WEST), EnumFacing.Axis.Y,
                new Tuple<>(EnumFacing.UP, EnumFacing.DOWN), EnumFacing.Axis.Z,
                new Tuple<>(EnumFacing.SOUTH, EnumFacing.NORTH));
        FACING_TO_RATIO_MAP = ImmutableMap.<EnumFacing, Double>builder()
            .put(EnumFacing.EAST, 1D)
            .put(EnumFacing.WEST, -1D)
            .put(EnumFacing.UP, 1D)
            .put(EnumFacing.DOWN, -1D)
            .put(EnumFacing.SOUTH, 1D)
            .put(EnumFacing.NORTH, -1D)
            .build();
    }

    @SuppressWarnings("WeakerAccess")
    protected final IRotationNode rotationNode;
    // Used for rendering purposes
    private double rotation;
    private double lastRotation;
    private double nextRotation;
    private boolean firstUpdate;

    @SuppressWarnings("WeakerAccess")
    public TileEntityBasicRotationTile() {
        super();
        this.rotationNode = new ImplRotationNode<>(this, .1);
        this.rotation = 0;
        this.lastRotation = 0;
        this.nextRotation = 0;
        this.firstUpdate = true;
    }

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
        rotationNode.readFromNBT(compound);
        this.lastRotation = this.rotation = this.nextRotation = rotationNode
            .getAngularRotationUnsynchronized();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        rotationNode.writeToNBT(compound);
        return compound;
    }

    @Override
    public void update() {
        if (this.getWorld().isRemote) {
            lastRotation = rotation;
            rotation += (nextRotation - rotation) * .85D;
        } else {
            if (this.firstUpdate) {
                // Inject the rotation node into the physics world.
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
                    .getPhysoManagingBlock(getWorld(), getPos());
                IRotationNodeWorld nodeWorld;
                if (physicsObjectOptional.isPresent()) {
                    nodeWorld = ValkyrienSkiesControlUtil
                        .getRotationWorldFromShip(physicsObjectOptional.get());
                } else {
                    nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromWorld(getWorld());
                }

                rotationNode.markInitialized();
                nodeWorld.enqueueTaskOntoWorld(
                    () -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                // nodeWorld.enqueueTaskOntoNode((task) -> task.setCustomTorqueFunction((physObject) -> 0.1D), getPos());

                this.firstUpdate = false;
            }
            rotation = this.rotationNode.getAngularRotationUnsynchronized();
            VSNetwork.sendTileToAllNearby(this);
            this.markDirty();
        }
    }

    public final double getRenderRotationRadians(float partialTick) {
        return lastRotation + (rotation - lastRotation) * partialTick;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("rotation", rotation);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        nextRotation = pkt.getNbtCompound().getDouble("rotation");
    }

    @Override
    public void invalidate() {
        super.invalidate();
        rotationNode.queueNodeForDeletion();
    }

}
