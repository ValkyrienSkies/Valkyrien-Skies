package org.valkyrienskies.mod.common.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.tileentity.TileEntitySmallShipSail;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockSmallShipSail extends BlockBoatChair {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockSmallShipSail() {
        super("small_ship_sail", Material.WOOD, 4.0F);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySmallShipSail();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
            if (physicsObject.isPresent()) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileEntitySmallShipSail) {
                    ((TileEntitySmallShipSail) tileEntity).setPilotEntity(playerIn);
                }
            }
        }
        return true;
    }
}
