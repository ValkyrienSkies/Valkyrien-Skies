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

package valkyrienwarfare.addon.control.tileentity;

import valkyrienwarfare.physics.PhysicsCalculationsManualControl;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.PilotControlsMessage;
import net.minecraft.entity.player.EntityPlayerMP;

public class TileEntityZepplinController extends ImplTileEntityPilotable {
	
	@Override
	public void onStopTileUsage() {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if (wrapper != null) {
			PhysicsCalculationsManualControl zepplinPhysics = (PhysicsCalculationsManualControl) wrapper.wrapping.physicsProcessor;
			zepplinPhysics.upRate = 0;
			zepplinPhysics.forwardRate = 0;
		}
	}
	
	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.Zepplin;
	}
	
	@Override
	boolean setClientPilotingEntireShip() {
		return true;
	}
	
	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if (wrapper != null) {
			PhysicsCalculationsManualControl zepplinPhysics = (PhysicsCalculationsManualControl) wrapper.wrapping.physicsProcessor;
			if (message.airshipLeft_KeyDown) {
				zepplinPhysics.yawRate -= 2.5;
			}
			if (message.airshipRight_KeyDown) {
				zepplinPhysics.yawRate += 2.5;
			}
			if (message.airshipUp_KeyDown) {
				zepplinPhysics.upRate += .25D;
			}
			if (message.airshipDown_KeyDown) {
				zepplinPhysics.upRate -= .25D;
			}
			if (message.airshipForward_KeyDown) {
				zepplinPhysics.forwardRate += .25D;
			}
			if (message.airshipBackward_KeyDown) {
				zepplinPhysics.forwardRate -= .25D;
			}
			if (message.airshipStop_KeyDown) {
				zepplinPhysics.yawRate = zepplinPhysics.upRate = zepplinPhysics.forwardRate = 0;
			}
			zepplinPhysics.yawRate = Math.min(Math.max(-50, zepplinPhysics.yawRate), 50);
			zepplinPhysics.upRate = Math.min(Math.max(-20, zepplinPhysics.upRate), 20);
			zepplinPhysics.forwardRate = Math.min(Math.max(-20, zepplinPhysics.forwardRate), 20);
		}
	}
	
}
