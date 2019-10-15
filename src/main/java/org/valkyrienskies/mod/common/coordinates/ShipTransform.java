/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.coordinates;

import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import lombok.extern.log4j.Log4j2;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.valkyrienskies.mod.common.math.Quaternion;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;
import valkyrienwarfare.api.TransformType;

/**
 * Immutable wrapper around the rotation matrices used by ships. The immutability is extremely
 * important to enforce for preventing multi-threaded access issues. All access to the internal
 * arrays is blocked to guarantee nothing goes wrong.
 * <p>
 * Used to transform vectors between the global coordinate system, and the subspace (ship)
 * coordinate system. TODO: Move this to VS API.
 *
 * @author thebest108
 */
@Immutable
@Log4j2
public class ShipTransform {

    private final double[] subspaceToGlobal;
    private final double[] globalToSubspace;

    /**
     * Don't use, we're planning on moving the math to a proper library eventually.
     *
     * @param subspaceToGlobal
     */
    @Deprecated
    public ShipTransform(double[] subspaceToGlobal) {
        this.subspaceToGlobal = subspaceToGlobal;
        this.globalToSubspace = RotationMatrices.inverse(subspaceToGlobal);
    }

    /**
     * Creates a new ship transform that moves positions from the current transform to the next
     * one.
     *
     * @param current
     * @param next
     */
    public ShipTransform(ShipTransform current, ShipTransform next) {
        double[] currentWorldToLocal = current.globalToSubspace;
        double[] nextLocaltoWorld = next.subspaceToGlobal;
        double[] currentWorldToNextWorld = RotationMatrices
            .getMatrixProduct(nextLocaltoWorld, currentWorldToLocal);
        this.subspaceToGlobal = currentWorldToNextWorld;
        this.globalToSubspace = RotationMatrices.inverse(subspaceToGlobal);
    }

    public ShipTransform(double posX, double posY, double posZ, double pitch, double yaw,
        double roll, Vector centerCoord) {
        double[] lToWTransform = RotationMatrices.getTranslationMatrix(posX, posY, posZ);
        lToWTransform = RotationMatrices
            .rotateAndTranslate(lToWTransform, pitch, yaw, roll, centerCoord);
        this.subspaceToGlobal = lToWTransform;
        this.globalToSubspace = RotationMatrices.inverse(subspaceToGlobal);
    }

    public ShipTransform(double translateX, double translateY, double translateZ) {
        this(translateX, translateY, translateZ, 0, 0, 0, new Vector());
    }

    /**
     * Initializes an empty ShipTransform that does no translation or rotation.
     */
    public ShipTransform() {
        this(RotationMatrices.getDoubleIdentity());
    }

    public static ShipTransform createRotationTransform(double pitch, double yaw, double roll) {
        double[] rotationOnlyMatrix = RotationMatrices.getRotationMatrix(pitch, yaw, roll);
        return new ShipTransform(rotationOnlyMatrix);
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
        return new BlockPos(blockPosAsVector.x - .5D, blockPosAsVector.y - .5D,
            blockPosAsVector.z - .5D);
    }

    public Quaternion createRotationQuaternion(TransformType transformType) {
        return Quaternion.QuaternionFromMatrix(getInternalMatrix(transformType));
    }

    public void writeToNBT(NBTTagCompound compound, String name) {
        compound.setByteArray(name, ValkyrienNBTUtils.toByteArray(subspaceToGlobal));
    }

    @Nullable
    public static ShipTransform readFromNBT(NBTTagCompound compound, String name) {
        byte[] localToGlobalAsBytes = compound.getByteArray(name);
        if (localToGlobalAsBytes.length == 0) {
            log.error("Loading from the ShipTransform has failed, now we are forced to fallback on " +
                    "Vanilla MC positions. This probably won't go well at all!");
            return null;
        }
        double[] localToGlobalInternalArray = ValkyrienNBTUtils.toDoubleArray(localToGlobalAsBytes);
        return new ShipTransform(localToGlobalInternalArray);
    }

    public VectorImmutable transform(VectorImmutable vector, TransformType transformType) {
        Vector vectorMutable = vector.createMutableVectorCopy();
        this.transform(vectorMutable, transformType);
        return vectorMutable.toImmutable();
    }

    public VectorImmutable rotate(VectorImmutable vector, TransformType transformType) {
        Vector vectorMutable = vector.createMutableVectorCopy();
        this.rotate(vectorMutable, transformType);
        return vectorMutable.toImmutable();
    }

    /**
     * Please do not ever use this unless it is absolutely necessary! This exposes implementation
     * details that will be changed eventually.
     *
     * @param transformType
     * @return Unsafe internal arrays; for the love of god do not modify them!
     */
    @Deprecated
    private double[] getInternalMatrix(TransformType transformType) {
        switch (transformType) {
            case SUBSPACE_TO_GLOBAL:
                return Arrays.copyOf(subspaceToGlobal, subspaceToGlobal.length);
            case GLOBAL_TO_SUBSPACE:
                return Arrays.copyOf(globalToSubspace, globalToSubspace.length);
            default:
                throw new IllegalArgumentException(
                    "Unexpected TransformType Enum: " + transformType);
        }
    }

    @Deprecated
    public void transform(Entity entity, TransformType subspaceToGlobal) {
        RotationMatrices.applyTransform(this, entity, subspaceToGlobal);
    }

    /**
     * Creates a standard 3x3 rotation matrix for this transform and the given transform type.
     *
     * @param transformType
     * @return
     */
    public Matrix3d createRotationMatrix(TransformType transformType) {
        double[] internalRotationMatrix = getInternalMatrix(transformType);
        return new Matrix3d(internalRotationMatrix[0], internalRotationMatrix[1],
            internalRotationMatrix[2], internalRotationMatrix[4], internalRotationMatrix[5],
            internalRotationMatrix[6], internalRotationMatrix[8], internalRotationMatrix[9],
            internalRotationMatrix[10]);
    }

    public Matrix4d createTransformMatrix(TransformType transformType) {
        double[] internalMatrix = getInternalMatrix(transformType);
        return new Matrix4d(internalMatrix);
    }

    @Deprecated
    public float[] generateFastRawTransformMatrix(TransformType transformType) {
        double[] internalMatrix = getInternalMatrix(transformType);
        float[] floatMatrix = new float[internalMatrix.length];
        for (int i = 0; i < internalMatrix.length; i++) {
            floatMatrix[i] = (float) internalMatrix[i];
        }
        return floatMatrix;
    }
}
