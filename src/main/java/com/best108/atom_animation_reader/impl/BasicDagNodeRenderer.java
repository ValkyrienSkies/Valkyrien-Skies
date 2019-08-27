package com.best108.atom_animation_reader.impl;

import com.best108.atom_animation_reader.IModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;

import java.util.List;

public class BasicDagNodeRenderer {

    private final String modelName;
    protected final List<BasicAnimationTransform> transformations;
    private final IModelRenderer modelRenderer;
    protected VectorImmutable pivot;

    public BasicDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations, IModelRenderer modelRenderer) {
        this.modelName = modelName;
        this.transformations = transformations;
        this.modelRenderer = modelRenderer;
        this.pivot = VectorImmutable.ZERO_VECTOR;
    }

    public void render(double keyframe, int brightness) {
        for (int i = 0; i < transformations.size(); i++) {
            Vector customPivot = pivot.createMutibleVectorCopy();
            for (int j = transformations.size() - 1; j > i; j--) {
                transformations.get(j).changePivot(customPivot, keyframe);
            }
            GlStateManager.translate(customPivot.X, customPivot.Y, customPivot.Z);
            transformations.get(i).transform(keyframe);
            GlStateManager.translate(-customPivot.X, -customPivot.Y, -customPivot.Z);
        }
//		Vector customPivot = pivot.createMutibleVectorCopy();
//		GlStateManager.translate(-customPivot.X, -customPivot.Y, -customPivot.Z);
        modelRenderer.renderModel(modelName, brightness);
    }

    public void setPivot(VectorImmutable pivot) {
        this.pivot = pivot;
    }

    public String getModelName() {
        return modelName;
    }
}
