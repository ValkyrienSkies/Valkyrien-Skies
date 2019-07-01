package valkyrienwarfare.mixin.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.mod.common.ship_handling.IHasShipManager;
import valkyrienwarfare.mod.common.ship_handling.WorldServerShipManager;

@Mixin(value = WorldServer.class)
public class MixinWorldServer implements IHasShipManager {

    private WorldServerShipManager manager = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructorFinished(CallbackInfo callbackInfo) {
        // if (PorkDB.contains(worldid) {
        // manager = PorkDB.load(worldid)
        // } else {
        manager = new WorldServerShipManager();
        // }
        manager.initializeTransients(World.class.cast(this));
    }

    @Inject(method = "flush", at = @At("HEAD"))
    private void beforeWorldUnload(CallbackInfo callbackInfo) {
        manager.onWorldUnload();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo callbackInfo) {
        manager.tick();
    }

    @Override
    public WorldServerShipManager getManager() {
        return manager;
    }
}
