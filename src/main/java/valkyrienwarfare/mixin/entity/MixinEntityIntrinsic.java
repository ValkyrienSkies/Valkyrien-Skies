/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.mixin.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.collision.EntityCollisionInjector;
import valkyrienwarfare.collision.EntityCollisionInjector.IntermediateMovementVariableStorage;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

@Mixin(value = Entity.class, priority = 1)
public abstract class MixinEntityIntrinsic {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public World world;

    public Entity thisClassAsAnEntity = Entity.class.cast(this);

    private IntermediateMovementVariableStorage alteredMovement = null;

    /**
     * fix a warning
     *
     * @author asdf
     */
    @ModifyArgs(method = "move",
            at = @At("HEAD"))
    public void changeMoveArgs(Args args, MoverType type, double dx, double dy, double dz) {
//    	System.out.println("test");
        if (PhysicsWrapperEntity.class.isInstance(this)) {
            //Don't move at all
            return;
        }

        double movDistSq = (dx * dx) + (dy * dy) + (dz * dz);

        if (movDistSq > 10000) {
            //Assume this will take us to Ship coordinates
            double newX = this.posX + dx;
            double newY = this.posY + dy;
            double newZ = this.posZ + dz;
            BlockPos newPosInBlock = new BlockPos(newX, newY, newZ);
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, newPosInBlock);

            if (wrapper == null) {
                return;
            }

            Vector endPos = new Vector(newX, newY, newZ);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, endPos);
            dx = endPos.X - this.posX;
            dy = endPos.Y - this.posY;
            dz = endPos.Z - this.posZ;
        }

        alteredMovement = EntityCollisionInjector.alterEntityMovement(thisClassAsAnEntity, type, dx, dy, dz);

        if (alteredMovement == null) {
            args.setAll(type, dx, dy, dz);
        } else {
            args.setAll(type, alteredMovement.dxyz.X, alteredMovement.dxyz.Y, alteredMovement.dxyz.Z);
        }
    }

    @Inject(method = "move",
        at = @At("RETURN"))
    public void postMove(CallbackInfo callbackInfo) {
        if (alteredMovement != null) {
            EntityCollisionInjector.alterEntityMovementPost(thisClassAsAnEntity, alteredMovement);
        }
    }
}
