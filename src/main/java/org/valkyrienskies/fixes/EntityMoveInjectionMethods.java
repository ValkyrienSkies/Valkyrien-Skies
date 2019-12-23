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
