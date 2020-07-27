package org.valkyrienskies.mod.common.ships.interpolation;

import net.minecraft.util.math.AxisAlignedBB;
import org.joml.*;
import org.valkyrienskies.mod.common.collision.Polygon;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nonnull;

/**
 * A simple implementation of ITransformInterpolator that uses an exponential moving average filter to smooth out the
 * movement of ships.
 */
public class SimpleEMATransformInterpolator implements ITransformInterpolator {

    // The current tick transform
    @Nonnull
    private ShipTransform curTickTransform;
    @Nonnull
    private AxisAlignedBB curAABB;
    // The latest received transform
    @Nonnull
    private ShipTransform latestReceivedTransform;
    @Nonnull
    private AxisAlignedBB latestRecievedAABB;
    // The alpha value used in the exponential moving average calculation. The greater it is the closer the ship is to
    // its actual value, the lower it is the smoother the movement of the ship.
    private final double filterAlpha;
    private static final double DOUBLE_EQUALS_THRESHOLD = 1e-6;

    public SimpleEMATransformInterpolator(@Nonnull ShipTransform initial, @Nonnull AxisAlignedBB initialAABB, double filterAlpha) {
        this.curTickTransform = initial;
        this.latestReceivedTransform = initial;
        this.curAABB = initialAABB;
        this.latestRecievedAABB = initialAABB;
        this.filterAlpha = filterAlpha;
    }

    @Override
    public void onNewTransformPacket(@Nonnull ShipTransform newTransform, @Nonnull AxisAlignedBB newAABB) {
        this.latestReceivedTransform = newTransform;
        this.latestRecievedAABB = newAABB;
    }

    @Override
    public void tickTransformInterpolator() {
        // First compute the new position
        Vector3dc curPos = new Vector3d(curTickTransform.getPosX(), curTickTransform.getPosY(), curTickTransform.getPosZ());
        // If the new center coord != current center coord, then offset the current position by the difference
        if (!latestReceivedTransform.getCenterCoord().equals(curTickTransform.getCenterCoord(), DOUBLE_EQUALS_THRESHOLD)) {
            // Add newCenter - oldCenter to the curPos
            Vector3d offset = latestReceivedTransform.getCenterCoord().sub(curTickTransform.getCenterCoord(), new Vector3d());
            curTickTransform.transformDirection(offset, TransformType.SUBSPACE_TO_GLOBAL);
            curPos = curPos.add(offset, new Vector3d());
        }
        Vector3dc latestDataPos = new Vector3d(latestReceivedTransform.getPosX(), latestReceivedTransform.getPosY(), latestReceivedTransform.getPosZ());
        Vector3dc newPos = curPos.lerp(latestDataPos, filterAlpha, new Vector3d());

        // Then compute the new rotation
        Quaterniondc curRot = curTickTransform.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaterniondc latestDataRot = latestReceivedTransform.rotationQuaternion(TransformType.SUBSPACE_TO_GLOBAL);
        Quaterniondc newRot = curRot.slerp(latestDataRot, filterAlpha, new Quaterniond()).normalize();

        // Then create the new transform using the new position and rotation (and center coord)
        curTickTransform = new ShipTransform(newPos, newRot, latestReceivedTransform.getCenterCoord());
    }

    @Override
    @Nonnull
    public ShipTransform getCurrentTickTransform() {
        return curTickTransform;
    }

    @Override
    @Nonnull
    public AxisAlignedBB getCurrentAABB() {
        Matrix4dc latestToCurrent = curTickTransform.getSubspaceToGlobal().mul(latestReceivedTransform.getGlobalToSubspace(), new Matrix4d());
        Polygon latestBB = new Polygon(latestRecievedAABB, latestToCurrent);
        return latestBB.getEnclosedAABB();
    }
}
