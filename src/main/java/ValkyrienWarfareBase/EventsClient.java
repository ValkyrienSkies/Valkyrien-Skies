package ValkyrienWarfareBase;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import ValkyrienWarfareBase.PhysicsManagement.PhysObjectManager;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventsClient {

	@SubscribeEvent
	public void onChunkLoadClient(ChunkEvent.Load event){
		
	}
	
	@SubscribeEvent
	public void onChunkUnloadClient(ChunkEvent.Unload event){
		
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event){
		AxisAlignedBB playerRangeBB = event.getPlayer().getEntityBoundingBox();
		List<PhysicsWrapperEntity> nearbyShips = PhysObjectManager.getNearbyPhysObjects(event.getPlayer().worldObj, playerRangeBB);
		float partialTick = event.getPartialTicks();
		boolean changed = false;
		for(PhysicsWrapperEntity wrapper:nearbyShips){
			
		
			
		}
	}
	
	public void getMouseOverOnShip(float partialTicks,PhysicsWrapperEntity wrapper)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        if (entity != null)
        {
            if (Minecraft.getMinecraft().theWorld != null)
            {
            	Minecraft.getMinecraft().mcProfiler.startSection("pick");
            	Minecraft.getMinecraft().pointedEntity = null;
                double d0 = (double)Minecraft.getMinecraft().playerController.getBlockReachDistance();
                Minecraft.getMinecraft().objectMouseOver = entity.rayTrace(d0, partialTicks);
                double d1 = d0;
                Vec3d vec3d = entity.getPositionEyes(partialTicks);
                boolean flag = false;
                int i = 3;

                if (Minecraft.getMinecraft().playerController.extendedReach())
                {
                    d0 = 6.0D;
                    d1 = 6.0D;
                }
                else
                {
                    if (d0 > 3.0D)
                    {
                        flag = true;
                    }
                }

                if (Minecraft.getMinecraft().objectMouseOver != null)
                {
                    d1 = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(vec3d);
                }

                Vec3d vec3d1 = entity.getLook(partialTicks);
                Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
                Minecraft.getMinecraft().entityRenderer.pointedEntity = null;
                Vec3d vec3d3 = null;
                float f = 1.0F;
                List<Entity> list = Minecraft.getMinecraft().theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
                {
                    public boolean apply(Entity p_apply_1_)
                    {
                        return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
                    }
                }));
                double d2 = d1;

                for (int j = 0; j < list.size(); ++j)
                {
                    Entity entity1 = (Entity)list.get(j);
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz((double)entity1.getCollisionBorderSize());
                    RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

                    if (axisalignedbb.isVecInside(vec3d))
                    {
                        if (d2 >= 0.0D)
                        {
                        	Minecraft.getMinecraft().entityRenderer.pointedEntity = entity1;
                            vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                            d2 = 0.0D;
                        }
                    }
                    else if (raytraceresult != null)
                    {
                        double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                        if (d3 < d2 || d2 == 0.0D)
                        {
                            if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity.canRiderInteract())
                            {
                                if (d2 == 0.0D)
                                {
                                	Minecraft.getMinecraft().entityRenderer.pointedEntity = entity1;
                                    vec3d3 = raytraceresult.hitVec;
                                }
                            }
                            else
                            {
                            	Minecraft.getMinecraft().entityRenderer.pointedEntity = entity1;
                                vec3d3 = raytraceresult.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }

                if (Minecraft.getMinecraft().entityRenderer.pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0D)
                {
                	Minecraft.getMinecraft().entityRenderer.pointedEntity = null;
                	Minecraft.getMinecraft().objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, (EnumFacing)null, new BlockPos(vec3d3));
                }

                if (Minecraft.getMinecraft().entityRenderer.pointedEntity != null && (d2 < d1 || Minecraft.getMinecraft().objectMouseOver == null))
                {
                	Minecraft.getMinecraft().objectMouseOver = new RayTraceResult(Minecraft.getMinecraft().entityRenderer.pointedEntity, vec3d3);

                    if (Minecraft.getMinecraft().entityRenderer.pointedEntity instanceof EntityLivingBase || Minecraft.getMinecraft().entityRenderer.pointedEntity instanceof EntityItemFrame)
                    {
                    	Minecraft.getMinecraft().pointedEntity = Minecraft.getMinecraft().entityRenderer.pointedEntity;
                    }
                }

                Minecraft.getMinecraft().mcProfiler.endSection();
            }
        }
    }
	
}
