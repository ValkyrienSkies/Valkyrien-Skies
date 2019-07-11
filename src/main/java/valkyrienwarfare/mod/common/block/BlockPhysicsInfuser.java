/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.common.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
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
import valkyrienwarfare.mod.client.gui.VW_Gui_Enum;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.physmanagement.relocation.DetectorManager;
import valkyrienwarfare.mod.common.tileentity.TileEntityPhysicsInfuser;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockPhysicsInfuser extends BlockVWDirectional implements ITileEntityProvider {

    public static final PropertyBool INFUSER_LIGHT_ON = PropertyBool.create("infuser_light_on");

    int shipSpawnDetectorID;

    public BlockPhysicsInfuser(Material materialIn) {
        super(materialIn);
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
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            BlockPos dummyPos = getDummyStatePos(state, pos);
            worldIn.setBlockState(dummyPos, ValkyrienWarfareMod.INSTANCE.physicsInfuserDummy.getDefaultState()
                    .withProperty(FACING, getDummyStateFacing(state)));
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityPhysicsInfuser te = (TileEntityPhysicsInfuser) worldIn.getTileEntity(pos);
        if (te == null || te.isInvalid()) {
            // Bah!
            return;
        }
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        // Drop all the items
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        BlockPos dummyPos = getDummyStatePos(state, pos);
        super.breakBlock(worldIn, pos, state);
        if (worldIn.getBlockState(dummyPos)
                .getBlock() == ValkyrienWarfareMod.INSTANCE.physicsInfuserDummy) {
            worldIn.setBlockToAir(dummyPos);
        }
        // Finally, delete the tile entity.
        worldIn.removeTileEntity(pos);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.BLUE
                + "Turns any blocks attached to this one into a brand new Ship, " +
                "just be careful not to infuse your entire world");
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing facingHorizontal = placer.getHorizontalFacing();

        if (!placer.isSneaking()) {
            facingHorizontal = facingHorizontal.getOpposite();
        }

        return this.getDefaultState()
                .withProperty(FACING, facingHorizontal)
                .withProperty(INFUSER_LIGHT_ON, false);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(ValkyrienWarfareMod.INSTANCE, VW_Gui_Enum.PHYSICS_INFUSER.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
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
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (super.canPlaceBlockAt(worldIn, pos)) {
            // Make sure the adjacent 4 blocks are free
            return worldIn.getBlockState(pos.north())
                    .getBlock()
                    .isReplaceable(worldIn, pos.north()) && worldIn.getBlockState(pos.south())
                    .getBlock()
                    .isReplaceable(worldIn, pos.south()) && worldIn.getBlockState(pos.east())
                    .getBlock()
                    .isReplaceable(worldIn, pos.east()) && worldIn.getBlockState(pos.west())
                    .getBlock()
                    .isReplaceable(worldIn, pos.west());
        } else {
            return false;
        }
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
}