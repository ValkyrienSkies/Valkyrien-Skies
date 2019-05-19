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

package valkyrienwarfare.addon.control.nodenetwork;

import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.physics.PhysicsCalculations;
import valkyrienwarfare.physics.management.PhysicsObject;

public interface INodeController extends Comparable<INodeController> {

    int getPriority();

    void setPriority(int newPriority);

    /**
     * Does nothing by default, insert processor logic here
     *
     * @param object
     * @param calculations
     * @param secondsToSimulate
     */
    void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate);

    /**
     * Returns the position of the TileEntity that is behind this interface.
     *
     * @return
     */
    BlockPos getNodePos();

    // Used maintain order of which processors get called first. If both processors
    // have equal priorities, then we use the BlockPos as a tiebreaker.
    @Override
    default int compareTo(INodeController other) {
        if (getPriority() != other.getPriority()) {
            return getPriority() - other.getPriority();
        } else {
            // break the tie
            return getNodePos().compareTo(other.getNodePos());
        }
    }
}
