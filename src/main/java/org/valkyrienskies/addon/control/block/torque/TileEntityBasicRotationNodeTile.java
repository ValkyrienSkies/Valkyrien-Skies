package org.valkyrienskies.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import org.valkyrienskies.addon.control.nodenetwork.BasicNodeTileEntity;
import org.valkyrienskies.addon.control.util.ValkyrienSkiesControlUtil;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Optional;

/**
 * A carbon copy of TileEntityBasicRotationTile except it extends BasicNodeTileEntity
 */
public class TileEntityBasicRotationNodeTile extends BasicNodeTileEntity implements
    IRotationNodeProvider, ITickable {

    protected final IRotationNode rotationNode;
    // Used for rendering purposes
    protected double rotation;
    protected double lastRotation;
    protected double nextRotation;
    private boolean firstUpdate;

    public TileEntityBasicRotationNodeTile() {
        super();
        this.rotationNode = new ImplRotationNode<>(this, .1);
        this.rotation = 0;
        this.lastRotation = 0;
        this.nextRotation = 0;
        this.firstUpdate = true;
    }

    public TileEntityBasicRotationNodeTile(int sortingPriority) {
        this();
        this.rotationNode.setSortingPriority(sortingPriority);
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
            if (this.firstUpdate || !rotationNode.isInitialized()) {
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
