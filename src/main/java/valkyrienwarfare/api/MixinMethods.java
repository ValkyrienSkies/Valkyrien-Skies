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

package valkyrienwarfare.api;

import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A way of accessing some methods that we're injecting using mixins
 *
 * @author DaPorkchop_
 */
public class MixinMethods {
	private static Method rayTraceBlocksIgnoreShip = null;

	public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
		try {
			if (rayTraceBlocksIgnoreShip == null) {
				rayTraceBlocksIgnoreShip = World.class.getMethod("rayTraceBlocksIgnoreShip", Vec3d.class, Vec3d.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, PhysicsWrapperEntity.class);
			}
			return (RayTraceResult) rayTraceBlocksIgnoreShip.invoke(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, toIgnore);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
