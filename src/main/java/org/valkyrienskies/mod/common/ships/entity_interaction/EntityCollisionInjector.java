package org.valkyrienskies.mod.common.ships.entity_interaction;

import lombok.Value;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.collision.*;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.ships.ship_world.IHasShipManager;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityCollisionInjector {

    private static final double errorSignificance = .001D;

    // Returns false if game should use default collision
    @Nullable
    public static IntermediateMovementVariableStorage alterEntityMovement(Entity entity,
        MoverType type, double dx, double dy, double dz) {
        final double origDx = dx;
        final double origDy = dy;
        final double origDz = dz;
        final double origPosX = entity.posX;
        final double origPosY = entity.posY;
        final double origPosZ = entity.posZ;
        boolean isLiving = entity instanceof EntityLivingBase;
        Vec3d velocity = new Vec3d(dx, dy, dz);
        EntityPolygon playerBeforeMove = new EntityPolygon(entity.getEntityBoundingBox(), entity);
        List<Polygon> colPolys = getCollidingPolygonsAndDoBlockCols(entity, velocity);

        PhysicsObject worldBelow = null;
        IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

        Vector3d total = new Vector3d();

        // Used to reset the player position after collision processing, effectively
        // using the player to integrate their velocity
        double posOffestX = 0;
        double posOffestY = 0;
        double posOffestZ = 0;

        // True IFF the player is on a ladder
        boolean isPlayerOnLadder = false;

        // region Ladder movement
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase base = (EntityLivingBase) entity;
            final List<PhysicsObject> collidingShips = ((IHasShipManager) entity.getEntityWorld()).getManager()
                    .getNearbyPhysObjects(base.getEntityBoundingBox());
            final Iterable<Triple<PhysicsObject, BlockPos, IBlockState>> ladderCollisions = getLadderCollisions(base, collidingShips);
            // For now, just ignore the y component. I may or may not use it later.

            final float forward = ((EntityLivingBase) entity).moveForward;
            final float strafe = ((EntityLivingBase) entity).moveStrafing;

            final double f1 = Math.sin(Math.toRadians(entity.rotationYaw));
            final double f2 = Math.cos(Math.toRadians(entity.rotationYaw));
            final double intendedXVel = strafe * f2 - forward * f1;
            final double intendedYVel = 0;
            final double intendedZVel = forward * f2 + strafe * f1;

            final Vector3dc originalVelocityDirection = new Vector3d(intendedXVel, intendedYVel, intendedZVel).normalize();
            final World world = entity.world;
            final Polygon playerPolygon = new Polygon(base.getEntityBoundingBox());

            for (final Triple<PhysicsObject, BlockPos, IBlockState> ladderCollision : ladderCollisions) {
                final IBlockState ladderState = ladderCollision.getRight();

                EnumFacing ladderFacing = null;
                // For now, we only support a few blocks
                if (ladderState.getPropertyKeys().contains(BlockHorizontal.FACING)) {
                    ladderFacing = ladderState.getValue(BlockHorizontal.FACING);
                }

                // We need the EnumFacing of the ladder for the code to work. If we couldn't find it then just give up :/
                if (ladderFacing != null) {
                    final Vector3d ladderNormal = JOML.convertDouble(ladderFacing.getDirectionVec());
                    final ShipTransform shipTransform = ladderCollision.getLeft().getShipTransform();
                    // Grow the ladder BB by a small margin (makes the ladder experience better imo)
                    final AxisAlignedBB ladderBB = ladderCollision.getRight().getBoundingBox(world, ladderCollision.getMiddle()).offset(ladderCollision.getMiddle()).grow(.4);
                    final Polygon ladderPoly = new Polygon(ladderBB, shipTransform.getSubspaceToGlobal());
                    // Determine if the player is actually colliding with the ladder
                    final PhysPolygonCollider collider = new PhysPolygonCollider(playerPolygon, ladderPoly, ladderCollision.getLeft().getShipTransformationManager().normals);
                    collider.processData();

                    shipTransform.transformDirection(ladderNormal, TransformType.SUBSPACE_TO_GLOBAL);

                    // Don't use "floor ladders"
                    final boolean isLadderFacingDown = ladderNormal.y > .8;
                    if (isLadderFacingDown) {
                        continue;
                    }

                    // If the ladder is facing up, then let the player use them like monkey bars
                    final boolean isLadderFacingUp = ladderNormal.y < -.8;

                    // Whether or not the player is actually colliding with a ladder, since it is close to one we give the player ladder movement.
                    dx = MathHelper.clamp(dx, -.15, .15);
                    dz = MathHelper.clamp(dz, -.15, .15);
                    base.fallDistance = 0;

                    if (!isLadderFacingUp) {
                        // Use ladders like normal
                        if (dy < -.15) {
                            dy = -.15;
                        }

                        final boolean isPlayerGoingTowardsLadder = originalVelocityDirection.dot(ladderNormal) < -.1;
                        final boolean isPlayerSneakingOnLadder = base.isSneaking() && base instanceof EntityPlayer;

                        if (isPlayerSneakingOnLadder && dy < 0) {
                            dy = 0;
                        }
                        if (!collider.seperated && isPlayerGoingTowardsLadder) {
                            dy = .2;
                        }
                    } else {
                        // Use ladders like monkey bars
                        dy = .2;
                    }

                    worldBelow = ladderCollision.getLeft();
                    isPlayerOnLadder = true;
                    break;
                }
            }
        }
        // endregion

        final Vector3dc velVec = new Vector3d(dx, dy, dz);

        for (Polygon poly : colPolys) {
            if (poly instanceof ShipPolygon) {
                ShipPolygon shipPoly = (ShipPolygon) poly;
                try {

                    EntityPolygonCollider fast = new EntityPolygonCollider(playerBeforeMove,
                        shipPoly, shipPoly.normals, velVec.add(total, new Vector3d()));
                    if (!fast.arePolygonsSeparated()) {
                        // fastCollisions.add(fast);
                        worldBelow = shipPoly.shipFrom;

                        Vector3d response = fast.getCollisions()[fast.getMinDistanceIndex()]
                            .getResponse();
                        // TODO: Add more potential yResponses
                        double stepSquared = entity.stepHeight * entity.stepHeight;
                        // Do not do stair stepping if the player is on a ladder.
                        boolean isStep = isLiving && entity.onGround && !isPlayerOnLadder;
                        if (response.y >= 0
                            && VSMath.canStandOnNormal(
                            fast.getCollisionAxes()[fast.getMinDistanceIndex()])) {
                            Vector3d slowButStopped = new Vector3d(0,
                                -fast.getCollisions()[fast.getMinDistanceIndex()]
                                    .getCollisionPenetrationDistance() / fast
                                    .getCollisionAxes()[fast.getMinDistanceIndex()].y(), 0);

                            response = slowButStopped;
                        }
                        if (isStep) {
                            EntityLivingBase living = (EntityLivingBase) entity;
                            if (Math.abs(living.moveForward) > .01
                                || Math.abs(living.moveStrafing) > .01) {
                                for (int i = 3; i < 6; i++) {
                                    Vector3d tempResponse = fast.getCollisions()[i].getResponse();
                                    if (tempResponse.y > 0
                                        && VSMath.canStandOnNormal(
                                        fast.getCollisions()[i].getCollisionNormal())
                                        && tempResponse.lengthSquared() < stepSquared) {
                                        if (tempResponse.lengthSquared() < .1) {
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


        if (worldBelow != null) {
            draggable.setEntityShipMovementData(draggable.getEntityShipMovementData().withLastTouchedShip(worldBelow.getShipData()).withTicksSinceTouchedShip(0));
        } else {
            draggable.setEntityShipMovementData(draggable.getEntityShipMovementData().withLastTouchedShip(null));
        }

        if (worldBelow == null) {
            return null;
        }

        dx += total.x;
        dy += total.y;
        dz += total.z;

        boolean alreadyOnGround = entity.onGround && (dy == origDy) && origDy < 0;
        Vector3d original = new Vector3d(origDx, origDy, origDz);
        Vector3d newMov = new Vector3d(dx - origDx, dy - origDy, dz - origDz);
        entity.collidedHorizontally = original.dot(newMov) < 0;
        entity.collidedVertically = isDifSignificant(dy, origDy);
        entity.onGround = entity.collidedVertically && origDy < 0 || alreadyOnGround;
        entity.collided = entity.collidedHorizontally || entity.collidedVertically;

        // entity.resetPositionToBB();

        double motionYBefore = entity.motionY;
        float oldFallDistance = entity.fallDistance;

        Vector3d dxyz = new Vector3d(dx, dy, dz);;
        Vector3d origDxyz = new Vector3d(origDx, origDy, origDz);
        Vector3d origPosXyz = new Vector3d(origPosX, origPosY, origPosZ);

        return new IntermediateMovementVariableStorage(dxyz, origDxyz, origPosXyz, alreadyOnGround,
            motionYBefore,
            oldFallDistance);
    }

    public static void alterEntityMovementPost(Entity entity,
        IntermediateMovementVariableStorage storage) {
        double dx = storage.dxyz.x();
        double dy = storage.dxyz.y();
        double dz = storage.dxyz.z();

        double origDx = storage.origDxyz.x();
        double origDy = storage.origDxyz.y();
        double origDz = storage.origDxyz.z();

        double origPosX = storage.origPosXyz.x();
        double origPosY = storage.origPosXyz.y();
        double origPosZ = storage.origPosXyz.z();

        boolean alreadyOnGround = storage.alreadyOnGround;
        double motionYBefore = storage.motionYBefore;
        float oldFallDistance = storage.oldFallDistance;

        ShipData worldBelow = ValkyrienUtils.getLastShipTouchedByEntity(entity);

        entity.collidedHorizontally =
            (motionInterfering(dx, origDx)) || (motionInterfering(dz, origDz));
        entity.collidedVertically = isDifSignificant(dy, origDy);
        entity.onGround =
            entity.collidedVertically && origDy < 0 || alreadyOnGround || entity.onGround;
        entity.collided = entity.collidedHorizontally || entity.collidedVertically;

        Vector3d entityPosInShip = new Vector3d(entity.posX, entity.posY - 0.20000000298023224D,
            entity.posZ);

        worldBelow.getShipTransform()
            .transformPosition(entityPosInShip, TransformType.GLOBAL_TO_SUBSPACE);

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

            Vector3d entityPos = new Vector3d(posX, posY, posZ);

            wrapper.getShipTransformationManager().getCurrentTickTransform()
                .transformPosition(entityPos, TransformType.GLOBAL_TO_SUBSPACE);

            setEntityPositionAndUpdateBB(entity, entityPos.x, entityPos.y, entityPos.z);

            int entityChunkX = MathHelper.floor(entity.posX / 16.0D);
            int entityChunkZ = MathHelper.floor(entity.posZ / 16.0D);

            if (wrapper.getChunkClaim().containsChunk(entityChunkX, entityChunkZ)) {
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

        public final Vector3dc dxyz;
        public final Vector3dc origDxyz;
        public final Vector3dc origPosXyz;
        public final boolean alreadyOnGround;
        public final double motionYBefore;
        public final float oldFallDistance;

    }

    /**
     * Returns all the possible ladders that the entity could potentially climb with
     */
    public static Iterable<Triple<PhysicsObject, BlockPos, IBlockState>> getLadderCollisions(final EntityLivingBase entity, final List<PhysicsObject> collidingShips) {
        final boolean isSpectator = entity instanceof EntityPlayer && ((EntityPlayer)entity).isSpectator();
        final World world = entity.getEntityWorld();
        final List<Triple<PhysicsObject, BlockPos, IBlockState>> ladderCollisions = new ArrayList<>();
        if (!isSpectator) {
            final AxisAlignedBB bb = entity.getEntityBoundingBox();
            for (PhysicsObject physicsObject : collidingShips) {
                final Polygon playerPolyInShip = new Polygon(bb, physicsObject.getShipTransform(), TransformType.GLOBAL_TO_SUBSPACE);

                final AxisAlignedBB playerPolyInShipBB = playerPolyInShip.getEnclosedAABB();

                int mX = MathHelper.floor(playerPolyInShipBB.minX);
                int mY = MathHelper.floor(playerPolyInShipBB.minY);
                int mZ = MathHelper.floor(playerPolyInShipBB.minZ);

                for (int y2 = mY; (double) y2 < playerPolyInShipBB.maxY; ++y2) {
                    for (int x2 = mX; (double) x2 < playerPolyInShipBB.maxX; ++x2) {
                        for (int z2 = mZ; (double) z2 < playerPolyInShipBB.maxZ; ++z2) {
                            BlockPos tmp = new BlockPos(x2, y2, z2);
                            IBlockState state = world.getBlockState(tmp);
                            if (state.getBlock().isLadder(state, world, tmp, entity)) {
                                ladderCollisions.add(Triple.of(physicsObject, tmp, state));
                            }
                        }
                    }
                }
            }
        }
        return ladderCollisions;
    }

}