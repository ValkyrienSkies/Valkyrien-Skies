package org.valkyrienskies.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    /**
     * This mixin fixes slabs not placing correctly on ships.
     */
    @Redirect(method = "rightClickMouse", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;processRightClickBlock(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;"
    ))
    private EnumActionResult rightClickBlockProxy(PlayerControllerMP playerControllerMP, EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {
        // Check if this is for a ship
        final Optional<ShipData> shipDataOptional = ValkyrienUtils.getShipManagingBlock(worldIn, pos);
        if (shipDataOptional.isPresent()) {
            // This ray trace was in the ship, we're going to have to mess with the hit vector
            final ShipData shipData = shipDataOptional.get();
            final ShipTransform shipTransform = shipData.getShipTransform();

            // Put the hit vector in ship coordinates
            final Vector3d hitVecInLocal = JOML.convert(vec);
            shipTransform.transformPosition(hitVecInLocal, TransformType.GLOBAL_TO_SUBSPACE);
            final Vec3d moddedHitVec = JOML.toMinecraft(hitVecInLocal);
            return playerControllerMP.processRightClickBlock(player, worldIn, pos, direction, moddedHitVec, hand);
        }
        return playerControllerMP.processRightClickBlock(player, worldIn, pos, direction, vec, hand);
    }
}
