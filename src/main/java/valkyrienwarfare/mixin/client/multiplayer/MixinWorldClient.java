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

package valkyrienwarfare.mixin.client.multiplayer;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.List;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {
    private boolean hasChanged = false;

    public MixinWorldClient() {
        super(null, null, null, null, false);
    }

    @Shadow
    public abstract void doVoidFogParticles(int posX, int posY, int posZ);

    @Inject(method = "doVoidFogParticles",
            at = @At("HEAD"),
            cancellable = true)
    public void preDoVoidParticles(int posX, int posY, int posZ, CallbackInfo callbackInfo) {
        if (!hasChanged) {
            int range = 15;
            AxisAlignedBB aabb = new AxisAlignedBB(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range);
            List<PhysicsWrapperEntity> physEntities = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(WorldClient.class.cast(this)).getNearbyPhysObjects(aabb);
            hasChanged = true;
            for (PhysicsWrapperEntity wrapper : physEntities) {
                Vector playPosInShip = new Vector(posX + .5D, posY + .5D, posZ + .5D);
//                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playPosInShip);
                wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(playPosInShip, TransformType.GLOBAL_TO_SUBSPACE);
                this.doVoidFogParticles(MathHelper.floor(playPosInShip.X), MathHelper.floor(playPosInShip.Y), MathHelper.floor(playPosInShip.Z));
            }
            hasChanged = false;
            callbackInfo.cancel();
        }
    }
}
