package ValkyrienWarfareBase.API;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A way of accessing some methods that we're injecting using mixins
 *
 * @author DaPorkchop_
 */
public class MixinMethods {
    private static Method rayTraceBlocksIgnoreShip = null;

    public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
        try {
            if (rayTraceBlocksIgnoreShip == null)   {
                rayTraceBlocksIgnoreShip = World.class.getMethod("rayTraceBlocksIgnoreShip", Vec3d.class, Vec3d.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, PhysicsWrapperEntity.class);
            }
            return (RayTraceResult) rayTraceBlocksIgnoreShip.invoke(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, toIgnore);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
