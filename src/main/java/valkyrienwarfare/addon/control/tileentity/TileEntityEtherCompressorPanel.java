package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.tileentity.TileEntity;
import valkyrienwarfare.math.Vector;

public class TileEntityEtherCompressorPanel extends TileEntityEtherPropulsion {

    public TileEntityEtherCompressorPanel(Vector vector, double power) {
		super(vector, power);
	}

	@Override
    public double getThrustActual() {
        return this.getMaxThrust() * this.getCurrentEtherEfficiency(); // * this.getThrustMultiplierGoal();
    }
}
