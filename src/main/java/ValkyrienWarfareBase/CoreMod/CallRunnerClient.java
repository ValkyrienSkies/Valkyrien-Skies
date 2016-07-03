package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public class CallRunnerClient {

	public static void onRenderEntities(RenderGlobal renderGlobal,Entity renderViewEntity, ICamera camera, float partialTicks)
    {
		renderGlobal.renderEntities(renderViewEntity, camera, partialTicks);
    }
	
	public static boolean onInvalidateRegionAndSetBlock(WorldClient client,BlockPos pos, IBlockState state)
    {
		int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        client.invalidateBlockReceiveRegion(i, j, k, i, j, k);
        return CallRunner.onSetBlockState(client,pos, state, 3);
    }
	
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
