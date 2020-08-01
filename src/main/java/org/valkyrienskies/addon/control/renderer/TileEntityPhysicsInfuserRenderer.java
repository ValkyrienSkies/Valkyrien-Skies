package org.valkyrienskies.addon.control.renderer;

import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.addon.control.renderer.atom_animation_parser.IAtomAnimation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.valkyrienskies.addon.control.block.BlockPhysicsInfuser;
import org.valkyrienskies.addon.control.tileentity.TileEntityPhysicsInfuser;

public class TileEntityPhysicsInfuserRenderer extends
    TileEntitySpecialRenderer<TileEntityPhysicsInfuser> {

    @Override
    public void render(TileEntityPhysicsInfuser tileentity, double x, double y, double z,
        float partialTick,
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

        IBlockState physicsInfuserState = ValkyrienSkiesControl.INSTANCE.vsControlBlocks.physicsInfuser
            .getStateFromMeta(tileentity.getBlockMetadata());
        EnumFacing enumfacing = physicsInfuserState.getValue(BlockPhysicsInfuser.FACING);
        int coreBrightness =
            physicsInfuserState.getValue(BlockPhysicsInfuser.INFUSER_LIGHT_ON) ? 15728864
                : brightness;
        float physicsInfuserRotation = -enumfacing.getHorizontalAngle() + 180;
        GlStateManager.rotate(physicsInfuserRotation, 0, 1, 0);

        GlStateManager.translate(-.5, 0, -.5);

        // First translate the model one block to the right
        GlStateManager.translate(-1, 0, 0);
        GlStateManager.scale(2, 2, 2);
        GibsAtomAnimationRegistry.getAnimation("physics_infuser_empty")
            .renderAnimation(keyframe, brightness);

        IAtomAnimation cores_animation = GibsAtomAnimationRegistry
            .getAnimation("physics_infuser_cores");
        // Render only the cores that exist within the physics infuser's inventory.
        IItemHandler handler = tileentity
            .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for (TileEntityPhysicsInfuser.EnumInfuserCore infuserCore : TileEntityPhysicsInfuser.EnumInfuserCore
            .values()) {
            if (!handler.getStackInSlot(infuserCore.coreSlotIndex).isEmpty) {
                GlStateManager.pushMatrix();
                GlStateManager
                    .translate(0, tileentity.getCoreVerticalOffset(infuserCore, partialTick), 0);
                cores_animation
                    .renderAnimationNode(infuserCore.coreModelName, keyframe, coreBrightness);
                GlStateManager.popMatrix();
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.resetColor();
    }
}
