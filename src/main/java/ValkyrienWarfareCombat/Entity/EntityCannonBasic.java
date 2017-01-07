package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareCombat.ValkyrienWarfareCombatMod;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCannonBasic extends EntityMountingWeaponBase {

	int tickDelay = 6;
	// int currentTicksOperated = 0;
	boolean isCannonLoaded = false;

	public EntityCannonBasic(World worldIn) {
		super(worldIn);
	}

	@Override
	public void onRiderInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
		if (!player.worldObj.isRemote) {
			if (canPlayerInteract(player, stack, hand)) {
				fireCannon(player, stack, hand);
			}
		}
	}

	public void fireCannon(EntityPlayer player, ItemStack stack, EnumHand hand) {
		Vec3d velocityNormal = getVectorForRotation(rotationPitch, rotationYaw);
		Vector velocityVector = new Vector(velocityNormal);
		velocityVector.multiply(2D);
		EntityCannonBall projectile = new EntityCannonBall(worldObj, velocityVector, this);
		projectile.posY += .5;
		worldObj.spawnEntityInWorld(projectile);

		isCannonLoaded = false;
		// worldObj.playSound(null, posX, posY, posZ, new SoundEvent(), SoundCategory.AMBIENT, volume, pitch, true);
	}

	public boolean canPlayerInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
		if (currentTicksOperated < 0) {
			currentTicksOperated++;
			return false;
		}
		if (!isCannonLoaded) {
			ItemStack cannonBallStack = new ItemStack(ValkyrienWarfareCombatMod.instance.cannonBall);
			ItemStack powderStack = new ItemStack(ValkyrienWarfareCombatMod.instance.powderPouch);

			boolean hasCannonBall = player.inventory.hasItemStack(cannonBallStack);
			boolean hasPowder = player.inventory.hasItemStack(powderStack);
			if (hasCannonBall && hasPowder || player.isCreative()) {
				for (ItemStack[] aitemstack : player.inventory.allInventories) {
					for (ItemStack itemstack : aitemstack) {
						if (itemstack != null && itemstack.isItemEqual(cannonBallStack)) {
							itemstack.stackSize--;
							if (itemstack.stackSize <= 0) {
								int index = player.inventory.getSlotFor(itemstack);
								player.inventory.setInventorySlotContents(index, null);
							}
						}
						if (itemstack != null && itemstack.isItemEqual(powderStack)) {
							itemstack.stackSize--;
							if (itemstack.stackSize <= 0) {
								int index = player.inventory.getSlotFor(itemstack);
								player.inventory.setInventorySlotContents(index, null);
							}
						}
					}
				}
				isCannonLoaded = true;
			}
		} else {
			currentTicksOperated++;
			if (currentTicksOperated > tickDelay) {
				// currentTicksOperated = -4;
				return true;
			}
		}

		return false;
	}

	@Override
	public void doItemDrops() {
		ItemStack itemstack = new ItemStack(ValkyrienWarfareCombatMod.instance.basicCannonSpawner, 1);

		if (this.getName() != null) {
			itemstack.setStackDisplayName(this.getName());
		}

		this.entityDropItem(itemstack, 0.0F);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		super.readEntityFromNBT(tagCompund);
		isCannonLoaded = tagCompund.getBoolean("isCannonLoaded");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		super.writeEntityToNBT(tagCompound);
		tagCompound.setBoolean("isCannonLoaded", isCannonLoaded);
	}

}
