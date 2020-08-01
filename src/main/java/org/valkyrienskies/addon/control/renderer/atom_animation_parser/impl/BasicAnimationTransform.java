package org.valkyrienskies.addon.control.renderer.atom_animation_parser.impl;

import net.minecraft.client.renderer.GlStateManager;
import org.joml.Vector3d;

import java.util.List;

public class BasicAnimationTransform {

    private final String animationTransform;
    private final BasicKeyframeInterpolator interpolator;

    public BasicAnimationTransform(String animationTransform, List<String[]> keyframes) {
        this.animationTransform = animationTransform;
        this.interpolator = new BasicKeyframeInterpolator(keyframes);
    }

    public void transform(double keyFrame) {
        float value = (float) interpolator.getValue(keyFrame);

        switch (animationTransform) {
            case "translateX":
                GlStateManager.translate(value, 0, 0);
                break;
            case "translateY":
                GlStateManager.translate(0, value, 0);
                break;
            case "translateZ":
                GlStateManager.translate(0, 0, value);
                break;
            case "rotateX":
                GlStateManager.rotate(value, 1, 0, 0);
                break;
            case "rotateY":
                GlStateManager.rotate(value, 0, 1, 0);
                break;
            case "rotateZ":
                GlStateManager.rotate(value, 0, 0, 1);
                break;
            // Unfinished operations; will most likely break any pivot points
            case "scaleX":
                GlStateManager.scale(value, 1, 1);
                break;
            case "scaleY":
                GlStateManager.scale(1, value, 1);
                break;
            case "scaleZ":
                GlStateManager.scale(1, 1, value);
                break;
        }
    }

    public void changePivot(Vector3d pivotChange, double keyframe) {
        double value = interpolator.getValue(keyframe);

        switch (animationTransform) {
            case "translateX":
                pivotChange.x += value;
                break;
            case "translateY":
                pivotChange.y += value;
                break;
            case "translateZ":
                pivotChange.z += value;
                break;
        }
    }
}
