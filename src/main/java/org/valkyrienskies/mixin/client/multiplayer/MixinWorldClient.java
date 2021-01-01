package org.valkyrienskies.mixin.client.multiplayer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The purpose of this mixin is to add torch particles to ships.
 */
@Mixin(WorldClient.class)
public class MixinWorldClient {

    @Shadow
    @Final
    private Minecraft mc;

    private final WorldClient thisAsWorldClient = WorldClient.class.cast(this);

    /**
     * Adds torch particles to ships.
     * @author Tri0de
     */
    @Overwrite
    public void doVoidFogParticles(int posX, int posY, int posZ) {
        final int i = 32;
        final Random random = ThreadLocalRandom.current();
        ItemStack itemstack = this.mc.player.getHeldItemMainhand();
        boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        final AxisAlignedBB shipDetectionBB = new AxisAlignedBB(posX - i, posY - i, posZ - i, posX + i, posY + i, posZ + i);

        final List<PhysicsObject> nearbyShipObjects = ValkyrienUtils.getPhysObjWorld(thisAsWorldClient).getPhysObjectsInAABB(shipDetectionBB);

        final Vector3d temp0 = new Vector3d();

        for (int j = 0; j < 667; ++j) {
            this.vs_showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos, nearbyShipObjects, temp0);
            this.vs_showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos, nearbyShipObjects, temp0);
        }
    }

    private void vs_showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos,
                                         final List<PhysicsObject> nearbyShipObjects, final Vector3d temp0) {
        final int i = x + random.nextInt(offset) - random.nextInt(offset);
        final int j = y + random.nextInt(offset) - random.nextInt(offset);
        final int k = z + random.nextInt(offset) - random.nextInt(offset);
        pos.setPos(i, j, k);

        vs_displayTickPos(pos, holdingBarrier, random);

        for (final PhysicsObject physicsObject : nearbyShipObjects) {
            if (!aabbContains(physicsObject.getShipBB(), i, j, k)) {
                continue;
            }
            final Vector3dc posInShip = physicsObject.getShipTransformationManager().getRenderTransform().transformPositionNew(
                    temp0.set(i + .5, j + .5, k + .5), TransformType.GLOBAL_TO_SUBSPACE
            );

            final double randomXOffset = random.nextDouble();
            final double randomYOffset = random.nextDouble();
            final double randomZOffset = random.nextDouble();

            pos.setPos(posInShip.x() + randomXOffset, posInShip.y() + randomYOffset, posInShip.z() + randomZOffset);
            vs_displayTickPos(pos, holdingBarrier, random);
        }
    }

    private void vs_displayTickPos(final BlockPos pos, final boolean holdingBarrier, final Random random) {
        final IBlockState iblockstate = thisAsWorldClient.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, thisAsWorldClient, pos, random);
        if (holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
            thisAsWorldClient.spawnParticle(EnumParticleTypes.BARRIER, (float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F, (float) pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D);
        }
    }

    private static boolean aabbContains(final AxisAlignedBB alignedBB, final int x, final int y, final int z) {
        if (x > alignedBB.minX && x < alignedBB.maxX) {
            if (y > alignedBB.minY && y < alignedBB.maxY) {
                return z > alignedBB.minZ && z < alignedBB.maxZ;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
