package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeProvider;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeWorld;
import valkyrienwarfare.addon.control.block.torque.ImplRotationNode;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public class TileEntityGiantPropellerPart extends TileEntityMultiblockPartForce<GiantPropellerMultiblockSchematic> implements IRotationNodeProvider<TileEntityGiantPropellerPart> {

    public static final int GIANT_PROPELLER_SORTING_PRIORITY = 50;
    protected final IRotationNode rotationNode;
    private double prevPropellerAngle;
    private double propellerAngle;
    private double nextPropellerAngle;
    private boolean firstUpdate;

    public TileEntityGiantPropellerPart() {
        super();
        this.rotationNode = new ImplRotationNode<>(this, 5, GIANT_PROPELLER_SORTING_PRIORITY);
        this.firstUpdate = true;
    }

    @Override
    public double getMaxThrust() {
        return super.getMaxThrust();
    }

    @Override
    public VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject physicsObject) {
        return null;
    }

    @Override
    public double getThrustMagnitude() {
        return 0;
    }

    @Override
    public void update() {
        if (!this.getWorld().isRemote) {
            if (firstUpdate) {
                this.rotationNode.markInitialized();
                this.rotationNode.queueTask(() -> this.rotationNode.setAngularVelocityRatio(this.getMultiBlockSchematic().getPropellerFacing().getOpposite(), Optional.of(-1D)));
                firstUpdate = false;
            }

            if (this.isPartOfAssembledMultiblock()) {
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
                if (physicsObjectOptional.isPresent() && this.isMaster()) {
                    if (!rotationNode.hasBeenPlacedIntoNodeWorld()) {
                        IRotationNodeWorld nodeWorld = physicsObjectOptional.get().getPhysicsProcessor().getPhysicsRotationNodeWorld();
                        if (nodeWorld != null) {
                            nodeWorld.enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                        }
                    }
                    this.prevPropellerAngle = this.propellerAngle;
                    // May need to convert to degrees from radians.
                    this.propellerAngle = Math.toDegrees(rotationNode.getAngularRotationUnsynchronized());
                }
                this.sendUpdatePacketToAllNearby();
            }
        } else {
            this.prevPropellerAngle = this.propellerAngle;
            double increment = nextPropellerAngle - propellerAngle;
            if (increment < 0) {
                increment = MathHelper.wrapDegrees(increment);
            }
            this.propellerAngle = this.propellerAngle + increment * .85;
        }
    }

    @Override
    public void dissembleMultiblockLocal() {
        super.dissembleMultiblockLocal();
        Optional<PhysicsObject> object = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
        if (object.isPresent()) {
            this.rotationNode.queueTask(() -> rotationNode.resetNodeData());

        }
    }

    @Override
    public Optional<IRotationNode> getRotationNode() {
        if (rotationNode.isInitialized()) {
            return Optional.of(rotationNode);
        } else {
            return Optional.empty();
        }
    }

    public EnumFacing getPropellerFacing() {
        if (!this.isPartOfAssembledMultiblock()) {
            return null;
        }
        return getMultiBlockSchematic().getPropellerFacing();
    }

    public int getPropellerRadius() {
        if (!this.isPartOfAssembledMultiblock()) {
            return 1;
        }
        return getMultiBlockSchematic().getPropellerRadius();
    }

    public float getPropellerAngle(float partialTick) {
        return (float) (prevPropellerAngle + (propellerAngle - prevPropellerAngle) * partialTick);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        rotationNode.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        rotationNode.writeToNBT(compound);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound toSend = super.writeToNBT(new NBTTagCompound());
        toSend.setDouble("propeller_angle", propellerAngle);
        // Use super.writeToNBT to avoid sending the rotation node over nbt.
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, toSend);
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.nextPropellerAngle = pkt.getNbtCompound().getDouble("propeller_angle");
    }
}
