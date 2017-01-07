package ValkyrienWarfareBase.Interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CustomPlayerControllerMPBAD extends PlayerControllerMP {

	public CustomPlayerControllerMPBAD(Minecraft mcIn, NetHandlerPlayClient netHandler) {
		super(mcIn, netHandler);
	}

	// Called to start block breaking
	@Override
	public boolean clickBlock(BlockPos loc, EnumFacing face) {
		return super.clickBlock(loc, face);
	}

	/**
	 * Resets current block damage and field_78778_j
	 */
	@Override
	public void resetBlockRemoving() {
		super.resetBlockRemoving();
	}

	// Returns true if the hand should swing when breaking a block
	@Override
	public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
		return super.onPlayerDamageBlock(posBlock, directionFacing);
	}

	/**
	 * Attacks an entity
	 */
	@Override
	public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {
		super.attackEntity(playerIn, targetEntity);
	}

	@Override
	public void onStoppedUsingItem(EntityPlayer playerIn) {
		super.onStoppedUsingItem(playerIn);
	}

	/**
	 * true for hitting entities far away.
	 */
	@Override
	public boolean extendedReach() {
		return super.extendedReach();
	}

}
