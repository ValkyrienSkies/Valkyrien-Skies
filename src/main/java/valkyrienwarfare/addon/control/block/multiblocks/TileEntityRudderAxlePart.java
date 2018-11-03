package valkyrienwarfare.addon.control.block.multiblocks;

import java.util.Optional;

import net.minecraft.util.EnumFacing;
import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class TileEntityRudderAxlePart extends TileEntityMultiblockPartForce {

	public TileEntityRudderAxlePart() {
		super();
	}
	
	@Override
	public VectorImmutable getForceOutputNormal() {
		// TODO Auto-generated method stub
		return new VectorImmutable(1, 0, 0);
	}

	@Override
	public double getThrustMagnitude() {
		return 0;
	}
	
	public Optional<EnumFacing> getRudderAxleAxisDirection() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleAxisDirection());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<EnumFacing> getRudderAxleFacingDirection() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleFacingDirection());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<Integer> getRudderAxleLength() {
		Optional<RudderAxleMultiblockSchematic> rudderAxleSchematicOptional = getRudderAxleSchematic();
		if (rudderAxleSchematicOptional.isPresent()) {
			return Optional.of(rudderAxleSchematicOptional.get().getAxleLength());
		} else {
			return Optional.empty();
		}
	}

	private Optional<RudderAxleMultiblockSchematic> getRudderAxleSchematic() {
		IMulitblockSchematic schematic = getMultiblockSchematic();
		if (this.isPartOfAssembledMultiblock() && schematic instanceof RudderAxleMultiblockSchematic) {
			return Optional.of((RudderAxleMultiblockSchematic) schematic);
		} else {
			return Optional.empty();
		}
	}
	
}
