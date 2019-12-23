package org.valkyrienskies.mixin.server.management;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.valkyrienskies.fixes.VSNetwork;

/**
 * As much as I don't like it, this mixin is absolutely necessary.
 */
@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    /**
     * Idk maybe Ill replace this with a pre injector. Does this even have an srg mapping?
     *
     * @author thebest108
     */
    @Overwrite
    public void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z,
        double radius, int dimension, Packet<?> packetIn) {
        VSNetwork.sendToAllNearExcept(except, x, y, z, radius, dimension, packetIn);
    }

}
