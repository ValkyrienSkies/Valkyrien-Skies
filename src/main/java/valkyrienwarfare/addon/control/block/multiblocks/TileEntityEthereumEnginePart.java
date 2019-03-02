package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.block.torque.IRotationNode;
import valkyrienwarfare.addon.control.block.torque.IRotationNodeProvider;
import valkyrienwarfare.addon.control.block.torque.ImplRotationNode;

import java.util.Optional;

public class TileEntityEthereumEnginePart extends TileEntityMultiblockPart<EthereumEngineMultiblockSchematic> implements IRotationNodeProvider {

	protected final IRotationNode rotationNode;
	private double prevKeyframe;
	private double currentKeyframe;
	
	public TileEntityEthereumEnginePart() {
		super();
		this.prevKeyframe = 0;
		this.currentKeyframe = 0;
		this.rotationNode = new ImplRotationNode<>(this);
		this.rotationNode.setRotationalInertia(5);
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

	@Override
	public void assembleMultiblock(EthereumEngineMultiblockSchematic schematic, BlockPos relativePos) {
		super.assembleMultiblock(schematic, relativePos);
		if (relativePos.equals(new BlockPos(-1, 0, -1))) {
			System.out.println(this.getPos());
		}
	}

	@Override
	public void dissembleMultiblockLocal() {
		super.dissembleMultiblockLocal();
		this.rotationNode.resetNodeData();
	}
	// The following methods are basically just here because interfaces can't have fields.
	@Override
	public Optional<IRotationNode> getRotationNode() {
		if (rotationNode.isInitialized()) {
			return Optional.of(rotationNode);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		rotationNode.readFromNBT(compound);
		rotationNode.markInitialized();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		rotationNode.writeToNBT(compound);
		return compound;
	}
}
