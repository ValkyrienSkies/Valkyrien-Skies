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

package org.valkyrienskies.mod.common.physmanagement.relocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DetectorManager {

    public static SpatialDetector getDetectorFor(int id, BlockPos start, World worldIn, int maximum,
        boolean checkCorners) {
        return getDetectorFor(DetectorIDs.values()[id], start, worldIn, maximum, checkCorners);
    }


    public static SpatialDetector getDetectorFor(DetectorIDs id, BlockPos start, World worldIn, int maximum,
        boolean checkCorners) {
        switch (id) {
            case ShipSpawnerGeneral:
                return new ShipSpawnDetector(start, worldIn, maximum, checkCorners);
            case BlockPosFinder:
                return new ShipBlockPosFinder(start, worldIn, maximum, checkCorners);
            case SingleBlockPosFinder:
                return new SingleBlockPosDetector(start, worldIn, maximum, checkCorners);
            default:
                throw new IllegalArgumentException("Unrecognized detector");
        }
    }

    public enum DetectorIDs {
        ShipSpawnerGeneral, BlockPosFinder, SingleBlockPosFinder
    }

}
