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

import lombok.extern.log4j.Log4j2;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.math.Quaternion;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

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

    private final Matrix4dc subspaceToGlobal;
    private final Matrix4dc globalToSubspace;

    /**
     * Don't use, we're planning on moving the math to a proper library eventually.
     *
     * @param doubleMatrix
     */
    @Deprecated
    public ShipTransform(double[] doubleMatrix) {
        this(VSMath.convertArrayMatrix4d(doubleMatrix));
    }

    public ShipTransform(Matrix4d sToG) {
        this.subspaceToGlobal = sToG;
        this.globalToSubspace = subspaceToGlobal.invert(new Matrix4d());
    }

    public ShipTransform(double posX, double posY, double posZ, double pitch, double yaw,
        double roll, Vector centerCoord) {
        double[] lToWTransform = RotationMatrices.getTranslationMatrix(posX, posY, posZ);
        lToWTransform = RotationMatrices
            .rotateAndTranslate(lToWTransform, pitch, yaw, roll, centerCoord);
        this.subspaceToGlobal = VSMath.convertArrayMatrix4d(lToWTransform);
        this.globalToSubspace = subspaceToGlobal.invert(new Matrix4d());
    }

    public static Matrix4d createTransform(ShipTransform prev, ShipTransform current) {
        Matrix4dc oldTransformGtoS = prev.globalToSubspace;
        Matrix4dc currentTransformStoG = current.subspaceToGlobal;
        return currentTransformStoG.mul(oldTransformGtoS, new Matrix4d());
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

    @Nullable
    public static ShipTransform readFromNBT(NBTTagCompound compound, String name) {
        byte[] localToGlobalAsBytes = compound.getByteArray(name);
        if (localToGlobalAsBytes.length == 0) {
            log.error("Loading from the ShipTransform has failed, now we are forced to fallback on " +
                    "Vanilla MC positions. This probably won't go well at all!");
            return null;
        }
        double[] localToGlobalInternalArray = ValkyrienNBTUtils.toDoubleArray(localToGlobalAsBytes);
        return new ShipTransform(new Matrix4d().set(localToGlobalInternalArray));
    }

    @Deprecated
    public void transform(Vector vector, TransformType transformType) {
        Vector3d copy = vector.toVector3d();
        getInternalMatrix(transformType).transformPosition(copy);
        vector.setValue(copy);
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

    @Deprecated
    public void rotate(Vector vector, TransformType transformType) {
        Vector3d copy = vector.toVector3d();
        getInternalMatrix(transformType).transformDirection(copy);
        vector.setValue(copy);
    }

    public Quaternion createRotationQuaternion(TransformType transformType) {
        Matrix4dc internalMatrix = getInternalMatrix(transformType);
        Matrix4dc transpose = internalMatrix.transpose(new Matrix4d());
        double[] oldCompat = new double[16];
        transpose.get(oldCompat);
        return Quaternion.QuaternionFromMatrix(oldCompat);
    }

    public void writeToNBT(NBTTagCompound compound, String name) {
        compound.setByteArray(name, ValkyrienNBTUtils.toByteArray(subspaceToGlobal.get(new double[16])));
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
    private Matrix4dc getInternalMatrix(TransformType transformType) {
        switch (transformType) {
            case SUBSPACE_TO_GLOBAL:
                return subspaceToGlobal;
            case GLOBAL_TO_SUBSPACE:
                return globalToSubspace;
            default:
                throw new IllegalArgumentException(
                    "Unexpected TransformType Enum: " + transformType);
        }
    }

    /**
     * Creates a standard 3x3 rotation matrix for this transform and the given transform type.
     *
     * @param transformType
     * @return
     */
    public Matrix3d createRotationMatrix(TransformType transformType) {
        return getInternalMatrix(transformType).get3x3(new Matrix3d());
    }

    public Matrix4dc createTransformMatrix(TransformType transformType) {
        return getInternalMatrix(transformType);
    }

    @Deprecated
    public float[] generateFastRawTransformMatrix(TransformType transformType) {
        Matrix4dc internalMatrix = getInternalMatrix(transformType);
        Matrix4dc transpose = internalMatrix.transpose(new Matrix4d());
        float[] floatMatrix = new float[16];
        transpose.get(floatMatrix);
        return floatMatrix;
    }

    @Deprecated
    public void transform(Entity player, TransformType globalToSubspace) {
        RotationMatrices.applyTransform(getInternalMatrix(globalToSubspace), player);
    }
}
