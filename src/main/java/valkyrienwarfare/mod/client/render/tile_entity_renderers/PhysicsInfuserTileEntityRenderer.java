package valkyrienwarfare.mod.client.render.tile_entity_renderers;

import com.best108.atom_animation_reader.IAtomAnimation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
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

        IBlockState physicsInfuserState = ValkyrienWarfareMod.INSTANCE.physicsInfuser.getStateFromMeta(tileentity.getBlockMetadata());
        EnumFacing enumfacing = physicsInfuserState.getValue(BlockPhysicsInfuser.FACING);
        int coreBrightness = physicsInfuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON) ? 15728864 : brightness;
        float physicsInfuserRotation = -enumfacing.getHorizontalAngle() + 180;
        GlStateManager.rotate(physicsInfuserRotation, 0, 1, 0);


        GlStateManager.translate(-.5, 0, -.5);


        // First translate the model one block to the right
        GlStateManager.translate(-1, 0, 0);
        GlStateManager.scale(2, 2, 2);
        GibsAnimationRegistry.getAnimation("physics_infuser_empty")
                .renderAnimation(keyframe, brightness);


        IAtomAnimation cores_animation = GibsAnimationRegistry.getAnimation("physics_infuser_cores");
        // Render only the cores that exist within the physics infuser's inventory.
        IItemHandler handler = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!handler.getStackInSlot(0).isEmpty) {
            cores_animation.renderAnimationNode("physics_core_small1_geo", keyframe, coreBrightness);
        }
        if (!handler.getStackInSlot(1).isEmpty) {
            cores_animation.renderAnimationNode("physics_core_small2_geo", keyframe, coreBrightness);
        }
        if (!handler.getStackInSlot(2).isEmpty) {
            cores_animation.renderAnimationNode("physics_core_main_geo", keyframe, coreBrightness);
        }
        if (!handler.getStackInSlot(3).isEmpty) {
            cores_animation.renderAnimationNode("physics_core_small4_geo", keyframe, coreBrightness);
        }
        if (!handler.getStackInSlot(4).isEmpty) {
            cores_animation.renderAnimationNode("physics_core_small3_geo", keyframe, coreBrightness);
        }


        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
