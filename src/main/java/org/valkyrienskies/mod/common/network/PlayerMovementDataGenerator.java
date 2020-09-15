package org.valkyrienskies.mod.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.entity.EntityShipMovementData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.UUID;

public class PlayerMovementDataGenerator {

    /**
     * Only works on the client.
     */
    public static PlayerMovementData generatePlayerMovementDataForClient() {
        final EntityPlayerSP entityPlayer = Minecraft.getMinecraft().player;
        final EntityShipMovementData entityShipMovementData = ValkyrienUtils.getEntityShipMovementDataFor(entityPlayer);

        final ShipData lastTouchedShip = entityShipMovementData.getLastTouchedShip();
        final UUID lastTouchedShipId = lastTouchedShip != null ? lastTouchedShip.getUuid() : null;
        final Vector3d playerPosInLocal = new Vector3d(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);
        final Vector3d playerLookInLocal = JOML.convert(entityPlayer.getLook(1));
        final boolean onGround = entityPlayer.onGround;

        if (lastTouchedShip != null) {
            final ShipTransform shipTransform = lastTouchedShip.getShipTransform();
            shipTransform.transformPosition(playerPosInLocal, TransformType.GLOBAL_TO_SUBSPACE);
            shipTransform.transformDirection(playerLookInLocal, TransformType.GLOBAL_TO_SUBSPACE);
        }

        return new PlayerMovementData(
                lastTouchedShipId,
                entityShipMovementData.getTicksSinceTouchedShip(),
                entityShipMovementData.getTicksPartOfGround(),
                playerPosInLocal,
                playerLookInLocal,
                onGround
        );
    }
}
