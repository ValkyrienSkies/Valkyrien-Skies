package valkyrienwarfare.physics.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.Quaternion;

/**
 * Immutable wrapper around the rotation matrices used by ships. The
 * immutability is extremely important to enforce for preventing multithreaded
 * access issues. All access to the internal arrays is blocked to guarantee
 * nothing goes wrong.
 * 
 * Used to transform vectors between the global coordinate system, and the local
 * (ship) coordinate system.
 * 
 * @author thebest108
 *
 */
public class ShipTransform {

    private final double[] localToGlobal;
    private final double[] globalToLocal;

    public ShipTransform(double[] localToGlobal) {
        this.localToGlobal = localToGlobal;
        this.globalToLocal = RotationMatrices.inverse(localToGlobal);
    }

    public void transform(Vector vector, TransformType transformType) {
        if (transformType == TransformType.LOCAL_TO_GLOBAL) {
            RotationMatrices.applyTransform(localToGlobal, vector);
        } else {
            RotationMatrices.applyTransform(globalToLocal, vector);
        }
    }

    public void rotate(Vector vector, TransformType transformType) {
        if (transformType == TransformType.LOCAL_TO_GLOBAL) {
            RotationMatrices.doRotationOnly(localToGlobal, vector);
        } else {
            RotationMatrices.doRotationOnly(globalToLocal, vector);
        }
    }
    
    public Vec3d transform(Vec3d vec3d, TransformType transformType) {
        Vector vec3dAsVector = new Vector(vec3d);
        transform(vec3dAsVector, transformType);
        return vec3dAsVector.toVec3d();
    }
    
    public Vec3d rotate(Vec3d vec3d, TransformType transformType) {
        Vector vec3dAsVector = new Vector(vec3d);
        rotate(vec3dAsVector, transformType);
        return vec3dAsVector.toVec3d();
    }

    public Quaternion createRotationQuaternion(TransformType transformType) {
        if (transformType == TransformType.LOCAL_TO_GLOBAL) {
            return Quaternion.QuaternionFromMatrix(localToGlobal);
        } else {
            return Quaternion.QuaternionFromMatrix(globalToLocal);
        }
    }
    
    /**
     * Please never use this except in cases where its absolutely necessary.
     * @param transformType
     * @return
     */
    @Deprecated
    public double[] getInternalMatrix(TransformType transformType) {
        if (transformType == TransformType.LOCAL_TO_GLOBAL) {
            return localToGlobal;
        } else {
            return globalToLocal;
        }
    }

    public BlockPos transform(BlockPos pos, TransformType transformType) {
        Vector blockPosAsVector = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
        transform(blockPosAsVector, transformType);
        return new BlockPos(blockPosAsVector.X - .5D, blockPosAsVector.Y - .5D, blockPosAsVector.Z - .5D);
    }
}
