package valkyrienwarfare.mod.client.render.tile_entity_renderers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.mod.block.BlockPhysicsInfuser;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;
import valkyrienwarfare.mod.tileentity.TileEntityPhysicsInfuser;

public class PhysicsInfuserTileEntityRenderer extends TileEntitySpecialRenderer<TileEntityPhysicsInfuser> {


    @Override
    public void render(TileEntityPhysicsInfuser tileentity, double x, double y, double z, float partialTick,
                       int destroyStage, float alpha) {

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();

        int brightness = tileentity.getWorld()
                .getCombinedLight(tileentity.getPos(), 0);

        GlStateManager.translate(.5, 0, .5);
        double keyframe = 1;

        IBlockState physicsInfuserState = tileentity.getWorld()
                .getBlockState(tileentity.getPos());
        if (physicsInfuserState.getBlock() == ValkyrienWarfareMod.INSTANCE.physicsInfuser) {
            EnumFacing enumfacing = physicsInfuserState.getValue(BlockPhysicsInfuser.FACING);
            float physicsInfuserRotation = -enumfacing.getHorizontalAngle() + 180;
            GlStateManager.rotate(physicsInfuserRotation, 0, 1, 0);
        }

        GlStateManager.translate(-.5, 0, -.5);


        // First translate the model one block to the right
        GlStateManager.translate(-1, 0, 0);
        GlStateManager.scale(2, 2, 2);
        GibsAnimationRegistry.getAnimation("physics_infuser_empty")
                .renderAnimation(keyframe, brightness);

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
