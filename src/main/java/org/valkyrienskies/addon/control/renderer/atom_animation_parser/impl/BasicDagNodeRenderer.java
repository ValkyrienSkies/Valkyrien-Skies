package org.valkyrienskies.addon.control.renderer.atom_animation_parser.impl;

import org.valkyrienskies.addon.control.renderer.atom_animation_parser.IModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class BasicDagNodeRenderer {

    private final String modelName;
    protected final List<BasicAnimationTransform> transformations;
    private final IModelRenderer modelRenderer;
    protected Vector3dc pivot;

    public BasicDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations,
        IModelRenderer modelRenderer) {
        this.modelName = modelName;
        this.transformations = transformations;
        this.modelRenderer = modelRenderer;
        this.pivot = new Vector3d();
    }

    public void render(double keyframe, int brightness) {
        for (int i = 0; i < transformations.size(); i++) {
            Vector3d customPivot = new Vector3d(pivot);
            for (int j = transformations.size() - 1; j > i; j--) {
                transformations.get(j).changePivot(customPivot, keyframe);
            }
            GlStateManager.translate(customPivot.x, customPivot.y, customPivot.z);
            transformations.get(i).transform(keyframe);
            GlStateManager.translate(-customPivot.x, -customPivot.y, -customPivot.z);
        }
//        Vector customPivot = pivot.createMutibleVectorCopy();
//        GlStateManager.translate(-customPivot.X, -customPivot.Y, -customPivot.Z);
        modelRenderer.renderModel(modelName, brightness);
    }

    public void setPivot(Vector3dc pivot) {
        this.pivot = pivot;
    }

    public String getModelName() {
        return modelName;
    }
}
