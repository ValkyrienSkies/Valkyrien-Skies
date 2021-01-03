package org.valkyrienskies.mod.fixes.darkness_lib_fix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
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

            final int blockLight = getLightFromNeighborsFor(world, EnumSkyBlock.BLOCK, blockPosInShip);
            maxLight = Math.max(blockLight, maxLight);
        }

        return maxLight;
    }

    private static int getLightFromNeighborsFor(World world, EnumSkyBlock type, BlockPos pos) {
        if (!world.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
            return 0;
        } else {
            if (pos.getY() < 0) {
                pos = new BlockPos(pos.getX(), 0, pos.getZ());
            }

            if (!world.isValid(pos)) {
                return type.defaultLightValue;
            } else if (!world.isBlockLoaded(pos)) {
                return type.defaultLightValue;
            } else if (world.getBlockState(pos).useNeighborBrightness()) {
                int i1 = world.getLightFor(type, pos.up());
                int i = world.getLightFor(type, pos.east());
                int j = world.getLightFor(type, pos.west());
                int k = world.getLightFor(type, pos.south());
                int l = world.getLightFor(type, pos.north());

                if (i > i1) {
                    i1 = i;
                }

                if (j > i1) {
                    i1 = j;
                }

                if (k > i1) {
                    i1 = k;
                }

                if (l > i1) {
                    i1 = l;
                }

                return i1;
            } else {
                Chunk chunk = world.getChunk(pos);
                return chunk.getLightFor(type, pos);
            }
        }
    }
}
