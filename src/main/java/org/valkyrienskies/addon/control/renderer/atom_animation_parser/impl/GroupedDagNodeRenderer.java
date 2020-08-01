package org.valkyrienskies.addon.control.renderer.atom_animation_parser.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class GroupedDagNodeRenderer extends BasicDagNodeRenderer {

    private final List<BasicDagNodeRenderer> children;

    public GroupedDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations,
        List<BasicDagNodeRenderer> children, Vector3dc pivot) {
        super(modelName, transformations, null);
        this.children = children;
        this.pivot = pivot;
    }

    @Override
    public void render(double keyframe, int brightness) {
        for (int i = 0; i < transformations.size(); i++) {
            Vector3d customPivot = new Vector3d(pivot);
            for (int j = transformations.size() - 1; j > i; j--) {
                transformations.get(j)
                    .changePivot(customPivot, keyframe);
            }
            GlStateManager.translate(customPivot.x, customPivot.y, customPivot.z);
            transformations.get(i)
                .transform(keyframe);
            GlStateManager.translate(-customPivot.x, -customPivot.y, -customPivot.z);
        }

        for (BasicDagNodeRenderer child : children) {
            child.render(keyframe, brightness);
        }
    }
}
