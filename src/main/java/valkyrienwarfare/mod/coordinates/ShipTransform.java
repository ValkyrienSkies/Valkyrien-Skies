/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.coordinates;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import scala.actors.threadpool.Arrays;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.math.RotationMatrices;
import valkyrienwarfare.math.Vector;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable wrapper around the rotation matrices used by ships. The
 * immutability is extremely important to enforce for preventing multi-threaded
 * access issues. All access to the internal arrays is blocked to guarantee
 * nothing goes wrong.
 * <p>
 * Used to transform vectors between the global coordinate system, and the subspace
 * (ship) coordinate system.
 *
 * @author thebest108
 */
@Immutable
public class ShipTransform {

    private final double[] subspaceToGlobal;
    private final double[] globalToSubspace;

    public ShipTransform(double[] subspaceToGlobal) {
        this.subspaceToGlobal = subspaceToGlobal;
        this.globalToSubspace = RotationMatrices.inverse(subspaceToGlobal);
    }

    public ShipTransform(double posX, double posY, double posZ, double pitch, double yaw, double roll, Vector centerCoord) {
        double[] lToWTransform = RotationMatrices.getTranslationMatrix(posX, posY, posZ);
        lToWTransform = RotationMatrices.rotateAndTranslate(lToWTransform, pitch, yaw, roll, centerCoord);
        this.subspaceToGlobal = lToWTransform;
        this.globalToSubspace = RotationMatrices.inverse(subspaceToGlobal);
    }

    /**
     * Initializes an empty ShipTransform that does no translation or rotation.
     */
    public ShipTransform() {
        this(RotationMatrices.getDoubleIdentity());
    }

    /**
     * Initializes this as a copy of the given ShipTransform.
     *
     * @param toCopy
     */
    public ShipTransform(ShipTransform toCopy) {
        this.subspaceToGlobal = Arrays.copyOf(toCopy.subspaceToGlobal, toCopy.subspaceToGlobal.length);
        this.globalToSubspace = Arrays.copyOf(toCopy.globalToSubspace, toCopy.globalToSubspace.length);
    }

    public void transform(Vector vector, TransformType transformType) {
        RotationMatrices.applyTransform(getInternalMatrix(transformType), vector);
    }

    public void rotate(Vector vector, TransformType transformType) {
        RotationMatrices.doRotationOnly(getInternalMatrix(transformType), vector);
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

    public BlockPos transform(BlockPos pos, TransformType transformType) {
        Vector blockPosAsVector = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
        transform(blockPosAsVector, transformType);
        return new BlockPos(blockPosAsVector.X - .5D, blockPosAsVector.Y - .5D, blockPosAsVector.Z - .5D);
    }

    public Quaternion createRotationQuaternion(TransformType transformType) {
        return Quaternion.QuaternionFromMatrix(getInternalMatrix(transformType));
    }

    /**
     * Please do not ever use this unless it is absolutely necessary! This exposes
     * the internal arrays and they unfortunately cannot be made safe without
     * sacrificing a lot of performance.
     *
     * @param transformType
     * @return Unsafe internal arrays; for the love of god do not modify them!
     */
    @Deprecated
    public double[] getInternalMatrix(TransformType transformType) {
        if (transformType == TransformType.SUBSPACE_TO_GLOBAL) {
            return subspaceToGlobal;
        } else if (transformType == TransformType.GLOBAL_TO_SUBSPACE) {
            return globalToSubspace;
        } else {
            throw new IllegalArgumentException("Unexpected TransformType Enum " + transformType + "!");
        }
    }

    public VectorImmutable transform(VectorImmutable vector, TransformType transformType) {
        Vector vectorMutable = vector.createMutibleVectorCopy();
        this.transform(vectorMutable, transformType);
        return vectorMutable.toImmutable();
    }

    public VectorImmutable rotate(VectorImmutable vector, TransformType transformType) {
        Vector vectorMutable = vector.createMutibleVectorCopy();
        this.rotate(vectorMutable, transformType);
        return vectorMutable.toImmutable();
    }
}
