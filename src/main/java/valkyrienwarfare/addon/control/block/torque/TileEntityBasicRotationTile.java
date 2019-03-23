package valkyrienwarfare.addon.control.block.torque;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.Optional;

/**
 * A simple implementation of an IRotationNodeProvider tile entity.
 */
public class TileEntityBasicRotationTile extends TileEntity implements IRotationNodeProvider, ITickable {

    // Maps EnumFacing.Axis to both possible EnumFacing values.
    public static final ImmutableMap<EnumFacing.Axis, Tuple<EnumFacing, EnumFacing>> AXIS_TO_FACING_MAP;
    static {
        AXIS_TO_FACING_MAP = ImmutableMap.of(EnumFacing.Axis.X, new Tuple<>(EnumFacing.EAST, EnumFacing.WEST), EnumFacing.Axis.Y, new Tuple<>(EnumFacing.UP, EnumFacing.DOWN), EnumFacing.Axis.Z, new Tuple<>(EnumFacing.SOUTH, EnumFacing.NORTH));
    }

    protected final IRotationNode rotationNode;
    // Used for rendering purposes
    private double rotation = 0;
    private double lastRotation = 0;
    private double nextRotation;
    private boolean firstUpdate;

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
        this.lastRotation = this.rotation = this.nextRotation = rotationNode.getAngularRotation();
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
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
                if (physicsObjectOptional.isPresent()) {
                    IRotationNodeWorld nodeWorld = physicsObjectOptional.get().getPhysicsProcessor().getPhysicsRotationNodeWorld();
                    rotationNode.markInitialized();
                    nodeWorld.enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                    // nodeWorld.enqueueTaskOntoNode((task) -> task.setCustomTorqueFunction((physObject) -> 0.1D), getPos());
                }
                this.firstUpdate = false;
            }
            rotation = this.rotationNode.getAngularRotationUnsynchronized();
            sendUpdatePacketToAllNearby();
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

    private void sendUpdatePacketToAllNearby() {
        SPacketUpdateTileEntity spacketupdatetileentity = getUpdatePacket();
        WorldServer serverWorld = (WorldServer) world;
        Vector pos = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(getWorld(), getPos());
        if (wrapper != null) {
            wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(pos,
                    TransformType.SUBSPACE_TO_GLOBAL);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform,
            // pos);
        }
        serverWorld.mcServer.getPlayerList().sendToAllNearExcept(null, pos.X, pos.Y, pos.Z, 128D,
                getWorld().provider.getDimension(), spacketupdatetileentity);
    }

}
