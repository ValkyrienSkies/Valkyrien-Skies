package ValkyrienWarfareBase.Mixin.client.renderer.tileentity;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher {

    @Shadow
    public double entityX;

    @Shadow
    public double entityY;

    @Shadow
    public double entityZ;

    @Shadow
    private boolean drawingBatch;

    @Shadow
    public World world;

    @Shadow
    public static double staticPlayerX;

    @Shadow
    public static double staticPlayerY;

    @Shadow
    public static double staticPlayerZ;

    @Shadow
    public void renderTileEntityAt(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {}

    @Overwrite
    public void renderTileEntity(TileEntityRendererDispatcher dispatch, TileEntity tileentityIn, float partialTicks, int destroyStage){
        BlockPos pos = tileentityIn.getPos();
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(tileentityIn.getWorld(), pos);

        if(wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null){
            try{
                if(drawingBatch){
                    dispatch.drawBatch(MinecraftForgeClient.getRenderPass());
                    dispatch.preDrawBatch();
                }

                wrapper.wrapping.renderer.setupTranslation(partialTicks);

                double playerX = TileEntityRendererDispatcher.instance.staticPlayerX;
                double playerY = TileEntityRendererDispatcher.instance.staticPlayerY;
                double playerZ = TileEntityRendererDispatcher.instance.staticPlayerZ;

                TileEntityRendererDispatcher.instance.staticPlayerX = wrapper.wrapping.renderer.offsetPos.getX();
                TileEntityRendererDispatcher.instance.staticPlayerY = wrapper.wrapping.renderer.offsetPos.getY();
                TileEntityRendererDispatcher.instance.staticPlayerZ = wrapper.wrapping.renderer.offsetPos.getZ();

                if(drawingBatch){
                    dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
                    dispatch.drawBatch(MinecraftForgeClient.getRenderPass());
                    dispatch.preDrawBatch();
                }else{
                    dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
                }
                TileEntityRendererDispatcher.instance.staticPlayerX = playerX;
                TileEntityRendererDispatcher.instance.staticPlayerY = playerY;
                TileEntityRendererDispatcher.instance.staticPlayerZ = playerZ;

                wrapper.wrapping.renderer.inverseTransform(partialTicks);
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
        }
    }

    public void renderTileEntityOriginal(TileEntity tileentityIn, float partialTicks, int destroyStage)
    {
        if (tileentityIn.getDistanceSq(this.entityX, this.entityY, this.entityZ) < tileentityIn.getMaxRenderDistanceSquared())
        {
            RenderHelper.enableStandardItemLighting();
            if(!drawingBatch || !tileentityIn.hasFastRenderer())
            {
                int i = this.world.getCombinedLight(tileentityIn.getPos(), 0);
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
            BlockPos blockpos = tileentityIn.getPos();
            this.renderTileEntityAt(tileentityIn, (double)blockpos.getX() - staticPlayerX, (double)blockpos.getY() - staticPlayerY, (double)blockpos.getZ() - staticPlayerZ, partialTicks, destroyStage);
        }
    }
}
