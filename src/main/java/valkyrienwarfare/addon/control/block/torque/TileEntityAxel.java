package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.tileentity.TileEntity;

import java.util.Optional;

public class TileEntityAxel extends TileEntity implements IRotationNodeProvider {

    private final IRotationNode rotationNode;

    public TileEntityAxel() {
        super();
        this.rotationNode = new ImplRotationNode<>(this);
    }

    @Override
    public Optional<IRotationNode> getRotationNode() {
        return null;
    }
}
