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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.physics.collision.Polygon;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.client.render.IntrinsicTileEntityInterface;

@Mixin(TileEntity.class)
@Implements(@Interface(iface = IntrinsicTileEntityInterface.class, prefix = "vw$"))
public abstract class MixinTileEntityCLIENT {
    @Shadow
    public World world;

    @Intrinsic(displace = true)
    public AxisAlignedBB vw$getRenderBoundingBox() {
        AxisAlignedBB toReturn = getRenderBoundingBox();
        BlockPos pos = new BlockPos(toReturn.minX, toReturn.minY, toReturn.minZ);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
        if (wrapper != null) {
            Polygon poly = new Polygon(toReturn, wrapper.wrapping.coordTransform.lToWTransform);
            return poly.getEnclosedAABB();
        }
        return toReturn;
    }

    @Shadow
    public abstract AxisAlignedBB getRenderBoundingBox();

    @Shadow
    public abstract BlockPos getPos();
}
