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

package valkyrienwarfare.mixin.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.piloting.ControllerInputType;
import valkyrienwarfare.addon.control.piloting.IShipPilot;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.interaction.ShipUUIDToPosData;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

import java.util.UUID;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IShipPilot {
	public MixinEntityPlayer()  {
		super(null);
		//wtf java
	}
	
	public PhysicsWrapperEntity pilotedShip;
	public BlockPos blockBeingControlled;
	public ControllerInputType controlInputType;

	@Inject(method = "getBedSpawnLocation",
	at = @At("HEAD"),
	cancellable = true)
	static void preGetBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn, CallbackInfoReturnable<BlockPos> callbackInfo)	{
		int chunkX = bedLocation.getX() >> 4;
		int chunkZ = bedLocation.getZ() >> 4;

		UUID shipManagingID = ValkyrienWarfareMod.chunkManager.getShipIDManagingPos_Persistant(worldIn, chunkX, chunkZ);
		if (shipManagingID != null) {
			ShipUUIDToPosData.ShipPositionData positionData = ValkyrienWarfareMod.chunkManager.getShipPosition_Persistant(worldIn, shipManagingID);

			if (positionData != null) {
				double[] lToWTransform = RotationMatrices.convertToDouble(positionData.lToWTransform);

				Vector bedPositionInWorld = new Vector(bedLocation.getX() + .5D, bedLocation.getY() + .5D, bedLocation.getZ() + .5D);
				RotationMatrices.applyTransform(lToWTransform, bedPositionInWorld);

				bedPositionInWorld.Y += 1D;

				bedLocation = new BlockPos(bedPositionInWorld.X, bedPositionInWorld.Y, bedPositionInWorld.Z);

				callbackInfo.setReturnValue(bedLocation);
			} else {
				System.err.println("A ship just had Chunks claimed persistant, but not any position data persistant");
			}
		}
	}
	
	@Override
	public PhysicsWrapperEntity getPilotedShip() {
		return pilotedShip;
	}
	
	@Override
	public void setPilotedShip(PhysicsWrapperEntity wrapper) {
		pilotedShip = wrapper;
	}
	
	@Override
	public boolean isPilotingShip() {
		return pilotedShip != null;
	}
	
	@Override
	public BlockPos getPosBeingControlled() {
		return blockBeingControlled;
	}
	
	@Override
	public void setPosBeingControlled(BlockPos pos) {
		blockBeingControlled = pos;
	}
	
	@Override
	public ControllerInputType getControllerInputEnum() {
		return controlInputType;
	}
	
	@Override
	public void setControllerInputEnum(ControllerInputType type) {
		controlInputType = type;
	}
	
	@Override
	public boolean isPilotingATile() {
		return blockBeingControlled != null;
	}
	
	@Override
	public boolean isPiloting() {
		return isPilotingShip() || isPilotingATile();
	}
	
	@Override
	public void stopPilotingEverything() {
		setPilotedShip(null);
		setPosBeingControlled(null);
		setControllerInputEnum(null);
	}
}
