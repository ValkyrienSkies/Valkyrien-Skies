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

package valkyrienwarfare.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.collision.EntityCollisionInjector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MixinMethods {
    public static Method rayTraceBlocksIgnoreShip = null;

    public static EntityCollisionInjector.IntermediateMovementVariableStorage handleMove(/*Args args, */MoverType type, double dx, double dy, double dz, Entity this_) {
        if (PhysicsWrapperEntity.class.isInstance(this_)) {
            //Don't move at all
            return null;
        }

        double movDistSq = (dx * dx) + (dy * dy) + (dz * dz);

        if (movDistSq > 10000) {
            //Assume this_ will take us to Ship coordinates
            double newX = this_.posX + dx;
            double newY = this_.posY + dy;
            double newZ = this_.posZ + dz;
            BlockPos newPosInBlock = new BlockPos(newX, newY, newZ);
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this_.world, newPosInBlock);

            if (wrapper == null) {
                return null;
            }

            Vector endPos = new Vector(newX, newY, newZ);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, endPos);
            dx = endPos.X - this_.posX;
            dy = endPos.Y - this_.posY;
            dz = endPos.Z - this_.posZ;
        }

        EntityCollisionInjector.IntermediateMovementVariableStorage alteredMovement = EntityCollisionInjector.alterEntityMovement(this_, type, dx, dy, dz);

        /*if (alteredMovement == null) {
            args.setAll(type, dx, dy, dz);
        } else {
            args.setAll(type, alteredMovement.dxyz.X, alteredMovement.dxyz.Y, alteredMovement.dxyz.Z);
        }*/

        return alteredMovement;
    }

    public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
        try {
            if (rayTraceBlocksIgnoreShip == null) {
                rayTraceBlocksIgnoreShip = World.class.getMethod("rayTraceBlocksIgnoreShip", Vec3d.class, Vec3d.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, PhysicsWrapperEntity.class);
            }
            return (RayTraceResult) rayTraceBlocksIgnoreShip.invoke(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, toIgnore);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
