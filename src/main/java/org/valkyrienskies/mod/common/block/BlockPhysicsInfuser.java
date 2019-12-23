package org.valkyrienskies.mod.common.block;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.valkyrienskies.mod.client.gui.VS_Gui_Enum;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.physmanagement.relocation.DetectorManager;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockPhysicsInfuser extends BlockVSDirectional implements ITileEntityProvider {

    public static final PropertyBool INFUSER_LIGHT_ON = PropertyBool.create("infuser_light_on");

	protected String name;

    int shipSpawnDetectorID;

    public BlockPhysicsInfuser(String name) {
        super(name, Material.WOOD, 0.0F, true);
		this.name = name;
        shipSpawnDetectorID = DetectorManager.DetectorIDs.ShipSpawnerGeneral.ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // Lower 3 bits are for enumFacing
        EnumFacing enumFacing = EnumFacing.byIndex(meta & 7);
        if (enumFacing.getAxis() == EnumFacing.Axis.Y) {
            enumFacing = EnumFacing.NORTH;
        }
        // Highest bit is for light on
        boolean lightOn = meta >> 3 == 1;
        return this.getDefaultState()
            .withProperty(FACING, enumFacing)
            .withProperty(INFUSER_LIGHT_ON, lightOn);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int stateMeta = state.getValue(FACING)
            .getIndex();
        if (state.getValue(INFUSER_LIGHT_ON)) {
            stateMeta |= 8;
        }
        return stateMeta;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
        EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            BlockPos dummyPos = getDummyStatePos(state, pos);
            worldIn.setBlockState(dummyPos,
                ValkyrienSkiesMod.INSTANCE.physicsInfuserDummy.getDefaultState()
                    .withProperty(FACING, getDummyStateFacing(state)));
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityPhysicsInfuser tileEntity = (TileEntityPhysicsInfuser) worldIn.getTileEntity(pos);
        // If there's a valid TileEntity, try dropping the contents of it's inventory.
        if (tileEntity != null && !tileEntity.isInvalid()) {
            IItemHandler handler = tileEntity
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            // Safety in case the capabilities system breaks. If we can't find the handler then
            // there isn't anything to drop anyways.
            if (handler != null) {
                // Drop all the items
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    InventoryHelper
                        .spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }
        // Remove the dummy block of this physics infuser, if there is one.
        BlockPos dummyBlockPos = getDummyStatePos(state, pos);
        super.breakBlock(worldIn, pos, state);
        if (worldIn.getBlockState(dummyBlockPos)
            .getBlock() == ValkyrienSkiesMod.INSTANCE.physicsInfuserDummy) {
            worldIn.setBlockToAir(dummyBlockPos);
        }
        // Finally, delete the tile entity.
        worldIn.removeTileEntity(pos);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip,
        ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.BLUE + I18n.format("tooltip.valkyrienskies." + this.name));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
        float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        EnumFacing playerFacing = placer.getHorizontalFacing();
        if (!placer.isSneaking()) {
            playerFacing = playerFacing.getOpposite();
        }

        // Find the facing that's closest to what the player wanted.
        EnumFacing facingHorizontal;
        if (canPlaceBlockAtWithFacing(worldIn, pos, playerFacing)) {
            facingHorizontal = playerFacing;
        } else if (canPlaceBlockAtWithFacing(worldIn, pos, playerFacing.rotateY())) {
            facingHorizontal = playerFacing.rotateY();
        } else if (canPlaceBlockAtWithFacing(worldIn, pos, playerFacing.rotateYCCW())) {
            facingHorizontal = playerFacing.rotateYCCW();
        } else if (canPlaceBlockAtWithFacing(worldIn, pos, playerFacing.getOpposite())) {
            facingHorizontal = playerFacing.getOpposite();
        } else {
            // There was no valid facing! How the did this method even get called!
            throw new IllegalStateException("Cannot find valid state for placement for Physics Infuser!");
        }

        return this.getDefaultState()
            .withProperty(FACING, facingHorizontal)
            .withProperty(INFUSER_LIGHT_ON, false);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn,
        EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn
                .openGui(ValkyrienSkiesMod.INSTANCE, VS_Gui_Enum.PHYSICS_INFUSER.ordinal(), worldIn,
                    pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (super.canPlaceBlockAt(worldIn, pos)) {
            // Make sure the adjacent 4 blocks are free
            for (EnumFacing horizontal : EnumFacing.HORIZONTALS) {
                if (canPlaceBlockAtWithFacing(worldIn, pos, horizontal)) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos getDummyStatePos(IBlockState state, BlockPos pos) {
        EnumFacing dummyBlockFacing = state.getValue(FACING)
            .rotateY()
            .getOpposite();
        return pos.offset(dummyBlockFacing);
    }

    public EnumFacing getDummyStateFacing(IBlockState state) {
        return state.getValue(FACING).rotateY();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPhysicsInfuser();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(INFUSER_LIGHT_ON) ? 7 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, INFUSER_LIGHT_ON);
    }

    private boolean canPlaceBlockAtWithFacing(World world, BlockPos pos, EnumFacing facing) {
        BlockPos dummyStatePos = getDummyStatePos(getDefaultState().withProperty(FACING, facing),
            pos);
        return world.getBlockState(dummyStatePos)
            .getBlock()
            .isReplaceable(world, dummyStatePos);
    }
}