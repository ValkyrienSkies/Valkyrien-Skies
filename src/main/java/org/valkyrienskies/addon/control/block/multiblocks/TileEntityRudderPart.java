package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.AxisAngle4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.addon.control.MultiblockRegistry;
import org.valkyrienskies.mod.common.network.VSNetwork;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

import java.util.List;
import java.util.Optional;

public class TileEntityRudderPart extends
    TileEntityMultiblockPartForce<RudderAxleMultiblockSchematic, TileEntityRudderPart> {

    // Angle must be between -90 and 90
    private double rudderAngle;
    // For client rendering purposes only
    private double prevRudderAngle;
    private double nextRudderAngle;

    public TileEntityRudderPart() {
        super();
        this.rudderAngle = 0;
        this.prevRudderAngle = 0;
        this.nextRudderAngle = 0;
    }

    @Override
    public void update() {
        super.update();
        this.prevRudderAngle = rudderAngle;
        if (this.getWorld().isRemote) {
            // Do this to smooth out lag between the server sending packets.
            this.rudderAngle = rudderAngle + .5 * (nextRudderAngle - rudderAngle);
        }
    }

    public Vector3d getForcePositionInShipSpace() {
        Vector3d facingOffset = getForcePosRelativeToAxleInShipSpace();
        if (facingOffset != null) {
            return new Vector3d(facingOffset.x + pos.getX() + .5, facingOffset.y + pos.getY() + .5,
                facingOffset.z + pos.getZ() + .5);
        } else {
            return null;
        }
    }

    private Vector3d getForcePosRelativeToAxleInShipSpace() {
        if (getRudderAxleSchematic().isPresent()) {
            Vec3i directionFacing = getRudderAxleFacingDirection().get().getDirectionVec();
            Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
            Vector3d facingOffset = new Vector3d(directionFacing.getX(), directionFacing.getY(),
                directionFacing.getZ());
            double axleLength = getRudderAxleLength().get();
            // Then estimate the torque output for both, and use the one that has a positive
            // dot product to torqueAttemptNormal.
            facingOffset.mul(axleLength / 2D);
            // Then rotate the offset vector
            AxisAngle4d rotation = new AxisAngle4d(Math.toRadians(rudderAngle), directionAxle.getX(), directionAxle.getY(), directionAxle.getZ());

            rotation.transform(facingOffset);
            return facingOffset;
        } else {
            return null;
        }
    }

    public Vector3d calculateForceFromVelocity(PhysicsObject physicsObject) {
        if (getRudderAxleSchematic().isPresent()) {
            Vector3d directionFacing = this.getForcePosRelativeToAxleInShipSpace();
            Vector3d forcePosRelativeToShipCenter = this.getForcePositionInShipSpace();
            forcePosRelativeToShipCenter.sub(physicsObject.getShipTransform().getCenterCoord());
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform()
                .transformDirection(forcePosRelativeToShipCenter, TransformType.SUBSPACE_TO_GLOBAL);

            Vector3d velocity = physicsObject.getPhysicsCalculations()
                .getVelocityAtPoint(forcePosRelativeToShipCenter);
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform()
                .transformDirection(velocity, TransformType.GLOBAL_TO_SUBSPACE);
            // Now we have the velocity in local, the position in local, and the position relative to the axle
            Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
            Vector3d directionAxleVector = new Vector3d(directionAxle.getX(), directionAxle.getY(), directionAxle.getZ());

            Vector3d surfaceNormal = directionAxleVector.cross(directionFacing, new Vector3d());
            surfaceNormal.normalize();

            double dragMagnitude = surfaceNormal.dot(velocity) * 10000;
            return surfaceNormal.mul(-dragMagnitude);
        } else {
            return null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        rudderAngle = compound.getDouble("rudderAngle");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound toReturn = super.writeToNBT(compound);
        toReturn.setDouble("rudderAngle", rudderAngle);
        return toReturn;
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
        net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        double currentRudderAngle = rudderAngle;
        super.onDataPacket(net, pkt);
        nextRudderAngle = pkt.getNbtCompound().getDouble("rudderAngle");
        this.rudderAngle = currentRudderAngle;
    }

    @Override
    public Vector3dc getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
        return null;
    }

    @Override
    public Vector3dc getForceOutputNormal(double secondsToApply, PhysicsObject object) {
        return null;
    }

    @Override
    public double getThrustMagnitude(PhysicsObject physicsObject) {
        return 0;
    }

    public Optional<EnumFacing> getRudderAxleAxisDirection() {
        Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
        if (rudderAxleSchematicOptional.isPresent()) {
            return Optional.of(rudderAxleSchematicOptional.get().getAxleAxisDirection());
        } else {
            return Optional.empty();
        }
    }

    public Optional<EnumFacing> getRudderAxleFacingDirection() {
        Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
        if (rudderAxleSchematicOptional.isPresent()) {
            return Optional.of(rudderAxleSchematicOptional.get().getAxleFacingDirection());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Integer> getRudderAxleLength() {
        Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
        if (rudderAxleSchematicOptional.isPresent()) {
            return Optional.of(rudderAxleSchematicOptional.get().getAxleLength());
        } else {
            return Optional.empty();
        }
    }

    private Optional<RudderAxleMultiblockSchematic> getRudderAxleSchematic() {
        if (this.isPartOfAssembledMultiblock()) {
            return Optional.of(getMultiBlockSchematic());
        } else {
            return Optional.empty();
        }
    }

    public double getRudderAngle() {
        return this.rudderAngle;
    }

    public void setRudderAngle(double forcedValue) {
        this.rudderAngle = forcedValue;
        VSNetwork.sendTileToAllNearby(this);
    }

    public double getRenderRudderAngle(double partialTicks) {
        return this.prevRudderAngle + ((this.rudderAngle - this.prevRudderAngle) * partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.isPartOfAssembledMultiblock() && this.isMaster() && getRudderAxleAxisDirection()
            .isPresent()) {
            BlockPos minPos = this.pos;
            EnumFacing axleAxis = getRudderAxleAxisDirection().get();
            EnumFacing axleFacing = getRudderAxleFacingDirection().get();
            Vec3i otherAxis = axleAxis.getDirectionVec().crossProduct(axleFacing.getDirectionVec());

            int nexAxisX = axleAxis.getDirectionVec().getX() + axleFacing.getDirectionVec().getX();
            int nexAxisY = axleAxis.getDirectionVec().getY() + axleFacing.getDirectionVec().getY();
            int nexAxisZ = axleAxis.getDirectionVec().getZ() + axleFacing.getDirectionVec().getZ();

            int axleLength = getRudderAxleLength().get();

            int offsetX = nexAxisX * axleLength;
            int offsetY = nexAxisY * axleLength;
            int offsetZ = nexAxisZ * axleLength;

            BlockPos maxPos = minPos.add(offsetX, offsetY, offsetZ);

            int otherAxisXExpanded = otherAxis.getX() * axleLength;
            int otherAxisYExpanded = otherAxis.getY() * axleLength;
            int otherAxisZExpanded = otherAxis.getZ() * axleLength;

            return new AxisAlignedBB(minPos, maxPos)
                .grow(otherAxisXExpanded, otherAxisYExpanded, otherAxisZExpanded)
                .grow(.5, .5, .5);
        } else {
            return super.getRenderBoundingBox();
        }
    }

    @Override
    public boolean attemptToAssembleMultiblock(World worldIn, BlockPos pos, EnumFacing facing) {
        List<IMultiblockSchematic> schematics = MultiblockRegistry.getSchematicsWithPrefix("multiblock_rudder_axle");
        for (IMultiblockSchematic schematic : schematics) {
            RudderAxleMultiblockSchematic rudderSchem = (RudderAxleMultiblockSchematic) schematic;
            if (facing.getAxis() != rudderSchem.getAxleAxisDirection().getAxis()
                && rudderSchem.getAxleFacingDirection() == facing
                && schematic.attemptToCreateMultiblock(worldIn, pos)) {
                return true;
            }
        }
        return false;
    }
}
