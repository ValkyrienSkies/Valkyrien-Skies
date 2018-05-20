/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mixin.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.BlockMass;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.List;

@Mixin(Explosion.class)
public abstract class MixinExplosion {

    @Shadow
    @Final
    public World world;

    @Shadow
    @Final
    public float size;

    @Shadow
    @Final
    public double x;

    @Shadow
    @Final
    public double y;

    @Shadow
    @Final
    public double z;

    @Inject(method = "doExplosionA", at = @At("RETURN"))
    public void postExplosionA(CallbackInfo callbackInfo) {
        Vector center = new Vector(this.x, this.y, this.z);
        World worldIn = this.world;
        float radius = this.size;

        AxisAlignedBB toCheck = new AxisAlignedBB(center.X - radius, center.Y - radius, center.Z - radius,
                center.X + radius, center.Y + radius, center.Z + radius);
        List<PhysicsWrapperEntity> shipsNear = ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.world)
                .getNearbyPhysObjects(toCheck);
        // TODO: Make this compatible and shit!
        for (PhysicsWrapperEntity ship : shipsNear) {
            Vector inLocal = new Vector(center);
//            RotationMatrices.applyTransform(ship.wrapping.coordTransform.wToLTransform, inLocal);
            ship.wrapping.coordTransform.getCurrentTickTransform().transform(inLocal, TransformType.GLOBAL_TO_LOCAL);
            // inLocal.roundToWhole();
            Explosion expl = new Explosion(ship.world, null, inLocal.X, inLocal.Y, inLocal.Z, radius, false, false);

            double waterRange = .6D;

            boolean cancelDueToWater = false;

            for (int x = (int) Math.floor(expl.x - waterRange); x <= Math.ceil(expl.x + waterRange); x++) {
                for (int y = (int) Math.floor(expl.y - waterRange); y <= Math.ceil(expl.y + waterRange); y++) {
                    for (int z = (int) Math.floor(expl.z - waterRange); z <= Math.ceil(expl.z + waterRange); z++) {
                        if (!cancelDueToWater) {
                            IBlockState state = this.world.getBlockState(new BlockPos(x, y, z));
                            if (state.getBlock() instanceof BlockLiquid) {
                                cancelDueToWater = true;
                            }
                        }
                    }
                }
            }

            expl.doExplosionA();

            if (!cancelDueToWater) {
                for (Object o : expl.affectedBlockPositions) {
                    BlockPos pos = (BlockPos) o;

                    IBlockState state = ship.world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!block.isAir(state, worldIn, (BlockPos) o)) {
                        // || ship.wrapping.explodedPositionsThisTick.contains(o)) {
                        if (block.canDropFromExplosion(expl)) {
                            block.dropBlockAsItemWithChance(ship.world, pos, state, 1.0F / expl.size, 0);
                        }
                        block.onBlockExploded(ship.world, pos, expl);
                        if (!worldIn.isRemote && false) {
                            Vector posVector = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
                            ship.wrapping.coordTransform.fromLocalToGlobal(posVector);

                            double mass = BlockMass.basicMass.getMassFromState(state, pos, ship.world);
                            double explosionForce = Math.sqrt(this.size) * 1000D * mass;
                            Vector forceVector = new Vector(pos.getX() + .5 - expl.x, pos.getY() + .5 - expl.y,
                                    pos.getZ() + .5 - expl.z);
                            double vectorDist = forceVector.length();

                            forceVector.normalize();
                            forceVector.multiply(explosionForce / vectorDist);

//                            RotationMatrices.doRotationOnly(ship.wrapping.coordTransform.lToWTransform, forceVector);
                            ship.wrapping.coordTransform.getCurrentTickTransform().transform(forceVector, TransformType.LOCAL_TO_GLOBAL);
                            // TODO: Make this work again
                            // PhysicsQueuedForce queuedForce = new PhysicsQueuedForce(forceVector,
                            // posVector, false, 1);

                            // if (!ship.wrapping.explodedPositionsThisTick.contains(pos)) {
                            // ship.wrapping.explodedPositionsThisTick.add(pos);
                            // }

                            // ship.wrapping.queueForce(queuedForce);
                        }
                    }
                }
            }
        }
    }
}
