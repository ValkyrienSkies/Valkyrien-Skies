package org.valkyrienskies.addon.control.renderer.infuser_core_rendering;

import java.util.List;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Used to create the custom infuser core render logic, such that the hard model is 3d and the
 * inventory model is 2d. Unfortunately forge doesn't provide a better way to do this.
 */
@SideOnly(Side.CLIENT)
public class InfuserCoreBakedModel implements IBakedModel {

    private final IBakedModel handModel, inventoryModel;

    public InfuserCoreBakedModel(IBakedModel handModel, IBakedModel inventoryModel) {
        this.handModel = handModel;
        this.inventoryModel = inventoryModel;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
        ItemCameraTransforms.TransformType cameraTransformType) {
        switch (cameraTransformType) {
            case GUI:
                return inventoryModel.handlePerspective(cameraTransformType);
            default:
                return handModel.handlePerspective(cameraTransformType);
        }
    }

    // The rest of the crap past here doesn't matter.
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side,
        long rand) {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return inventoryModel.getOverrides();
    }

}
