package valkyrienwarfare.addon.control.block.ethercompressor;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.addon.control.nodenetwork.BasicForceNodeTileEntity;
import valkyrienwarfare.deprecated_api.IBlockForceProvider;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public abstract class BlockForceProviderBasic extends Block implements ITileEntityProvider, IBlockForceProvider  {

	public BlockForceProviderBasic(Material materialIn) {
		super(materialIn);
		// TODO Auto-generated constructor stub
	}

    @Override
    public Vector getBlockForceInShipSpace(World world, BlockPos pos, IBlockState state, Entity shipEntity, double secondsToApply) {
        PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) shipEntity;
        PhysicsObject obj = wrapper.getPhysicsObject();
        TileEntity worldTile = obj.getShipChunks().getTileEntity(pos);
        if (worldTile == null) {
            return null;
        }
        if (worldTile instanceof BasicForceNodeTileEntity) {
        	BasicForceNodeTileEntity engineTile = (BasicForceNodeTileEntity) worldTile;
            return engineTile.getForceOutputUnoriented(secondsToApply, obj);
        }
        return null;
    }

}
