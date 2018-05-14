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

package valkyrienwarfare.physics.data;

import net.minecraft.util.math.AxisAlignedBB;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.network.PhysWrapperPositionMessage;
import valkyrienwarfare.physics.management.PhysicsObject;

public class ShipTransformationPacketHolder {

    public final int relativeTick;
    public final double posX, posY, posZ;
    public final double pitch, yaw, roll;
    public final Vector centerOfRotation;
    private AxisAlignedBB shipBB;

    public ShipTransformationPacketHolder(PhysWrapperPositionMessage wrapperMessage) {
        posX = wrapperMessage.posX;
        posY = wrapperMessage.posY;
        posZ = wrapperMessage.posZ;

        pitch = wrapperMessage.pitch;
        yaw = wrapperMessage.yaw;
        roll = wrapperMessage.roll;

        centerOfRotation = wrapperMessage.centerOfMass;

        relativeTick = wrapperMessage.relativeTick;
        shipBB = wrapperMessage.shipBB;
    }
    
    public ShipTransformationPacketHolder(ShipTransformationPacketHolder before, ShipTransformationPacketHolder after) {
        posX = (before.posX + after.posX) / 2D;
        posY = (before.posY + after.posY) / 2D;
        posZ = (before.posZ + after.posZ) / 2D;
        
        pitch = (before.pitch + after.pitch) / 2D;
        yaw = (before.yaw + after.yaw) / 2D;
        roll = (before.roll + after.roll) / 2D;
        
        centerOfRotation = before.centerOfRotation.getAddition(after.centerOfRotation).getProduct(.5D);
        
        relativeTick = before.relativeTick;
        // TODO: Make this proper
        shipBB = before.shipBB;
    }

    // Apply all the position/rotation variables accordingly onto the passed physObject
    public void applyToPhysObject(PhysicsObject physObj) {
        physObj.wrapper.posX = posX;
        physObj.wrapper.posY = posY;
        physObj.wrapper.posZ = posZ;

        physObj.wrapper.setPitch(pitch);
        physObj.wrapper.setYaw(yaw);
        physObj.wrapper.setRoll(roll);

        physObj.centerCoord = centerOfRotation;
        
        physObj.setCollisionBoundingBox(shipBB);
    }
}
