package com.jackredcreeper.cannon.blocks;

import javax.annotation.Nullable;

import com.jackredcreeper.cannon.Reference;
import com.jackredcreeper.cannon.entities.EntityCannonball;
import com.jackredcreeper.cannon.init.ModItems;
import com.jackredcreeper.cannon.tileentity.TileEntityCannon;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;




public class BlockCannon extends BlockDirectional implements ITileEntityProvider {
	
	public BlockCannon() {
		super(Material.GROUND);
		setHardness(0.5f);
		setResistance(1);
		
		setUnlocalizedName(Reference.ModBlocks.CANNON.getUnlocalizedName());
		setRegistryName(Reference.ModBlocks.CANNON.getRegistryName());
		
        this.setDefaultState(this.blockState.getBaseState().withProperty(LOOKING, EnumFacing.NORTH));
        this.setCreativeTab(CreativeTabs.REDSTONE);
        
        int CannonCooldown = 0;
        boolean CannonReady = false;
        int Ammo = 0;
	}
	
    public static final PropertyDirection LOOKING = BlockDirectional.FACING;

	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof TileEntityCannon) { 
				if (!worldIn.isRemote){
					TileEntity tileEntity = worldIn.getTileEntity(pos);
					if(tileEntity instanceof TileEntityCannon) {
						TileEntityCannon cannon = (TileEntityCannon) tileEntity;
					
						if (heldItem == null)
							{      return false;      }
						else
					        {
				
					        	Item item = heldItem.getItem();
						            if (item == ModItems.key)
						        	{ cannon.fireCannon(worldIn, playerIn, pos, state, side);} 
						            if (item == ModItems.loader)
						        	{ cannon.loadCannon(worldIn, playerIn);}
						            if (item == ModItems.tuner)
						        	{ cannon.setAngle(playerIn);}
						            if (item == ModItems.cannonball)
						        	{ cannon.setAmmo(heldItem);}
						            if (item == ModItems.explosiveball)
						        	{ cannon.setAmmo(heldItem);}
				        
				        else	            	
					        {      return false;     	}
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
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
       

    private void setDefaultDirection(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            EnumFacing enumfacing = (EnumFacing)state.getValue(LOOKING);
            boolean flag = worldIn.getBlockState(pos.north()).isFullBlock();
            boolean flag1 = worldIn.getBlockState(pos.south()).isFullBlock();

            if (enumfacing == EnumFacing.NORTH && flag && !flag1)
            {
                enumfacing = EnumFacing.SOUTH;
                System.out.println("SOUTH");
            }
            else if (enumfacing == EnumFacing.SOUTH && flag1 && !flag)
            {
                enumfacing = EnumFacing.NORTH;
                System.out.println("NORTH");
            }
            else
            {
                boolean flag2 = worldIn.getBlockState(pos.west()).isFullBlock();
                boolean flag3 = worldIn.getBlockState(pos.east()).isFullBlock();

                if (enumfacing == EnumFacing.WEST && flag2 && !flag3)
                {
                    enumfacing = EnumFacing.EAST;
                    System.out.println("EAST");
                }
                else if (enumfacing == EnumFacing.EAST && flag3 && !flag2)
                {
                    enumfacing = EnumFacing.WEST;
                    System.out.println("WEST");
                }
            }

            worldIn.setBlockState(pos, state.withProperty(LOOKING, enumfacing), 2);
        }
    }


    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(LOOKING, EnumFacing.getFront(meta & 7));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | ((EnumFacing)state.getValue(LOOKING)).getIndex();

        return i;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(LOOKING, rot.rotate((EnumFacing)state.getValue(LOOKING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(LOOKING)));
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {LOOKING, });
    }
    
    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(LOOKING, BlockPistonBase.getFacingFromEntity(pos, placer));
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        worldIn.setBlockState(pos, state.withProperty(LOOKING, BlockPistonBase.getFacingFromEntity(pos, placer)), 2);

        if (stack.hasDisplayName())
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);

        }
    }
    
    
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        super.onBlockAdded(worldIn, pos, state);
        this.setDefaultDirection(worldIn, pos, state);
    }
  
    
    
    
    
}