package com.jackredcreeper.cannon.blocks;

import com.jackredcreeper.cannon.CannonModReference;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;


public class BlockCannon extends BlockDirectional implements ITileEntityProvider {

	public static final PropertyDirection LOOKING = BlockDirectional.FACING;

	public BlockCannon() {
		super(Material.IRON);
		setHardness(0.5f);
		setResistance(1);
		setResistance(1);

		setUnlocalizedName(CannonModReference.ModBlocks.CANNON.getUnlocalizedName());
		setRegistryName(CannonModReference.ModBlocks.CANNON.getRegistryName());

		this.setDefaultState(this.blockState.getBaseState().withProperty(LOOKING, EnumFacing.NORTH));
		this.setCreativeTab(CreativeTabs.COMBAT);

		int CannonCooldown = 0;
		boolean CannonReady = false;
		int Ammo = 0;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Cannon block used to fire explosive projectiles.");

		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.GRAY + TextFormatting.ITALIC + "Can fire cannon balls, explosive balls, grapeshot, and solid ball ammo types.");
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityCannon) {
			if (!worldIn.isRemote) {
				TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof TileEntityCannon) {
					TileEntityCannon cannon = (TileEntityCannon) tileEntity;

					if (heldItem == null) {
						return false;
					} else {

						Item item = heldItem.getItem();
						if (item == ModItems.key) {
							cannon.fireCannon(worldIn, playerIn, pos, state, side);
						}
						if (item == ModItems.loader) {
							cannon.loadCannon(worldIn, playerIn);
						}
						if (item == ModItems.tuner) {
							if (playerIn.isSneaking()) {
								float angle = -5F;
							}
							float angle = +5F;
							cannon.setAngle(playerIn, angle);
						}
						if (item == ModItems.cannonball) {
							cannon.setAmmo(heldItem);
						}
						if (item == ModItems.explosiveball) {
							cannon.setAmmo(heldItem);
						}
						if (item == ModItems.grapeshot) {
							cannon.setAmmo(heldItem);
						}
						if (item == ModItems.solidball) {
							cannon.setAmmo(heldItem);
						} else {
							return false;
						}
					}
				}
			}
		}
		return true;
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCannon();
	}

	/**
	 * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
	 */
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}


	private void setDefaultDirection(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			EnumFacing enumfacing = (EnumFacing) state.getValue(LOOKING);
			boolean flag = worldIn.getBlockState(pos.north()).isFullBlock();
			boolean flag1 = worldIn.getBlockState(pos.south()).isFullBlock();

			if (enumfacing == EnumFacing.NORTH && flag && !flag1) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && flag1 && !flag) {
				enumfacing = EnumFacing.NORTH;
			} else {
				boolean flag2 = worldIn.getBlockState(pos.west()).isFullBlock();
				boolean flag3 = worldIn.getBlockState(pos.east()).isFullBlock();

				if (enumfacing == EnumFacing.WEST && flag2 && !flag3) {
					enumfacing = EnumFacing.EAST;
				} else if (enumfacing == EnumFacing.EAST && flag3 && !flag2) {
					enumfacing = EnumFacing.WEST;
				}
			}

			worldIn.setBlockState(pos, state.withProperty(LOOKING, enumfacing), 2);
		}
	}


	/**
	 * Convert the given metadata into a BlockState for this block
	 */
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(LOOKING, EnumFacing.getFront(meta & 7));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state) {
		int i = 0;
		i = i | ((EnumFacing) state.getValue(LOOKING)).getIndex();

		return i;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(LOOKING, rot.rotate((EnumFacing) state.getValue(LOOKING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(LOOKING)));
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{LOOKING,});
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
//    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
//    {
//        return this.getDefaultState().withProperty(LOOKING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
//    }
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.isSneaking() ? placer.getHorizontalFacing().getOpposite() : placer.getHorizontalFacing());
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		this.setDefaultDirection(worldIn, pos, state);
	}


}