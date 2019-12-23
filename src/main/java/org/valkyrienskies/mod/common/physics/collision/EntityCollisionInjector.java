package org.valkyrienskies.mod.common.physics.collision;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.mod.common.math.VSMath;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.collision.polygons.EntityPolygon;
import org.valkyrienskies.mod.common.physics.collision.polygons.EntityPolygonCollider;
import org.valkyrienskies.mod.common.physics.collision.polygons.Polygon;
import org.valkyrienskies.mod.common.physics.collision.polygons.ShipPolygon;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.interaction.EntityDraggable;
import org.valkyrienskies.mod.common.physmanagement.interaction.IDraggable;
import org.valkyrienskies.mod.common.ship_handling.IHasShipManager;
import valkyrienwarfare.api.TransformType;

public class EntityCollisionInjector {

    private static final double errorSignificance = .001D;

    // Returns false if game should use default collision
    @Nullable
    public static IntermediateMovementVariableStorage alterEntityMovement(Entity entity,
        MoverType type, double dx, double dy, double dz) {
        Vector velVec = new Vector(dx, dy, dz);
        double origDx = dx;
        double origDy = dy;
        double origDz = dz;
        double origPosX = entity.posX;
        double origPosY = entity.posY;
        double origPosZ = entity.posZ;
        boolean isLiving = entity instanceof EntityLivingBase;
        Vec3d velocity = new Vec3d(dx, dy, dz);
        EntityPolygon playerBeforeMove = new EntityPolygon(entity.getEntityBoundingBox(), entity);
        List<Polygon> colPolys = getCollidingPolygonsAndDoBlockCols(entity, velocity);

        PhysicsObject worldBelow = null;
        IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

        Vector total = new Vector();

        // Used to reset the player position after collision processing, effectively
        // using the player to integrate their velocity
        double posOffestX = 0;
        double posOffestY = 0;
        double posOffestZ = 0;

        for (Polygon poly : colPolys) {
            if (poly instanceof ShipPolygon) {
                ShipPolygon shipPoly = (ShipPolygon) poly;
                try {

                    EntityPolygonCollider fast = new EntityPolygonCollider(playerBeforeMove,
                        shipPoly, shipPoly.normals, velVec.getAddition(total));
                    if (!fast.arePolygonsSeparated()) {
                        // fastCollisions.add(fast);
                        worldBelow = shipPoly.shipFrom;

                        Vector response = fast.getCollisions()[fast.getMinDistanceIndex()]
                            .getResponse();
                        // TODO: Add more potential yResponses
                        double stepSquared = entity.stepHeight * entity.stepHeight;
                        boolean isStep = isLiving && entity.onGround;
                        if (response.y >= 0
                            && VSMath.canStandOnNormal(
                            fast.getCollisionAxes()[fast.getMinDistanceIndex()])) {
                            Vector slowButStopped = new Vector(0,
                                -fast.getCollisions()[fast.getMinDistanceIndex()]
                                    .getCollisionPenetrationDistance() / fast
                                    .getCollisionAxes()[fast.getMinDistanceIndex()].y, 0);

                            response = slowButStopped;
                        }
                        if (isStep) {
                            EntityLivingBase living = (EntityLivingBase) entity;
                            if (Math.abs(living.moveForward) > .01D
                                || Math.abs(living.moveStrafing) > .01D) {
                                for (int i = 3; i < 6; i++) {
                                    Vector tempResponse = fast.getCollisions()[i].getResponse();
                                    if (tempResponse.y > 0
                                        && VSMath.canStandOnNormal(
                                        fast.getCollisions()[i].getCollisionNormal())
                                        && tempResponse.lengthSq() < stepSquared) {
                                        if (tempResponse.lengthSq() < .1D) {
                                            // Too small to be a real step, let it through
                                            response = tempResponse;
                                        } else {
                                            // System.out.println("Try Stepping!");
                                            AxisAlignedBB axisalignedbb = entity
                                                .getEntityBoundingBox()
                                                .offset(tempResponse.x, tempResponse.y,
                                                    tempResponse.z);
                                            entity.setEntityBoundingBox(axisalignedbb);
                                            // I think this correct, but it may create more problems than it solves
                                            response.zero();
                                            entity.resetPositionToBB();
                                        }
                                        // entity.moveEntity(x, y, z);
                                        // response = tempResponse;
                                    }
                                }
                            }
                        }
                        // total.add(response);
                        if (Math.abs(response.x) > .01D) {
                            total.x += response.x;
                        }
                        if (Math.abs(response.y) > .01D) {
                            total.y += response.y;
                        }
                        if (Math.abs(response.z) > .01D) {
                            total.z += response.z;
                        }

                        entity.posX += response.x;
                        entity.posY += response.y;
                        entity.posZ += response.z;

                        posOffestX += response.x;
                        posOffestY += response.y;
                        posOffestZ += response.z;

                        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
                            .offset(response.x, response.y, response.z);
                        entity.setEntityBoundingBox(axisalignedbb);
                        entity.resetPositionToBB();

                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }

        }

        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox()
            .offset(-posOffestX, -posOffestY, -posOffestZ);
        entity.setEntityBoundingBox(axisalignedbb);
        entity.resetPositionToBB();

        draggable.setWorldBelowFeet(worldBelow);

        if (worldBelow == null) {
            return null;
        }

        dx += total.x;
        dy += total.y;
        dz += total.z;

        boolean alreadyOnGround = entity.onGround && (dy == origDy) && origDy < 0;
        Vector original = new Vector(origDx, origDy, origDz);
        Vector newMov = new Vector(dx - origDx, dy - origDy, dz - origDz);
        entity.collidedHorizontally = original.dot(newMov) < 0;
        entity.collidedVertically = isDifSignificant(dy, origDy);
        entity.onGround = entity.collidedVertically && origDy < 0 || alreadyOnGround;
        entity.collided = entity.collidedHorizontally || entity.collidedVertically;

        // entity.resetPositionToBB();

        double motionYBefore = entity.motionY;
        float oldFallDistance = entity.fallDistance;

        Vector dxyz;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            // base.motionY = dy;
            // Delete this ladder crap; need a custom solution!

            // if (base.isOnLadder()) {

            // base.motionX = MathHelper.clamp(base.motionX, -0.15000000596046448D,
            // 0.15000000596046448D);
            // base.motionZ = MathHelper.clamp(base.motionZ, -0.15000000596046448D,
            // 0.15000000596046448D);
            // base.fallDistance = 0.0F;

            // if (base.motionY < -0.15D) {
            // base.motionY = -0.15D;
            // }

            // boolean flag = base.isSneaking() && base instanceof EntityPlayer;

            // if (flag && base.motionY < 0.0D) {
            // base.motionY = 0.0D;
            // }
            // }
            dxyz = new Vector(dx, dy, dz);
        } else {
            dxyz = new Vector(dx, dy, dz);
        }

        Vector origDxyz = new Vector(origDx, origDy, origDz);
        Vector origPosXyz = new Vector(origPosX, origPosY, origPosZ);

        if (worldBelow != null && false) {
            double playerMass = 100D;
            Vector impulse = new Vector(total);
            Vector inBodyPos = new Vector(entity.posX, entity.posY, entity.posZ);

            // inBodyPos.transform(worldBelow.wrapping.coordTransform.wToLRotation);
            // impulse.transform(worldBelow.wrapping.coordTransform.wToLRotation);

            impulse.multiply(playerMass * -100D);
            // impulse.multiply();

            // PhysicsQueuedForce queuedForce = new PhysicsQueuedForce(impulse, inBodyPos,
            // false, 1);

            // worldBelow.wrapping.queueForce(queuedForce);
        }

        return new IntermediateMovementVariableStorage(dxyz, origDxyz, origPosXyz, alreadyOnGround,
            motionYBefore,
            oldFallDistance);
    }

    public static void alterEntityMovementPost(Entity entity,
        IntermediateMovementVariableStorage storage) {
        double dx = storage.dxyz.x;
        double dy = storage.dxyz.y;
        double dz = storage.dxyz.z;

        double origDx = storage.origDxyz.x;
        double origDy = storage.origDxyz.y;
        double origDz = storage.origDxyz.z;

        double origPosX = storage.origPosXyz.x;
        double origPosY = storage.origPosXyz.y;
        double origPosZ = storage.origPosXyz.z;

        boolean alreadyOnGround = storage.alreadyOnGround;
        double motionYBefore = storage.motionYBefore;
        float oldFallDistance = storage.oldFallDistance;

        IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

        PhysicsObject worldBelow = draggable.getWorldBelowFeet();

        entity.collidedHorizontally =
            (motionInterfering(dx, origDx)) || (motionInterfering(dz, origDz));
        entity.collidedVertically = isDifSignificant(dy, origDy);
        entity.onGround =
            entity.collidedVertically && origDy < 0 || alreadyOnGround || entity.onGround;
        entity.collided = entity.collidedHorizontally || entity.collidedVertically;

        Vector entityPosInShip = new Vector(entity.posX, entity.posY - 0.20000000298023224D,
            entity.posZ);

        worldBelow.getShipTransformationManager().getCurrentTickTransform()
            .transform(entityPosInShip,
                TransformType.GLOBAL_TO_SUBSPACE);

        int j4 = MathHelper.floor(entityPosInShip.x);
        int l4 = MathHelper.floor(entityPosInShip.y);
        int i5 = MathHelper.floor(entityPosInShip.z);
        BlockPos blockpos = new BlockPos(j4, l4, i5);
        IBlockState iblockstate = entity.world.getBlockState(blockpos);

        Block block = iblockstate.getBlock();

        // TODO: Use Mixins to call entity.updateFallState() instead!

        // fixes slime blocks
        if (block instanceof BlockSlime && !entity.isInWeb) {
            entity.motionY = motionYBefore;
        }

        entity.fallDistance = oldFallDistance;
        if (entity instanceof EntityLivingBase) {

            if (!entity.world.isRemote && entity.fallDistance > 3.0F && entity.onGround) {
                // System.out.println("LAND DAMNIT!");
                float f = MathHelper.ceil(entity.fallDistance - 3.0F);
                if (!iblockstate.getBlock().isAir(iblockstate, entity.world, blockpos)) {
                    double d0 = Math.min(0.2F + f / 15.0F, 2.5D);

                    int i = (int) (150.0D * d0);
                    if (!iblockstate.getBlock()
                        .addLandingEffects(iblockstate, (WorldServer) entity.world, blockpos,
                            iblockstate, (EntityLivingBase) entity, i)) {
                        ((WorldServer) entity.world)
                            .spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX,
                                entity.posY, entity.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D,
                                Block.getStateId(iblockstate));
                    }
                }
            }
        }

        if (entity.onGround) {
            if (entity.fallDistance > 0.0F) {
                // Responsible for breaking crops when you jump on them
                iblockstate.getBlock()
                    .onFallenUpon(entity.world, blockpos, entity, entity.fallDistance);
            }

            entity.fallDistance = 0.0F;
        } else if (entity.motionY < 0.0D) {
            entity.fallDistance = (float) (entity.fallDistance - entity.motionY);
        }

        if (/* entity.canTriggerWalking() **/
            entity instanceof EntityPlayer && !entity.isRiding()) {
            if (dy != origDy) {
                // if (!(entity.motionY > 0 && dy > 0)) {
                block.onLanded(entity.world, entity);
                // }
            }

            if (block != null && entity.onGround) {
                block.onEntityWalk(entity.world, blockpos, entity);
            }

            // entity.distanceWalkedModified = (float)((double)entity.distanceWalkedModified
            // + (double)MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
            // entity.distanceWalkedOnStepModified =
            // (float)((double)entity.distanceWalkedOnStepModified +
            // (double)MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

            if (entity.distanceWalkedOnStepModified > entity.nextStepDistance
                && iblockstate.getMaterial() != Material.AIR) {
                entity.nextStepDistance = (int) entity.distanceWalkedOnStepModified + 1;

                /*
                 * if (this.isInWater()) { float f = MathHelper.sqrt_double(this.motionX *
                 * this.motionX * 0.20000000298023224D + this.motionY * this.motionY +
                 * this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
                 *
                 * if (f > 1.0F) { f = 1.0F; }
                 *
                 * this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() -
                 * this.rand.nextFloat()) * 0.4F); }
                 */

                // System.out.println("Play a sound!");
                // entity.playStepSound(blockpos, block);

                // TODO: In future, replace this with entity.playStepSound()
                SoundType soundtype = block
                    .getSoundType(entity.world.getBlockState(blockpos), entity.world, blockpos,
                        entity);

                if (entity.world.getBlockState(blockpos.up()).getBlock() == Blocks.SNOW_LAYER) {
                    soundtype = Blocks.SNOW_LAYER.getSoundType();
                    entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F,
                        soundtype.getPitch());
                } else if (!block.getDefaultState().getMaterial().isLiquid()) {
                    entity.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F,
                        soundtype.getPitch());
                }
            }
        }

        if (dx != origDx) {
            entity.motionX = dx;
        }
        if (dy != origDy) {
            if (!(entity.motionY > 0 && dy > 0)) {
                entity.motionY = 0;
            }
        }
        if (dz != origDz) {
            entity.motionZ = dz;
        }
    }

    /*
     * This method generates an arrayList of Polygons that the player is colliding
     * with
     */
    public static ArrayList<Polygon> getCollidingPolygonsAndDoBlockCols(Entity entity,
        Vec3d velocity) {
        ArrayList<Polygon> collisions = new ArrayList<Polygon>();
        AxisAlignedBB entityBB = entity.getEntityBoundingBox()
            .offset(velocity.x, velocity.y, velocity.z).expand(1, 1,
                1);

        List<PhysicsObject> ships = ((IHasShipManager) entity.getEntityWorld()).getManager()
            .getNearbyPhysObjects(entityBB);
        // If a player is riding a Ship, don't process any collision between that Ship
        // and the Player
        for (PhysicsObject wrapper : ships) {
            try {
                Polygon playerInLocal = new Polygon(entityBB,
                    wrapper.getShipTransformationManager()
                        .getCurrentTickTransform(),
                    TransformType.GLOBAL_TO_SUBSPACE);
                AxisAlignedBB bb = playerInLocal.getEnclosedAABB();

                if ((bb.maxX - bb.minX) * (bb.maxZ - bb.minZ) > 9898989) {
                    // This is too big, something went wrong here
                    break;
                }
                List<AxisAlignedBB> collidingBBs = entity.world.getCollisionBoxes(entity, bb);

                // TODO: Fix the performance of this!
                if (entity.world.isRemote || entity instanceof EntityPlayer) {
                    VSMath.mergeAABBList(collidingBBs);
                }

                for (AxisAlignedBB inLocal : collidingBBs) {
                    ShipPolygon poly = new ShipPolygon(inLocal,
                        wrapper.getShipTransformationManager()
                            .getCurrentTickTransform(),
                        TransformType.SUBSPACE_TO_GLOBAL,
                        wrapper.getShipTransformationManager().normals,
                        wrapper);
                    collisions.add(poly);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (PhysicsObject wrapper : ships) {
            double posX = entity.posX;
            double posY = entity.posY;
            double posZ = entity.posZ;

            Vector entityPos = new Vector(posX, posY, posZ);

            wrapper.getShipTransformationManager().getCurrentTickTransform()
                .transform(entityPos,
                    TransformType.GLOBAL_TO_SUBSPACE);
            // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform,
            // entityPos);

            setEntityPositionAndUpdateBB(entity, entityPos.x, entityPos.y, entityPos.z);

            int entityChunkX = MathHelper.floor(entity.posX / 16.0D);
            int entityChunkZ = MathHelper.floor(entity.posZ / 16.0D);

            if (wrapper.getOwnedChunks().containsChunk(entityChunkX, entityChunkZ)) {
                Chunk chunkIn = wrapper
                    .getChunkAt(entityChunkX, entityChunkZ);

                int chunkYIndex = MathHelper.floor(entity.posY / 16.0D);

                if (chunkYIndex < 0) {
                    chunkYIndex = 0;
                }

                if (chunkYIndex >= chunkIn.entityLists.length) {
                    chunkYIndex = chunkIn.entityLists.length - 1;
                }

                chunkIn.entityLists[chunkYIndex].add(entity);
                entity.doBlockCollisions();
                chunkIn.entityLists[chunkYIndex].remove(entity);
            }

            setEntityPositionAndUpdateBB(entity, posX, posY, posZ);

        }

        return collisions;
    }

    public static void setEntityPositionAndUpdateBB(Entity entity, double x, double y, double z) {
        entity.posX = x;
        entity.posY = y;
        entity.posZ = z;
        float f = entity.width / 2.0F;
        float f1 = entity.height;
        entity.boundingBox = new AxisAlignedBB(x - f, y, z - f, x + f, y + f1, z + f);
    }

    private static boolean isDifSignificant(double dif1, double d2) {
        return !(Math.abs(dif1 - d2) < errorSignificance);
    }

    private static boolean motionInterfering(double orig, double modded) {
        return Math.signum(orig) != Math.signum(modded);
    }

    @Value
    public static class IntermediateMovementVariableStorage {

        public final Vector dxyz;
        public final Vector origDxyz;
        public final Vector origPosXyz;
        public final boolean alreadyOnGround;
        public final double motionYBefore;
        public final float oldFallDistance;

    }

}