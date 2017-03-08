package ValkyrienWarfareCombat.Render;

import java.util.Map;

import javax.annotation.Nullable;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Render.PhysObjectRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class RenderManagerOverride extends RenderManager {

	private final RenderManager def;

	public RenderManagerOverride(RenderManager def){
		super(def.renderEngine, Minecraft.getMinecraft().getRenderItem());
		this.def = def;
	}

	/*
	 * INTERCEPT
	 */

	private boolean shouldRender(Entity entity){
		return PhysObjectRenderManager.renderingMountedEntities || !ValkyrienWarfareMod.physicsManager.isEntityFixed(entity);
	}

	@Override
	public boolean shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ){
		return shouldRender(entityIn) && def.shouldRender(entityIn, camera, camX, camY, camZ);
	}

	@Override
	public void renderEntityStatic(Entity p_188388_1_, float p_188388_2_, boolean p_188388_3_){
		if(shouldRender(p_188388_1_))
			def.renderEntityStatic(p_188388_1_, p_188388_2_, p_188388_3_);
	}

	@Override
	public void doRenderEntity(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_){
		if(shouldRender(entityIn))
			def.doRenderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);
	}

	/*
	 * We don't care
	 */

	@Override
	public Map<String, RenderPlayer> getSkinMap(){
		return def.getSkinMap();
	}

	@Override
	public void setRenderPosition(double renderPosXIn, double renderPosYIn, double renderPosZIn){
		def.setRenderPosition(renderPosXIn, renderPosYIn, renderPosZIn);
	}

	@Override
	public <T extends Entity> Render<T> getEntityClassRenderObject(Class<? extends Entity> entityClass){
		return def.getEntityClassRenderObject(entityClass);
	}

	@Override
	@Nullable
	public <T extends Entity> Render<T> getEntityRenderObject(T entityIn){
		return def.getEntityRenderObject(entityIn);
	}

	@Override
	public void cacheActiveRenderInfo(World worldIn, FontRenderer textRendererIn, Entity livingPlayerIn, Entity pointedEntityIn, GameSettings optionsIn, float partialTicks){
		def.cacheActiveRenderInfo(worldIn, textRendererIn, livingPlayerIn, pointedEntityIn, optionsIn, partialTicks);
	}

	@Override
	public void setPlayerViewY(float playerViewYIn){
		def.setPlayerViewY(playerViewYIn);
	}

	@Override
	public boolean isRenderShadow(){
		return def.isRenderShadow();
	}

	@Override
	public void setRenderShadow(boolean renderShadowIn){
		def.setRenderShadow(renderShadowIn);
	}

	@Override
	public void setDebugBoundingBox(boolean debugBoundingBoxIn){
		def.setDebugBoundingBox(debugBoundingBoxIn);
	}

	@Override
	public boolean isDebugBoundingBox(){
		return def.isDebugBoundingBox();
	}

	@Override
	public boolean isRenderMultipass(Entity p_188390_1_){
		return def.isRenderMultipass(p_188390_1_);
	}

	@Override
	public void renderMultipass(Entity p_188389_1_, float p_188389_2_){
		def.renderMultipass(p_188389_1_, p_188389_2_);
	}

	/**
	 * World sets this RenderManager's worldObj to the world provided
	 */
	@Override
	public void set(@Nullable World worldIn){
		def.set(worldIn);
	}

	@Override
	public double getDistanceToCamera(double x, double y, double z){
		return def.getDistanceToCamera(x, y, z);
	}

	/**
	 * Returns the font renderer
	 */
	@Override
	public FontRenderer getFontRenderer(){
		return def.getFontRenderer();
	}

	@Override
	public void setRenderOutlines(boolean renderOutlinesIn){
		def.setRenderOutlines(renderOutlinesIn);
	}

}
