package org.valkyrienskies.mod.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import org.joml.Vector3d;

@UtilityClass
public class VSRenderUtils {

    public static Vector3d getEntityPartialPosition(Entity entity, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        return new Vector3d(x, y, z);
    }

}
