package org.valkyrienskies.mod.common.ship_handling;

import java.util.function.Function;
import net.minecraft.world.World;

/**
 * Used to get the ship manager from world objects.
 */
public interface IHasShipManager {

    IWorldShipManager getManager();

    void setManager(Function<World, IWorldShipManager> managerSupplier);

}
