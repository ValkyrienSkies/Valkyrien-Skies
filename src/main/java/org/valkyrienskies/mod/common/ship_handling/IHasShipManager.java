package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.world.World;

import java.util.function.Function;

/**
 * Used to get the ship manager from world objects.
 */
public interface IHasShipManager {

    IPhysObjectWorld getManager();

    void setManager(Function<World, IPhysObjectWorld> managerSupplier);

}
