package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.Math.RotationMatrices;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class CustomPlayerControllerMP extends PlayerControllerMP{

	public CustomPlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {
		super(mcIn, netHandler);
	}

	public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing)
    {
        if (!mcIn.theWorld.extinguishFire(mcIn.thePlayer, pos, facing))
        {
            playerController.func_187103_a(pos);
        }
    }

    //Called to start block breaking
    @Override
    public boolean clickBlock(BlockPos loc, EnumFacing face){
    	return super.clickBlock(loc, face);
    }

    /**
     * Resets current block damage and field_78778_j
     */
    @Override
    public void resetBlockRemoving(){      
    	super.resetBlockRemoving();
    }

    //Returns true if the hand should swing when breaking a block
    @Override
    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing){
    	return super.onPlayerDamageBlock(posBlock, directionFacing);
    }

    /**
     * Attacks an entity
     */
    @Override
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity){
    	super.attackEntity(playerIn, targetEntity);
    }


    @Override
    public void onStoppedUsingItem(EntityPlayer playerIn){
    	super.onStoppedUsingItem(playerIn);
    }
    /**
     * true for hitting entities far away.
     */
    @Override
    public boolean extendedReach(){
        return super.extendedReach();
    }
	
}
