package com.jackredcreeper.cannon.blocks;

import java.util.List;

import com.jackredcreeper.cannon.CannonModReference;
import com.jackredcreeper.cannon.world.NewExp;
import com.jackredcreeper.cannon.world.NewExp2;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockAirMine extends Block {

	public BlockAirMine() {
		super(Material.IRON);
		setHardness(0.5f);
		setResistance(0.5f);

		setUnlocalizedName(CannonModReference.ModBlocks.AIRMINE.getUnlocalizedName());
		setRegistryName(CannonModReference.ModBlocks.AIRMINE.getRegistryName());

        this.setCreativeTab(CreativeTabs.COMBAT);

	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Any Ship hitting this will have a bad time.");
	}

	public void BlockDestroyedByPlayer(BlockPos inWorldPos, World worldObj) {

		double x = inWorldPos.getX();
    	double y = inWorldPos.getY();
    	double z = inWorldPos.getZ();

    	float size = 8F;
    	float power = 0.01F;
    	float blast = 0.01F;
    	float damage = 100F;

    	NewExp2 explosion1 = new NewExp2(worldObj,null,x,y,z,size,power,damage,blast,false,true);
        explosion1.newBoom(worldObj,null,x,y,z,size,power,damage,blast,false,true);

	}

	public void BlockDestroyedByExplosion(BlockPos inWorldPos, World worldObj) {

		double x = inWorldPos.getX();
    	double y = inWorldPos.getY();
    	double z = inWorldPos.getZ();

    	float size = 8F;
    	float power = 0.01F;
    	float blast = 0.01F;
    	float damage = 100F;

    	NewExp2 explosion1 = new NewExp2(worldObj,null,x,y,z,size,power,damage,blast,false,true);
        explosion1.newBoom(worldObj,null,x,y,z,size,power,damage,blast,false,true);

	}
}
