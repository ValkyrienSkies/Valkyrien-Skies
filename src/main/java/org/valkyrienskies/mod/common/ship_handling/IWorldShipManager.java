package org.valkyrienskies.mod.common.ship_handling;

import net.minecraft.world.World;

public interface IWorldShipManager {

    World getWorld();

    void tick();

    void onWorldUnload();

}
