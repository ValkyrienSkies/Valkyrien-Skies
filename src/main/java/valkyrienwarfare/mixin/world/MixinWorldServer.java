package valkyrienwarfare.mixin.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ship_handling.IHasShipManager;
import valkyrienwarfare.ship_handling.WorldShipManager;

@Mixin(value = WorldServer.class)
public class MixinWorldServer implements IHasShipManager {

    private WorldShipManager manager = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructorFinished(CallbackInfo callbackInfo) {
        // if (PorkDB.contains(worldid) {
        // manager = PorkDB.load(worldid)
        // } else {
        manager = new WorldShipManager();
        // }
        manager.initialize(World.class.cast(this));
    }

    @Override
    public WorldShipManager getManager() {
        return manager;
    }
}
