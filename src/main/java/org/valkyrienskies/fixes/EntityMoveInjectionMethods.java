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

package org.valkyrienskies.fixes;

import java.util.Optional;

import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.EntityCollisionInjector;
import org.valkyrienskies.mod.common.physics.collision.EntityCollisionInjector.IntermediateMovementVariableStorage;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.api.TransformType;

/**
 * This class used to do more (We made Entity.java extend this class in the past); but after tons of
 * refactors this class only exists to hold static methods.
 */
public class EntityMoveInjectionMethods {

    public static IntermediateMovementVariableStorage handleMove(MoverType type, double dx,
        double dy, double dz, Entity this_) {
        if (this_ instanceof EntityPlayer && ((EntityPlayer) this_).isSpectator()) {
            return null;
        }

        double movDistSq = (dx * dx) + (dy * dy) + (dz * dz);

        if (movDistSq > 10000) {
            // Assume this_ will take us to Ship coordinates
            double newX = this_.posX + dx;
            double newY = this_.posY + dy;
            double newZ = this_.posZ + dz;
            BlockPos newPosInBlock = new BlockPos(newX, newY, newZ);
            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(this_.world, newPosInBlock);

            if (!physicsObject.isPresent()) {
                return null;
            }

            Vector endPos = new Vector(newX, newY, newZ);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transform(endPos, TransformType.GLOBAL_TO_SUBSPACE);
            dx = endPos.x - this_.posX;
            dy = endPos.y - this_.posY;
            dz = endPos.z - this_.posZ;
        }

        IntermediateMovementVariableStorage alteredMovement = EntityCollisionInjector
            .alterEntityMovement(this_, type, dx, dy, dz);

        return alteredMovement;
    }

}
