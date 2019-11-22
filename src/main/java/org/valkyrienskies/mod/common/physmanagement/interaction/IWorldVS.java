/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.physmanagement.interaction;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;

public interface IWorldVS {

    /**
     * Makes a physics wrapper entity be ignored by the {@link net.minecraft.world.World#rayTraceBlocks(Vec3d,
     * Vec3d, boolean, boolean, boolean)} method (and overloads thereof).
     * <p>
     * This has no effect on the behavior of {@link #rayTraceBlocksIgnoreShip(Vec3d, Vec3d, boolean,
     * boolean, boolean, PhysicsObject)}.
     * <p>
     * Must be followed later by a call to {@link #unexcludeShipFromRayTracer(PhysicsObject)}
     * with the same {@link PhysicsObject} instance as a parameter.
     *
     * @param entity the {@link PhysicsObject} to exclude from ray tracing
     */
    void excludeShipFromRayTracer(PhysicsObject entity);

    void unexcludeShipFromRayTracer(PhysicsObject entity);

    RayTraceResult rayTraceBlocksIgnoreShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                            boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                            PhysicsObject toIgnore);
}
