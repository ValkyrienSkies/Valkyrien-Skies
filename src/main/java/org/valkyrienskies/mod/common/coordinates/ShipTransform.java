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
import org.joml.*;
import org.valkyrienskies.mod.common.math.RotationMatrices;
import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.util.ValkyrienNBTUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.lang.Math;

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
        // First we translate the block coordinates to coordinates where center of mass is <0,0,0>
        Matrix4dc intialTranslate = new Matrix4d().translate(-centerCoord.x, -centerCoord.y, -centerCoord.z);
        // Then we rotate the coordinates based on the pitch/yaw/roll.
        Matrix4dc rotationMatrix = new Matrix4d().rotateXYZ(Math.toRadians(pitch), Math.toRadians(yaw), Math.toRadians(roll));
        // Then we translate the coordinates to where they are in the world.
        Matrix4dc finalTranslate = new Matrix4d().translate(posX, posY, posZ);

        // We use matrix multiplication to combine these three matrix operations into one.
        // (Remember that matrix multiplication is done from right to left)
        this.subspaceToGlobal = finalTranslate.mul(rotationMatrix, new Matrix4d()).mul(intialTranslate);;
        this.globalToSubspace = subspaceToGlobal.invert(new Matrix4d());
    }

    public static Matrix4d createTransform(ShipTransform prev, ShipTransform current) {
        Matrix4dc oldTransformGtoS = prev.globalToSubspace;
        Matrix4dc currentTransformStoG = current.subspaceToGlobal;
        return currentTransformStoG.mul(oldTransformGtoS, new Matrix4d());
    }

    public ShipTransform(double translateX, double translateY, double translateZ) {
        this(new Matrix4d().setTranslation(translateX, translateY, translateZ));
    }

    /**
     * Initializes an empty ShipTransform that does no translation or rotation.
     */
    public ShipTransform() {
        this.subspaceToGlobal = new Matrix4d();
        this.globalToSubspace = new Matrix4d();
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

    public void transformPosition(Vector3d position, TransformType transformType) {
        getTransformMatrix(transformType).transformPosition(position);
    }

    public void transformDirection(Vector3d direction, TransformType transformType) {
        getTransformMatrix(transformType).transformDirection(direction);
    }

    @Deprecated
    public void transform(Vector vector, TransformType transformType) {
        Vector3d copy = vector.toVector3d();
        transformPosition(copy, transformType);
        vector.setValue(copy);
    }

    public Vec3d transform(Vec3d vec3d, TransformType transformType) {
        Vector vec3dAsVector = new Vector(vec3d);
        transform(vec3dAsVector, transformType);
        return vec3dAsVector.toVec3d();
    }

    public Vec3d rotate(Vec3d vec3d, TransformType transformType) {
        Vector3d vec3dAsVector = new Vector3d(vec3d.x, vec3d.y, vec3d.z);
        transformDirection(vec3dAsVector, transformType);
        return new Vec3d(vec3dAsVector.x, vec3dAsVector.y, vec3dAsVector.z);
    }

    public BlockPos transform(BlockPos pos, TransformType transformType) {
        Vector3d blockPosAsVector = new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
        transformPosition(blockPosAsVector, transformType);
        return new BlockPos(blockPosAsVector.x - .5D, blockPosAsVector.y - .5D,
                blockPosAsVector.z - .5D);
    }

    @Deprecated
    public void rotate(Vector vector, TransformType transformType) {
        Vector3d copy = vector.toVector3d();
        transformDirection(copy, transformType);
        vector.setValue(copy);
    }

    public Quaterniond rotationQuaternion(TransformType transformType) {
        return getTransformMatrix(transformType).getNormalizedRotation(new Quaterniond());
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
     * Creates a standard 3x3 rotation matrix for this transform and the given transform type.
     *
     * @param transformType
     * @return
     */
    public Matrix3dc createRotationMatrix(TransformType transformType) {
        return getTransformMatrix(transformType).get3x3(new Matrix3d());
    }

    /**
     * Returns the same matrix this object has (not a copy). For that reason please DO NOT CAST THIS to Matrix4d.
     * Doing so would violate the contract that the internal transform never changes, so DO NOT DO IT!
     *
     * @param transformType
     * @return
     */
    public Matrix4dc getTransformMatrix(TransformType transformType) {
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

    @Deprecated
    public void transform(Entity player, TransformType globalToSubspace) {
        RotationMatrices.applyTransform(getTransformMatrix(globalToSubspace), player);
    }
}
