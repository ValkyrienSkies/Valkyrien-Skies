package valkyrienwarfare.mod.common.ship_handling;

import net.minecraft.world.World;

interface IWorldShipManager {

    World getWorld();

    void tick();

    void onWorldUnload();

}
