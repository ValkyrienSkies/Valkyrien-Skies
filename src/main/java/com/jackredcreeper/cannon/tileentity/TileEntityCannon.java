package com.jackredcreeper.cannon.tileentity;

import javax.annotation.Nullable;

import com.jackredcreeper.cannon.blocks.BlockCannon;
import com.jackredcreeper.cannon.entities.EntityCannonball;
import com.jackredcreeper.cannon.entities.EntityExplosiveball;
import com.jackredcreeper.cannon.init.ModItems;

import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class TileEntityCannon extends TileEntity {
	
    int CannonCooldown = 0;
    int CC = 0;
    boolean CannonReady = true;
    float Angle = 0;
    int Ammo = 0;

    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	
    	super.writeToNBT(compound);
    	compound.setInteger("CannonCooldown", CannonCooldown);
    	compound.setBoolean("CannonReady", CannonReady);
    	compound.setFloat("Angle", Angle);
    	compound.setInteger("Ammo", Ammo);
		return compound;    	
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
    	
    	super.readFromNBT(compound);
    	CannonCooldown = compound.getInteger("CannonCooldown");
        CannonReady = compound.getBoolean("CannonReady");
        Angle = compound.getFloat("Angle");
        Ammo = compound.getInteger("Ammo");
    }
    
    public void fireCannon(World worldIn, EntityPlayer playerIn, BlockPos pos, IBlockState state, EnumFacing side) {
    	
		if (CannonReady) {
			firecannon(worldIn, pos, side, state); 
			CannonReady = false;
			CannonCooldown = 0;
		}
		else
    	{			playerIn.addChatComponentMessage(new TextComponentString("Cannon needs to be loaded!")); }
    }


    public void loadCannon(World worldIn, EntityPlayer playerIn) {
    	if (Ammo != 0) { 
			
			if (CannonCooldown == CC) {
					CannonReady = true;
				worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE , SoundCategory.BLOCKS, 3, 1);
			}
			else
			{
				CannonCooldown++;
			}
    	}
    	else
    	{			playerIn.addChatComponentMessage(new TextComponentString("Cannon needs ammo!")); }
    }
    
    
    public void firecannon(World worldIn, BlockPos pos, EnumFacing side, IBlockState state) {
//lets boom!

        EnumFacing enumfacing = (EnumFacing)state.getValue(BlockCannon.LOOKING);
        double d0 = pos.getX() + 0.5D + (float)enumfacing.getFrontOffsetX();
        //double posX = dispenser.getX() + ((float)direction.getFrontOffsetX());
        double d1 = pos.getY() + 0.4D + (float)enumfacing.getFrontOffsetY();
        double d2 = pos.getZ() + 0.5D + (float)enumfacing.getFrontOffsetZ();
        
        EntityCannonball entitycannonball = new EntityCannonball(worldIn, d0,d1,d2);
        EntityExplosiveball entityexplosiveball = new EntityExplosiveball(worldIn, d0,d1,d2);
        
        
        if (Ammo == 2) {
        entitycannonball.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + Angle), (double)enumfacing.getFrontOffsetZ(), 3, 0);
        worldIn.spawnEntityInWorld(entitycannonball);
        }
        if (Ammo == 3) {
        entityexplosiveball.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + Angle), (double)enumfacing.getFrontOffsetZ(), 3, 0);
        worldIn.spawnEntityInWorld(entityexplosiveball);
        }
        worldIn.playSound((EntityPlayer)null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE , SoundCategory.BLOCKS, 2, 1.5F);
        
        Ammo = 0;
	
  //Boooooom!
    }

	public  void setAngle(EntityPlayer playerIn) {
		Angle += 0.05F;
		if (Angle > 0.3F) {Angle = 0;}
		playerIn.addChatComponentMessage(new TextComponentString("Extra angle = " + Float.toString(Angle)));
		
	}
	
	public  void setAmmo(ItemStack heldItem) {
		Item item = heldItem.getItem();
		if (item == ModItems.cannonball) {Ammo = 2; CC = 7;}
		if (item == ModItems.explosiveball) {Ammo = 3; CC = 14;}
		
	}
    
    
    
    
    
}