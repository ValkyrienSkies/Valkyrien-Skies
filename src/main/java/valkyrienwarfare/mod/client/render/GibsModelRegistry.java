package valkyrienwarfare.mod.client.render;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

/**
 * Used to register and get IBakedModels for 'gibs', which are basically any
 * models that aren't a block or an item. Provides basic simple functionality
 * for getting an IBakedModel from a ResourceLocation.
 * 
 * @author thebest108
 *
 */
public class GibsModelRegistry {

	private static final Map<ResourceLocation, IBakedModel> GIBS_BAKED_MODEL_CACHE = new HashMap<ResourceLocation, IBakedModel>();
	
	public static IBakedModel getGibModel(ResourceLocation gibLocation) {
		if (GIBS_BAKED_MODEL_CACHE.containsKey(gibLocation)) {
			return GIBS_BAKED_MODEL_CACHE.get(gibLocation);
		}
		// We don't have this model cached, we're going to have to build it.
		
		IModel mod;
		try {
			mod = ModelLoaderRegistry.getModel(gibLocation);
			IModelState state = mod.getDefaultState();
	        
	        IBakedModel model = mod.bake(state, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
	        // loadedModels.put(handle.getKey(), model);
//	        return model;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
		return null;
	}
}
