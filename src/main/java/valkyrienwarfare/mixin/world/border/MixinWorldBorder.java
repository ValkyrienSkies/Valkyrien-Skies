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

package valkyrienwarfare.mixin.world.border;

import valkyrienwarfare.chunkmanagement.PhysicsChunkManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldBorder.class)
public abstract class MixinWorldBorder {

	@Overwrite
	public boolean contains(BlockPos pos) {
		if (PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
			return true;
		}
		return (double) (pos.getX() + 1) > this.minX() && (double) pos.getX() < this.maxX() && (double) (pos.getZ() + 1) > this.minZ() && (double) pos.getZ() < this.maxZ();
	}

	@Overwrite
	public boolean contains(ChunkPos range) {
		if (PhysicsChunkManager.isLikelyShipChunk(range.x, range.z)) {
			return true;
		}
		return (double) range.getXEnd() > this.minX() && (double) range.getXStart() < this.maxX() && (double) range.getZEnd() > this.minZ() && (double) range.getZStart() < this.maxZ();
	}

	@Overwrite
	public boolean contains(AxisAlignedBB bb) {
		int xPos = (int) bb.minX;
		int zPos = (int) bb.minZ;
		if (PhysicsChunkManager.isLikelyShipChunk(xPos >> 4, zPos >> 4)) {
			return true;
		}
		return bb.maxX > this.minX() && bb.minX < this.maxX() && bb.maxZ > this.minZ() && bb.minZ < this.maxZ();
	}

	@Shadow
	public abstract double minX();

	@Shadow
	public abstract double minZ();

	@Shadow
	public abstract double maxX();

	@Shadow
	public abstract double maxZ();

}
