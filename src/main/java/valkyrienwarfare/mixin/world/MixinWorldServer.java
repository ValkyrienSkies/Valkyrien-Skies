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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(value = WorldServer.class, priority = 1005)
public abstract class MixinWorldServer {

    private final WorldServer thisWorldServer = WorldServer.class.cast(this);
    
    @Inject(method = "setBlockState", at = @At("HEAD"))
    public void duringMarkAndNotifyBlock(BlockPos pos, IBlockState newState, int flags,
            CallbackInfoReturnable callbackInfo) {
        PhysicsWrapperEntity physEntity = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(thisWorldServer,
                pos);
        if (physEntity != null) {
            IBlockState oldState = this.getBlockState(pos);
            physEntity.wrapping.onSetBlockState(oldState, newState, pos);
            if (oldState != newState) {
                System.out.println(oldState.getBlock().getLocalizedName());
                System.out.println(newState.getBlock().getLocalizedName());
            }
        }
    }
    
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
}
