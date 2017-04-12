package com.jackredcreeper.cannon.tileentity;

import javax.annotation.Nullable;

import com.jackredcreeper.cannon.blocks.BlockCannon;
import com.jackredcreeper.cannon.entities.EntityCannonball;
import com.jackredcreeper.cannon.entities.EntityExplosiveball;
import com.jackredcreeper.cannon.entities.EntityGrapeshot;
import com.jackredcreeper.cannon.entities.EntitySolidball;
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
    boolean CannonReady = false;
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

    
////////////////////////////////////// Make shit go boom
    
    public void firecannon(World worldIn, BlockPos pos, EnumFacing side, IBlockState state) {
    	
    	//direction
        EnumFacing enumfacing = (EnumFacing)state.getValue(BlockCannon.LOOKING);
        double d0 = pos.getX() + 0.5D + (float)enumfacing.getFrontOffsetX();
        double d1 = pos.getY() + 0.4D + (float)enumfacing.getFrontOffsetY();
        double d2 = pos.getZ() + 0.5D + (float)enumfacing.getFrontOffsetZ();
        
        EntityCannonball entitycannonball = new EntityCannonball(worldIn, d0,d1,d2);
        EntityExplosiveball entityexplosiveball = new EntityExplosiveball(worldIn, d0,d1,d2);
        EntityGrapeshot entitygrapeshot1 = new EntityGrapeshot(worldIn, d0,d1,d2);
        EntityGrapeshot entitygrapeshot2 = new EntityGrapeshot(worldIn, d0,d1,d2);
        EntityGrapeshot entitygrapeshot3 = new EntityGrapeshot(worldIn, d0,d1,d2);
        EntityGrapeshot entitygrapeshot4 = new EntityGrapeshot(worldIn, d0,d1,d2);
        EntityGrapeshot entitygrapeshot5 = new EntityGrapeshot(worldIn, d0,d1,d2);
        EntitySolidball entitysolidball = new EntitySolidball(worldIn, d0,d1,d2);
        
        
        if (Ammo == 1) {
        entitycannonball.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 1);
        worldIn.spawnEntityInWorld(entitycannonball);
        }
        if (Ammo == 2) {
        entityexplosiveball.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 1);
        worldIn.spawnEntityInWorld(entityexplosiveball);
        }
        if (Ammo == 3) {
        	entitygrapeshot5.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 4.0F);
        	entitygrapeshot4.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 4.0F);
        	entitygrapeshot3.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 4.0F);
        	entitygrapeshot2.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 4.0F);
        	entitygrapeshot1.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 3, 4.0F);
            worldIn.spawnEntityInWorld(entitygrapeshot1);
            worldIn.spawnEntityInWorld(entitygrapeshot2);
            worldIn.spawnEntityInWorld(entitygrapeshot3);
            worldIn.spawnEntityInWorld(entitygrapeshot4);
            worldIn.spawnEntityInWorld(entitygrapeshot5);
        }
        if (Ammo == 4) {
        entitysolidball.setThrowableHeading((double)enumfacing.getFrontOffsetX(), (double)((float)enumfacing.getFrontOffsetY() + (Angle/100)), (double)enumfacing.getFrontOffsetZ(), 2, 1);
        worldIn.spawnEntityInWorld(entitysolidball);
        }
        
        
        worldIn.playSound((EntityPlayer)null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE , SoundCategory.BLOCKS, 2, 1.5F);
        
        Ammo = 0;
	
    }
/////////////////////////////////////// End of making shit go boom
    
/////////////////////////////////////// Angle 'n' ammo
    
	public  void setAngle(EntityPlayer playerIn,double angle) {
		Angle += angle;
		if (Angle > 15F) {Angle = -15F;}
		if (Angle < -15F) {Angle = 15F;}
		playerIn.addChatComponentMessage(new TextComponentString("Extra Y velocity = " + Float.toString(Angle/100)));
		
	}
	
	public  void setAmmo(ItemStack heldItem) {
		Item item = heldItem.getItem();
		if (item == ModItems.cannonball) {Ammo = 1; CC = 7;}
		if (item == ModItems.explosiveball) {Ammo = 2; CC = 14;}
		if (item == ModItems.grapeshot) {Ammo = 3; CC = 7;}
		if (item == ModItems.solidball) {Ammo = 4; CC = 7;}
		CannonReady = false;
	}
    
    
    
    
    
}