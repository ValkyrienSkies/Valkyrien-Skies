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

package org.valkyrienskies.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.PhysicsCalculations;
import valkyrienwarfare.api.TransformType;

public class TileEntityGyroscopeStabilizer extends TileEntity {

    // Up to 15,000,000 newton-meters of torque generated.
    public static final double MAXIMUM_TORQUE = 15000000;
    // The direction we are want to align to.
    private static final Vector GRAVITY_UP = new Vector(0, 1, 0);

    public Vector getTorqueInGlobal(PhysicsCalculations physicsCalculations, BlockPos pos) {
        Vector shipLevelNormal = new Vector(GRAVITY_UP);
        physicsCalculations.getParent().shipTransformationManager().getCurrentPhysicsTransform()
            .rotate(shipLevelNormal, TransformType.SUBSPACE_TO_GLOBAL);
        Vector torqueDir = GRAVITY_UP.cross(shipLevelNormal);
        double angleBetween = Math.toDegrees(GRAVITY_UP.angleBetween(shipLevelNormal));
        torqueDir.normalize();

        double torquePowerFactor = angleBetween / 5;

        torquePowerFactor = Math.max(Math.min(1, torquePowerFactor), 0);

        // System.out.println(angleBetween);

        torqueDir.multiply(MAXIMUM_TORQUE * torquePowerFactor * physicsCalculations
            .getPhysicsTimeDeltaPerPhysTick() * -1D);
        return torqueDir;
    }

}
