package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.addon.control.block.torque.*;
import org.valkyrienskies.addon.control.util.ValkyrienSkiesControlUtil;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.List;
import java.util.Optional;

public class TileEntityGiantPropellerPart extends
    TileEntityMultiblockPartForce<GiantPropellerMultiblockSchematic, TileEntityGiantPropellerPart> implements
    IRotationNodeProvider<TileEntityGiantPropellerPart> {

    public static final int GIANT_PROPELLER_SORTING_PRIORITY = 50;
    private final IRotationNode rotationNode;
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
        return 999999;
    }

    @Override
    public Vector3dc getForceOutputNormal(double secondsToApply,
                                          PhysicsObject physicsObject) {
        if (!this.isPartOfAssembledMultiblock()) {
            return null;
        } else {
            if (!this.isMaster()) {
                TileEntityGiantPropellerPart master = this.getMaster();
                if (master != null) {
                    return master.getForceOutputNormal(secondsToApply, physicsObject);
                } else {
                    return null;
                }
            } else {
                if (!this.getRotationNode().isPresent()) {
                    return null;
                } else if (this.getRotationNode().get().getAngularVelocity() == 0) {
                    return null;
                }
                Vector3d facingDir = JOML.convertTo3d(this.getPropellerFacing().getDirectionVec());
                final double angularVelocity = this.getRotationNode().get().getAngularVelocity();
                if (angularVelocity != 0) {
                    // We don't want the propeller animation and force to be backwards.
                    facingDir.mul(-Math.signum(angularVelocity));
                }
                return facingDir;
            }
        }
    }

    @Override
    public double getThrustMagnitude(PhysicsObject physicsObject) {
        if (!this.isPartOfAssembledMultiblock()) {
            return 0;
        } else {
            if (!this.isMaster()) {
                TileEntityGiantPropellerPart master = this.getMaster();
                if (master != null) {
                    return master.getThrustMagnitude(physicsObject);
                } else {
                    return 0;
                }
            } else {
                if (!this.getRotationNode().isPresent()) {
                    return 0;
                }
                double angularVel = this.getRotationNode().get().getAngularVelocity();
                // Temporary simple thrust function.
                return 500D * angularVel * angularVel;
            }
        }
    }

    @Override
    public void update() {
        if (!this.getWorld().isRemote) {
            if (firstUpdate) {
                this.rotationNode.markInitialized();
                this.rotationNode.queueTask(() -> this.rotationNode.setAngularVelocityRatio(
                    this.getMultiBlockSchematic().getPropellerFacing().getOpposite(),
                    Optional.of(-1D)));
                firstUpdate = false;
            }

            if (this.isPartOfAssembledMultiblock()) {
                Optional<PhysicsObject> physicsObjectOptional = ValkyrienUtils
                    .getPhysoManagingBlock(getWorld(), getPos());
                if (this.isMaster()) {
                    if (!rotationNode.hasBeenPlacedIntoNodeWorld()) {
                        IRotationNodeWorld nodeWorld;
                        if (physicsObjectOptional.isPresent()) {
                            nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromShip(physicsObjectOptional.get());
                        } else {
                            nodeWorld = ValkyrienSkiesControlUtil.getRotationWorldFromWorld(getWorld());
                        }

                        nodeWorld.enqueueTaskOntoWorld(
                            () -> nodeWorld.setNodeFromPos(getPos(), rotationNode));

                        final int propellerRadius = this.getMultiBlockSchematic()
                            .getPropellerRadius();
                        this.rotationNode.queueTask(() -> this.rotationNode
                            .setRotationalInertia(propellerRadius * propellerRadius));
                    }
                    this.prevPropellerAngle = this.propellerAngle;
                    // May need to convert to degrees from radians.
                    this.propellerAngle = Math
                        .toDegrees(rotationNode.getAngularRotationUnsynchronized());
                }
                VSNetwork.sendTileToAllNearby(this);
            }
        } else {
            this.prevPropellerAngle = this.propellerAngle;
            double increment = nextPropellerAngle - propellerAngle;
            if (increment < 0) {
                increment = MathHelper.wrapDegrees(increment);
            }
            this.propellerAngle = this.propellerAngle + increment * .75;
        }
    }

    @Override
    public boolean attemptToAssembleMultiblock(World worldIn, BlockPos pos, EnumFacing facing) {
        List<IMultiblockSchematic> schematics = MultiblockRegistry.getSchematicsWithPrefix("multiblock_giant_propeller");
        for (IMultiblockSchematic schematic : schematics) {
            GiantPropellerMultiblockSchematic propSchem = (GiantPropellerMultiblockSchematic) schematic;
            if (propSchem.getPropellerFacing() == facing && schematic.attemptToCreateMultiblock(worldIn, pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void disassembleMultiblockLocal() {
        super.disassembleMultiblockLocal();

        Optional<PhysicsObject> object = ValkyrienUtils.getPhysoManagingBlock(getWorld(), getPos());
        object.ifPresent(obj -> this.rotationNode.queueTask(rotationNode::resetNodeData));
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
        return new SPacketUpdateTileEntity(pos, 0, toSend);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.nextPropellerAngle = pkt.getNbtCompound().getDouble("propeller_angle");
    }
}
