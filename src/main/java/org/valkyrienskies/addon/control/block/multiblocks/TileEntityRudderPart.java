package org.valkyrienskies.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.fixes.VSNetwork;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

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

    public Vector getForcePositionInShipSpace() {
        Vector facingOffset = getForcePosRelativeToAxleInShipSpace();
        if (facingOffset != null) {
            return new Vector(facingOffset.x + pos.getX() + .5, facingOffset.y + pos.getY() + .5,
                facingOffset.z + pos.getZ() + .5);
        } else {
            return null;
        }
    }

    private Vector getForcePosRelativeToAxleInShipSpace() {
        if (getRudderAxleSchematic().isPresent()) {
            Vec3i directionFacing = getRudderAxleFacingDirection().get().getDirectionVec();
            Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
            Vector facingOffset = new Vector(directionFacing.getX(), directionFacing.getY(),
                directionFacing.getZ());
            double axleLength = getRudderAxleLength().get();
            // Then estimate the torque output for both, and use the one that has a positive
            // dot product to torqueAttemptNormal.
            facingOffset.multiply(axleLength / 2D);
            // Then rotate the offset vector
            double[] rotationMatrix = RotationMatrices
                .getRotationMatrix(directionAxle.getX(), directionAxle.getY(), directionAxle.getZ(),
                    Math.toRadians(rudderAngle));
            RotationMatrices.applyTransform(rotationMatrix, facingOffset);
            return facingOffset;
        } else {
            return null;
        }
    }

    public Vector calculateForceFromVelocity(PhysicsObject physicsObject) {
        if (getRudderAxleSchematic().isPresent()) {
            Vector directionFacing = this.getForcePosRelativeToAxleInShipSpace();
            Vector forcePosRelativeToShipCenter = this.getForcePositionInShipSpace();
            forcePosRelativeToShipCenter
                    .subtract(new Vector(physicsObject.getTransform().getCenterCoord()));
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform()
                .rotate(forcePosRelativeToShipCenter, TransformType.SUBSPACE_TO_GLOBAL);

            Vector velocity = physicsObject.getPhysicsCalculations()
                .getVelocityAtPoint(forcePosRelativeToShipCenter);
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform()
                .rotate(velocity, TransformType.GLOBAL_TO_SUBSPACE);
            // Now we have the velocity in local, the position in local, and the position relative to the axle
            Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
            Vector directionAxleVector = new Vector(directionAxle);

            Vector surfaceNormal = directionAxleVector.cross(new Vector(directionFacing));
            surfaceNormal.normalize();
            double dragMagnitude = surfaceNormal.dot(velocity);

            Vector dragForceClockwise = new Vector(surfaceNormal, -dragMagnitude);
            // TODO: :(
            dragForceClockwise.multiply(100000);
            return dragForceClockwise;
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
    public Vector getForceOutputUnoriented(double secondsToApply, PhysicsObject physicsObject) {
        return null;
    }

    @Override
    public VectorImmutable getForceOutputNormal(double secondsToApply, PhysicsObject object) {
        return null;
    }

    @Override
    public double getThrustMagnitude() {
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

}
