package org.valkyrienskies.mod.common.capability.entity_backup;

import net.minecraft.entity.Entity;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class RestorableEntityBackup {

    private final Vector3dc position;
    private final Vector3dc positionLastTick;
    private final float pitch;
    private final float yaw;

    /**
     * Creates a new backup.
     *
     * @param entity The entity whose position and rotation is being recorded.
     */
    public RestorableEntityBackup(Entity entity) {
        position = new Vector3d(entity.posX, entity.posY, entity.posZ);
        positionLastTick = new Vector3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
        pitch = entity.rotationPitch;
        yaw = entity.rotationYaw;
    }

    /**
     * Restores the given entity to the position and rotation it had when this backup was created.
     *
     * @param entity The entity that will have its the position/rotation restored.
     */
    public void restoreEntityFromBackup(Entity entity) {
        entity.lastTickPosX = positionLastTick.x();
        entity.lastTickPosY = positionLastTick.y();
        entity.lastTickPosZ = positionLastTick.z();
        entity.rotationPitch = pitch;
        entity.rotationYaw = yaw;
        entity.setPosition(position.x(), position.y(), position.z());
    }
}
