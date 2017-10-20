package valkyrienwarfare.mixin.entity.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
	
	@Overwrite
	public static BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn) {
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
				
				return bedLocation;
			} else {
				System.err.println("A ship just had Chunks claimed persistant, but not any position data persistant");
			}
		}
		
		return bedLocation;
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
