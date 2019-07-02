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

package valkyrienwarfare.mixin.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.Optional;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {
    @Shadow
    protected BlockPos pos;

    @Shadow
    protected World world;

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(double x, double y, double z) {
        World tileWorld = this.world;
        double d0 = (double) this.pos.getX() + 0.5D - x;
        double d1 = (double) this.pos.getY() + 0.5D - y;
        double d2 = (double) this.pos.getZ() + 0.5D - z;
        double toReturn = d0 * d0 + d1 * d1 + d2 * d2;

        if (tileWorld != null) {
            //Assume on Ship
            if (tileWorld.isRemote && toReturn > 9999999D) {
                BlockPos pos = this.pos;
                Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);

                if (physicsObject.isPresent()) {
                    Vector tilePos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
                    physicsObject.get()
                            .getShipTransformationManager()
                            .fromLocalToGlobal(tilePos);

                    tilePos.X -= x;
                    tilePos.Y -= y;
                    tilePos.Z -= z;

                    return tilePos.lengthSq();
                }
            }
        }
        return toReturn;
    }
}
