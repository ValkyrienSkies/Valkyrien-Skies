package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class CallRunnerClient {

	public static int onRenderBlockLayer(RenderGlobal renderer,BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn){
		for(PhysicsWrapperEntity wrapper:ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderer.theWorld).physicsEntities){
			switch(blockLayerIn){
				case CUTOUT:
					if(wrapper.wrapping.renderer.needsCutoutUpdate){
						wrapper.wrapping.renderer.updateList(blockLayerIn);
					}
					break;
				case CUTOUT_MIPPED:
					if(wrapper.wrapping.renderer.needsCutoutMippedUpdate){
						wrapper.wrapping.renderer.updateList(blockLayerIn);
					}
					break;
				case SOLID:
					if(wrapper.wrapping.renderer.needsSolidUpdate){
						wrapper.wrapping.renderer.updateList(blockLayerIn);
					}
					break;
				case TRANSLUCENT:
					if(wrapper.wrapping.renderer.needsTranslucentUpdate){
						wrapper.wrapping.renderer.updateList(blockLayerIn);
					}
					break;
				default:
					break;
			}
			wrapper.wrapping.renderer.renderBlockLayer(blockLayerIn,partialTicks,pass);
		}
		return renderer.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
	}

}
