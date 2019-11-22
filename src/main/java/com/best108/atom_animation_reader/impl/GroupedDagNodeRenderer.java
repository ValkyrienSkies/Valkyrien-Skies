package com.best108.atom_animation_reader.impl;

import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import org.valkyrienskies.mod.common.coordinates.VectorImmutable;
import org.valkyrienskies.mod.common.math.Vector;

public class GroupedDagNodeRenderer extends BasicDagNodeRenderer {

    private final List<BasicDagNodeRenderer> children;

    public GroupedDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations,
        List<BasicDagNodeRenderer> children, VectorImmutable pivot) {
        super(modelName, transformations, null);
        this.children = children;
        this.pivot = pivot;
    }

    @Override
    public void render(double keyframe, int brightness) {
        for (int i = 0; i < transformations.size(); i++) {
            Vector customPivot = new Vector(pivot);
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
