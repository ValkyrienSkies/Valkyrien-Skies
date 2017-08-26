package ValkyrienWarfareBase.Mixin.entity.player;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.IShipPilot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IShipPilot {

	public PhysicsWrapperEntity pilotedShip;
	public BlockPos blockBeingControlled;
	public ControllerInputType controlInputType;

	@Overwrite
	public BlockPos getBedSpawnLocation(World worldIn, BlockPos bedLocation, boolean forceSpawn) {
		int chunkX = bedLocation.getX() >> 4;
		int chunkZ = bedLocation.getZ() >> 4;
		long chunkPos = ChunkPos.asLong(chunkX, chunkZ);

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
