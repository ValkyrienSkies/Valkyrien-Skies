package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.VWNetwork;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;
import valkyrienwarfare.mod.common.math.RotationMatrices;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class TileEntityRudderAxlePart extends TileEntityMultiblockPartForce<RudderAxleMultiblockSchematic, TileEntityRudderAxlePart> {

    // Angle must be between -90 and 90
    private double rudderAngle;
    // For client rendering purposes only
    private double prevRudderAngle;
    private double nextRudderAngle;

    public TileEntityRudderAxlePart() {
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
            return new Vector(facingOffset.X + pos.getX() + .5, facingOffset.Y + pos.getY() + .5, facingOffset.Z + pos.getZ() + .5);
        } else {
            return null;
        }
    }

    private Vector getForcePosRelativeToAxleInShipSpace() {
        if (getRudderAxleSchematic().isPresent()) {
            Vec3i directionFacing = getRudderAxleFacingDirection().get().getDirectionVec();
            Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
            Vector facingOffset = new Vector(directionFacing.getX(), directionFacing.getY(), directionFacing.getZ());
            double axleLength = getRudderAxleLength().get();
            // Then estimate the torque output for both, and use the one that has a positive
            // dot product to torqueAttemptNormal.
            facingOffset.multiply(axleLength / 2D);
            // Then rotate the offset vector
            double[] rotationMatrix = RotationMatrices.getRotationMatrix(directionAxle.getX(), directionAxle.getY(), directionAxle.getZ(), Math.toRadians(rudderAngle));
            RotationMatrices.applyTransform(rotationMatrix, facingOffset);
            return facingOffset;
        } else {
            return null;
        }
    }

    /**
     * Well shit I hope this works.
     *
     * @param physicsObject
     * @param torqueAttemptNormal
     * @param angleDegrees
     */
    public void attemptTorque(PhysicsObject physicsObject, VectorImmutable torqueAttemptNormal, double angleDegrees, Vector helmForwardDirecton) {
        if (!this.isMaster()) {
            if (this.getRudderAxleSchematic().isPresent()) {
                BlockPos masterPos = this.getPos().add(this.getRelativePos());
                TileEntityRudderAxlePart masterTile = (TileEntityRudderAxlePart) this.getWorld().getTileEntity(masterPos);
                if (masterTile != null) {
                    masterTile.attemptTorque(physicsObject, torqueAttemptNormal, angleDegrees, helmForwardDirecton);
                }
            }
            // this.setRudderAngle(angleDegrees);
        } else {
            if (Math.abs(angleDegrees) < 1) {
                angleDegrees = 0;
            }
            // This method essentially assumes the drag to be calculated with a linear vel of helmForwardDirecton and angular vel of zero.
            if (getRudderAxleSchematic().isPresent()) {
                Vector rudderOriginInLocal = new Vector(getPos().getX() + .5, getPos().getY() + .5, getPos().getZ() + .5);
                Vector localTorqueAttempt = torqueAttemptNormal.createMutibleVectorCopy();
                rudderOriginInLocal.subtract(physicsObject.getPhysicsProcessor().gameTickCenterOfMass);
                Vec3i directionFacing = getRudderAxleFacingDirection().get().getDirectionVec();
                Vec3i directionAxle = this.getRudderAxleAxisDirection().get().getDirectionVec();
                double axleLength = getRudderAxleLength().get();
                Vector facingOffset = new Vector(directionFacing.getX(), directionFacing.getY(), directionFacing.getZ());
                Vector axleOffset = new Vector(directionAxle.getX(), directionAxle.getY(), directionAxle.getZ());
                // Then estimate the torque output for both, and use the one that has a positive
                // dot product to torqueAttemptNormal.
                axleOffset.multiply(axleLength / 2D);
                facingOffset.multiply(axleLength / 2D);
                Vector totalOffset = axleOffset.getAddition(facingOffset);

                List<Double> rudderZeroCases = new ArrayList<Double>();
                // Add the possible cases
                rudderZeroCases.add(0D);
                rudderZeroCases.add(90D);
                Map<Double, Double> zeroCasesCorrectness = new HashMap<Double, Double>();
                for (Double possibleZeroCase : rudderZeroCases) {
                    Vector possibleTorque = calculateTorqueFromAngleAndVelocity(helmForwardDirecton,
                            new Vector(directionAxle), new Vector(totalOffset), rudderOriginInLocal, possibleZeroCase);
                    zeroCasesCorrectness.put(possibleZeroCase, possibleTorque.lengthSq());
                }
                Entry<Double, Double> min = null;
                for (Entry<Double, Double> entry : zeroCasesCorrectness.entrySet()) {
                    if (min == null || min.getValue() > entry.getValue()) {
                        min = entry;
                    }
                }

                double rudderZeroTorqueAngle = min.getKey();
                double[] rotationMatrix = RotationMatrices.getRotationMatrix(directionAxle.getX(), directionAxle.getY(),
                        directionAxle.getZ(), Math.toRadians(rudderZeroTorqueAngle));
                RotationMatrices.applyTransform(rotationMatrix, totalOffset);

                List<Double> rudderRotationCases = new ArrayList<Double>();
                // Add the possible cases
                rudderRotationCases.add(0D);
                rudderRotationCases.add(angleDegrees);
                rudderRotationCases.add(-angleDegrees);

                Map<Double, Double> rotationToTorqueCorrectness = new HashMap<Double, Double>();
                for (Double possibleRotationDegrees : rudderRotationCases) {
                    // Divide by 10 because we're only checking for direction. We don't want
                    // directions changing because of amplitude.
                    Vector possibleTorque = calculateTorqueFromAngleAndVelocity(helmForwardDirecton,
                            new Vector(directionAxle), new Vector(totalOffset), rudderOriginInLocal,
                            possibleRotationDegrees / 10);
                    double angleCorrectness = possibleTorque.dot(localTorqueAttempt);
                    rotationToTorqueCorrectness.put(possibleRotationDegrees, angleCorrectness);
                }

                Entry<Double, Double> max = null;
                for (Entry<Double, Double> entry : rotationToTorqueCorrectness.entrySet()) {
                    if (max == null || max.getValue() < entry.getValue()) {
                        max = entry;
                    }
                }

                // TODO: This might not be correct. Lets just hope it is.
                angleDegrees = (max.getKey() + rudderZeroTorqueAngle);
            }

            this.setRudderAngle(angleDegrees);
            if (getMultiBlockSchematic() != null) {
                for (BlockPosBlockPair pair : getMultiBlockSchematic().getStructureRelativeToCenter()) {
                    BlockPos pos = this.getPos().add(pair.getPos());
                    TileEntityRudderAxlePart childTile = (TileEntityRudderAxlePart) this.getWorld().getTileEntity(pos);
                    if (childTile != null) {
                        childTile.setRudderAngle(angleDegrees);
                    }
                }
            }
        }
    }

    private Vector calculateTorqueFromAngleAndVelocity(Vector velocity, Vector rotationAxis, Vector forcePos, Vector rudderOriginInLocal, double angleDegrees) {
        double[] rotationMatrix = RotationMatrices.getRotationMatrix(rotationAxis.X, rotationAxis.Y, rotationAxis.Z, Math.toRadians(angleDegrees));
        Vector totalOffsetClockwise = new Vector(forcePos);
        RotationMatrices.applyTransform(rotationMatrix, totalOffsetClockwise);
        Vector forcePositionInShipSpace = rudderOriginInLocal.getAddition(totalOffsetClockwise);

        Vector surfaceNormal = totalOffsetClockwise.cross(rotationAxis);
        surfaceNormal.normalize();
        double dragMagnitude = surfaceNormal.dot(velocity);

        Vector dragForceClockwise = new Vector(surfaceNormal, -dragMagnitude);
        // Clockwise case output torque
        Vector torqueMadeClockwise = forcePositionInShipSpace.cross(dragForceClockwise);
        return torqueMadeClockwise;
    }

    public Vector calculateForceFromVelocity(PhysicsObject physicsObject) {
        if (getRudderAxleSchematic().isPresent()) {
            Vector directionFacing = this.getForcePosRelativeToAxleInShipSpace();
            Vector forcePosRelativeToShipCenter = this.getForcePositionInShipSpace();
            forcePosRelativeToShipCenter.subtract(physicsObject.getPhysicsProcessor().gameTickCenterOfMass);
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform().rotate(forcePosRelativeToShipCenter, TransformType.SUBSPACE_TO_GLOBAL);


            Vector velocity = physicsObject.getPhysicsProcessor().getVelocityAtPoint(forcePosRelativeToShipCenter);
            physicsObject.getShipTransformationManager().getCurrentPhysicsTransform().rotate(velocity, TransformType.GLOBAL_TO_SUBSPACE);
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
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
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
        VWNetwork.sendTileToAllNearby(this);
    }

    public double getRenderRudderAngle(double partialTicks) {
        return this.prevRudderAngle + ((this.rudderAngle - this.prevRudderAngle) * partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.isPartOfAssembledMultiblock() && this.isMaster() && getRudderAxleAxisDirection().isPresent()) {
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

            return new AxisAlignedBB(minPos, maxPos).grow(otherAxisXExpanded, otherAxisYExpanded, otherAxisZExpanded)
                    .grow(.5, .5, .5);
        } else {
            return super.getRenderBoundingBox();
        }
    }

}
