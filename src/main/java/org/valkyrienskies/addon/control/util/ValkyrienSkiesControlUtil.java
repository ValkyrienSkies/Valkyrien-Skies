package org.valkyrienskies.addon.control.util;

import net.minecraft.world.World;
import org.valkyrienskies.addon.control.block.torque.IRotationNodeWorld;
import org.valkyrienskies.addon.control.block.torque.ImplRotationNodeWorld;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

import javax.annotation.Nonnull;
import java.util.WeakHashMap;

public class ValkyrienSkiesControlUtil {

    private static final WeakHashMap<World, IRotationNodeWorld> WORLD_TO_ROT_WORLD = new WeakHashMap<>();
    private static final WeakHashMap<PhysicsObject, IRotationNodeWorld> SHIP_TO_ROT_WORLD = new WeakHashMap<>();

    @Nonnull
    public static IRotationNodeWorld getRotationWorldFromShip(@Nonnull final PhysicsObject physicsObject) {
        if (physicsObject.getWorld().isRemote) {
            throw new IllegalArgumentException("Clients don't have rotation node worlds!");
        }
        if (!SHIP_TO_ROT_WORLD.containsKey(physicsObject))
            SHIP_TO_ROT_WORLD.put(physicsObject, new ImplRotationNodeWorld(physicsObject));
        return SHIP_TO_ROT_WORLD.get(physicsObject);
    }

    @Nonnull
    public static IRotationNodeWorld getRotationWorldFromWorld(@Nonnull final World world) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Clients don't have rotation node worlds!");
        }
        if (!WORLD_TO_ROT_WORLD.containsKey(world))
            WORLD_TO_ROT_WORLD.put(world, new ImplRotationNodeWorld(world));
        return WORLD_TO_ROT_WORLD.get(world);
    }

}
