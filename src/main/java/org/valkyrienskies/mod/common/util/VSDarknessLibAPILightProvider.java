package org.valkyrienskies.mod.common.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

import java.util.List;
import java.util.function.Function;

public class VSDarknessLibAPILightProvider implements Function<EntityPlayer, Integer> {
    @Override
    public Integer apply(final EntityPlayer entityPlayer) {
        final World world = entityPlayer.world;

        final AxisAlignedBB searchBB =
                new AxisAlignedBB(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ).grow(8);

        final List<PhysicsObject> nearbyShips = ValkyrienUtils.getPhysObjWorld(world).getPhysObjectsInAABB(searchBB);

        final Vector3d temp0 = new Vector3d();
        final BlockPos.MutableBlockPos temp1 = new BlockPos.MutableBlockPos();

        int maxLight = 0;

        for (final PhysicsObject physicsObject : nearbyShips) {
            final Vector3dc positionInShip = physicsObject.getShipTransform().transformPositionNew(temp0.set(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ), TransformType.GLOBAL_TO_SUBSPACE);
            final BlockPos blockPosInShip = temp1.setPos(positionInShip.x(), positionInShip.y(), positionInShip.z());

            final int blockLight = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, blockPosInShip);
            maxLight = Math.max(blockLight, maxLight);
        }

        return maxLight;
    }
}
