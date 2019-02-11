package valkyrienwarfare.mod.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

/**
 * Used to register and get IBakedModels for 'gibs', which are basically any
 * models that aren't a block or an item. Provides basic simple functionality
 * for getting an IBakedModel from a ResourceLocation.
 * 
 * @author thebest108
 *
 */
public class GibsModelRegistry {

	private static final List<ResourceLocation> MODEL_TEXTURES_INTERNAL = new ArrayList<ResourceLocation>();
	public static final List<ResourceLocation> MODEL_TEXTURES_IMMUTABLE = Collections.unmodifiableList(MODEL_TEXTURES_INTERNAL);
	
	private static final Map<String, ResourceLocation> NAMES_TO_RESOURCE_LOCATION = new HashMap<String, ResourceLocation>();
	private static final Map<String, IBakedModel> NAMES_TO_BAKED_MODELS = new HashMap<String, IBakedModel>();
	private static final Map<String, BufferBuilder.State> NAMES_TO_BUFFER_STATES = new HashMap<String, BufferBuilder.State>();
	private static final Map<String, Map<Integer, VertexBuffer>> NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER = new HashMap<String, Map<Integer, VertexBuffer>>();
	
	private static final ImmutableMap.Builder<String, String> FLIP_UV_CUSTOM_DATA_BUILDER = new ImmutableMap.Builder<String, String>();
	static {
		FLIP_UV_CUSTOM_DATA_BUILDER.put("flip-v", "true");
		FLIP_UV_CUSTOM_DATA_BUILDER.put("ambient", "true");
	}
	// Used to flip the UVs of obj models. Normally this data is put in the
	// blockstates.json, but since we're bypassing that it has to be added
	// directly to the IModel.
	private static final ImmutableMap<String, String> FLIP_UV_CUSTOM_DATA = FLIP_UV_CUSTOM_DATA_BUILDER.build();
	// Used to make sure that when we simulate rendering models they're not affected by light from other blocks.
	private static final BlockPos offsetPos = new BlockPos(0, 512, 0);
	/**
	 * Note this method is very unfinished, and really is only confirmed to work on obj models.
	 * @param name
	 * @param brightness
	 */
	public static void renderGibsModel(String name, int brightness) {
		if (!NAMES_TO_RESOURCE_LOCATION.containsKey(name)) {
			throw new IllegalArgumentException("No registed gibs model with the name " + name + "!");
		}
		// If we don't have an IBakedModel for this gib then make one.
		if (!NAMES_TO_BAKED_MODELS.containsKey(name)) {
			ResourceLocation location = NAMES_TO_RESOURCE_LOCATION.get(name);
			try {
				IModel model = ModelLoaderRegistry.getModel(location).process(FLIP_UV_CUSTOM_DATA);
				IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
				NAMES_TO_BAKED_MODELS.put(name, bakedModel);
			} catch(Exception e) {
				System.err.println("No model found for: " + name);
				e.printStackTrace();
				throw new IllegalStateException();
			}
		}
		// Then if we don't have a BufferBuilder.State for this gib then make one.
		if (!NAMES_TO_BUFFER_STATES.containsKey(name)) {
			FastBlockModelRenderer.VERTEX_BUILDER.begin(7, DefaultVertexFormats.BLOCK);
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBakedModel modelFromState = NAMES_TO_BAKED_MODELS.get(name);
			blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, modelFromState,
					Blocks.AIR.getDefaultState(), offsetPos, FastBlockModelRenderer.VERTEX_BUILDER, false, 0);
			BufferBuilder.State bufferState = FastBlockModelRenderer.VERTEX_BUILDER.getVertexState();
			FastBlockModelRenderer.VERTEX_BUILDER.finishDrawing();
			FastBlockModelRenderer.VERTEX_BUILDER.reset();
			NAMES_TO_BUFFER_STATES.put(name, bufferState);
		}
		// Then if we don't have a brightness to VertexBuffer map for this gib then make one.
		if (!NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER.containsKey(name)) {
			NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER.put(name, new HashMap<Integer, VertexBuffer>());
		}
		// Then if the brightness to VertexBuffer map doesn't have a VertexBuffer for the given brightness then make one.
		if (!NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER.get(name).containsKey(brightness)) {
			BufferBuilder.State bufferState = NAMES_TO_BUFFER_STATES.get(name);
			
			FastBlockModelRenderer.VERTEX_BUILDER.setTranslation(0, 0, 0);
			FastBlockModelRenderer.VERTEX_BUILDER.begin(7, DefaultVertexFormats.BLOCK);
			FastBlockModelRenderer.VERTEX_BUILDER.setVertexState(bufferState);

			// This code adjusts the brightness of the model rendered.
			int j = FastBlockModelRenderer.VERTEX_BUILDER.vertexFormat.getNextOffset() >> 2;
			int cont = FastBlockModelRenderer.VERTEX_BUILDER.getVertexCount();
			int offsetUV = FastBlockModelRenderer.VERTEX_BUILDER.vertexFormat.getUvOffsetById(1) / 4;
			int bufferNextSize = FastBlockModelRenderer.VERTEX_BUILDER.vertexFormat.getIntegerSize();

			// Set the brightness manually, so that we don't have to create a new IBakedModel for each brightness. 
			for (int contont = 0; contont < cont; contont += 4) {
				try {
					int i = (contont) * bufferNextSize + offsetUV;
					FastBlockModelRenderer.VERTEX_BUILDER.rawIntBuffer.put(i, brightness);
					FastBlockModelRenderer.VERTEX_BUILDER.rawIntBuffer.put(i + j, brightness);
					FastBlockModelRenderer.VERTEX_BUILDER.rawIntBuffer.put(i + j * 2, brightness);
					FastBlockModelRenderer.VERTEX_BUILDER.rawIntBuffer.put(i + j * 3, brightness);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			VertexBuffer gibVertexBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
			// Now that the VERTEX_BUILDER has been filled with all the render data, we must
			// upload it to the gpu.
			// The VERTEX_UPLOADER copies the state of the VERTEX_BUILDER to
			// blockVertexBuffer, and then uploads it to the gpu.
			FastBlockModelRenderer.VERTEX_BUILDER.finishDrawing();
			FastBlockModelRenderer.VERTEX_BUILDER.reset();
			// Copy the data over from the BufferBuilder into the VertexBuffer, and then
			// upload that data to the gpu memory.
			gibVertexBuffer.bufferData(FastBlockModelRenderer.VERTEX_BUILDER.getByteBuffer());
			NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER.get(name).put(brightness, gibVertexBuffer);
		}
		// Finally, once past all these checks, we can render it.
		GlStateManager.pushMatrix();
		GlStateManager.translate(-offsetPos.getX(), -offsetPos.getY(), -offsetPos.getZ());
		FastBlockModelRenderer.renderVertexBuffer(NAMES_AND_BRIGHTNESS_TO_VERTEX_BUFFER.get(name).get(brightness));
		GlStateManager.popMatrix();
	}
	
	/**
	 * Must be run when TextureStitchEvent.Pre is thrown.
	 * 
	 * @param name
	 * @param modelLocation
	 */
	public static void registerGibsModel(String name, ResourceLocation modelLocation) {
		NAMES_TO_RESOURCE_LOCATION.put(name, modelLocation);
	}
	
	public static void generateIModels() {
		for (String name : GibsModelRegistry.NAMES_TO_RESOURCE_LOCATION.keySet()) {
			ResourceLocation modelLocation = GibsModelRegistry.NAMES_TO_RESOURCE_LOCATION.get(name);
			IModel model;
			try {
				model = ModelLoaderRegistry.getModel(modelLocation);
				GibsModelRegistry.MODEL_TEXTURES_INTERNAL.addAll(model.getTextures());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}

}
