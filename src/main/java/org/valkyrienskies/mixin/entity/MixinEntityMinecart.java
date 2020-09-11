package org.valkyrienskies.mixin.entity;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Mixin(EntityMinecart.class)
public class MixinEntityMinecart {

    private final EntityMinecart self = EntityMinecart.class.cast(this);

    private ShipTransform transform = null;
    private boolean isInGlobal = true;

    @Inject(
        method = "onUpdate",
        at = @At("HEAD")
    )
    public void preOnUpdate(CallbackInfo ci) {
        if (!VSConfig.minecartsOnShips) return;
        if (self.world.isRemote) return;
        moveToSubspace();
    }

    @Inject(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/item/EntityMinecart;doBlockCollisions()V"
        )
    )
    public void preBlockCollisions(CallbackInfo ci) {
        if (!VSConfig.minecartsOnShips) return;
        if (self.world.isRemote) return;
        moveToGlobal();
    }

    @Inject(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/item/EntityMinecart;moveDerailedMinecart()V"
        )
    )
    public void preMoveDerailed(CallbackInfo ci) {
        if (!VSConfig.minecartsOnShips) return;
        if (self.world.isRemote) return;
        moveToGlobal();
    }

    private void moveToSubspace() {
        Vec3d position = self.getPositionVector();
        for (PhysicsObject ship : ValkyrienUtils.getPhysosLoadedInWorld(self.world)) {
            if (ship.getShipBB().contains(position)) {
                transform = ship.getShipTransform();

                transformThis(transform.getGlobalToSubspace());
                isInGlobal = false;

                return;
            }
        }
    }
    
    private void moveToGlobal() {
        if (!isInGlobal) {
            transformThis(transform.getSubspaceToGlobal());
            isInGlobal = true;
        }
    }

    private void transformThis(Matrix4dc transform) {
        Vector3d pos = transform.transformPosition(JOML.convert(self.getPositionVector()));
        Vector3d lastPos = transform.transformPosition(new Vector3d(self.lastTickPosX, self.lastTickPosY, self.lastTickPosZ));

        self.setPosition(pos.x, pos.y, pos.z);
        self.lastTickPosX = lastPos.x;
        self.lastTickPosY = lastPos.y;
        self.lastTickPosZ = lastPos.z;
    }

}
