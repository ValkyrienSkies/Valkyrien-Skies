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

package org.valkyrienskies.mixin.util.math;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

/**
 * Necessary for now. I just wish Mojang would delete the distance function.
 */
@Mixin(ChunkPos.class)
public abstract class MixinChunkPos {

    private static final double TOO_MUCH_DISTANCE = 100000;
    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(Entity entityIn) {
        double d0 = this.x * 16 + 8;
        double d1 = this.z * 16 + 8;
        double d2 = d0 - entityIn.posX;
        double d3 = d1 - entityIn.posZ;
        double vanilla = d2 * d2 + d3 * d3;

        // A big number
        if (vanilla < TOO_MUCH_DISTANCE) {
            return vanilla;
        }

        try {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(entityIn.world,
                new BlockPos(d0, 127, d1));

            if (physicsObject.isPresent()) {
                Vector entityPosInLocal = new Vector(entityIn);
                // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform,
                // entityPosInLocal);
                physicsObject.get()
                    .getShipTransformationManager()
                    .getCurrentTickTransform()
                    .transform(entityPosInLocal,
                        TransformType.GLOBAL_TO_SUBSPACE);
                entityPosInLocal.subtract(d0, entityPosInLocal.Y, d1);
                return entityPosInLocal.lengthSq();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vanilla;
    }

}
