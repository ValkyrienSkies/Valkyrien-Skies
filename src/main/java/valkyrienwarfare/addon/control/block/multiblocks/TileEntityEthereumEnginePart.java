package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.util.math.BlockPos;

public class TileEntityEthereumEnginePart extends TileEntityMultiblockPart {

	private double prevKeyframe;
	private double currentKeyframe;
	
	public TileEntityEthereumEnginePart() {
		super();
		this.prevKeyframe = 0;
		this.currentKeyframe = 0;
	}
	
	@Override
	public void update() {
		super.update();
		prevKeyframe = currentKeyframe;
		currentKeyframe += 2.5;
		currentKeyframe = currentKeyframe % 99;
	}
	
	public double getCurrentKeyframe(double partialTick) {
		double increment = currentKeyframe - prevKeyframe;
		if (increment < 0) {
			increment = (increment % 99) + 99;
		}
		return prevKeyframe + (increment * partialTick) + 1;
	}
	
}
