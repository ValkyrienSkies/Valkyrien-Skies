package ValkyrienWarfareCombat.Item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemExplosiveArrow extends ItemArrow {

	public ItemExplosiveArrow() {
		super();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List itemInformation, boolean par4) {
		itemInformation.add(TextFormatting.BLUE + "Creates a WAY bigger explosion than it should.");
		itemInformation.add(TextFormatting.ITALIC + "" + TextFormatting.RED + TextFormatting.ITALIC + "Unfinished until v_0.91_alpha");
	}

	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
		EntityTippedArrow entitytippedarrow = new EntityTippedArrow(worldIn, shooter) {
			private boolean doExpl = true;

			@Override
			public void onUpdate() {
				super.onUpdate();
			}

			@Override
			public boolean isImmuneToExplosions() {
				return true;
			}

			@Override
			protected void onHit(RayTraceResult raytraceResultIn) {
				super.onHit(raytraceResultIn);
				world.createExplosion(this, posX, posY, posZ, 20F, true);
				setDead();
			}
		};
		entitytippedarrow.setPotionEffect(stack);
		return entitytippedarrow;
	}

}
