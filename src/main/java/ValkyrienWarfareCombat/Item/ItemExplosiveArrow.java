package ValkyrienWarfareCombat.Item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemExplosiveArrow extends ItemArrow {

	public ItemExplosiveArrow(){
		super();
	}

	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(worldIn, shooter){
        	private boolean doExpl = true;
        	@Override
        	public void onUpdate(){
        		super.onUpdate();
            }
        	@Override
            public boolean isImmuneToExplosions(){
                return true;
            }
        	@Override
            protected void onHit(RayTraceResult raytraceResultIn){
        		super.onHit(raytraceResultIn);
        		world.createExplosion(this, posX, posY, posZ, 20F, true);
        		setDead();
        	}
        };
        entitytippedarrow.setPotionEffect(stack);
        return entitytippedarrow;
    }

}
