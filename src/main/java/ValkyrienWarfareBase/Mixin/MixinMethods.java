package ValkyrienWarfareBase.Mixin;

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
    public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
        try {
            Method rayTraceBlocksIgnoreShip = World.class.getMethod("rayTraceBlocksIgnoreShip", Vec3d.class, Vec3d.class, Boolean.class, Boolean.class, Boolean.class, PhysicsWrapperEntity.class);
            return (RayTraceResult) rayTraceBlocksIgnoreShip.invoke(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, toIgnore);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)   {
            e.printStackTrace();
        }
        return null;
    }
}
