package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeProvider;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeWorld;
import valkyrienwarfare.addon.control.block.torque.ImplRotationNode;
import valkyrienwarfare.mod.coordinates.VectorImmutable;
import valkyrienwarfare.physics.management.PhysicsObject;

import java.util.Optional;

public class TileEntityGiantPropellerPart extends TileEntityMultiblockPartForce<GiantPropellerMultiblockSchematic> implements IRotationNodeProvider<TileEntityGiantPropellerPart> {

    protected final IRotationNode rotationNode;
    private double prevPropellerAngle;
    private double propellerAngle;
    private boolean firstUpdate;

    public TileEntityGiantPropellerPart() {
        super();
        this.rotationNode = new ImplRotationNode<>(this, 5);
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
        this.prevPropellerAngle = this.propellerAngle;
        this.propellerAngle += 5;
        if (!this.getWorld().isRemote) {
            if (firstUpdate) {
                this.rotationNode.markInitialized();
                firstUpdate = false;
            }

            if (this.isPartOfAssembledMultiblock()) {
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienWarfareMod.getPhysicsObject(getWorld(), getPos());
                if (physicsObjectOptional.isPresent() && !rotationNode.hasBeenPlacedIntoNodeWorld() && this.isMaster()) {
                    IRotationNodeWorld nodeWorld = physicsObjectOptional.get().getPhysicsProcessor().getPhysicsRotationNodeWorld();
                    if (nodeWorld != null) {
                        System.out.println("Placed into world");
                        nodeWorld.enqueueTaskOntoWorld(() -> nodeWorld.setNodeFromPos(getPos(), rotationNode));
                    }
                }
            }
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
        if (this.getWorld() == null || !this.getWorld().isRemote) {
            rotationNode.readFromNBT(compound);
        }
//		rotationNode.markInitialized();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        rotationNode.writeToNBT(compound);
        return compound;
    }

}
