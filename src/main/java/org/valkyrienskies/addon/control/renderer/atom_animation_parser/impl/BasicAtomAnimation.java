package org.valkyrienskies.addon.control.renderer.atom_animation_parser.impl;

import org.valkyrienskies.addon.control.renderer.atom_animation_parser.IAtomAnimation;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;

/**
 * A very basic implementation of the IAtomAnimation interface.
 *
 * @author thebest108
 */
public class BasicAtomAnimation implements IAtomAnimation {

    private List<BasicDagNodeRenderer> dagNodes;
    private double minKeyFrame;
    private double maxKeyFrame;

    public BasicAtomAnimation(List<BasicDagNodeRenderer> dagNodes, double minKeyFrame,
        double maxKeyFrame) {
        this.dagNodes = dagNodes;
        this.minKeyFrame = minKeyFrame;
        this.maxKeyFrame = maxKeyFrame;
    }

    @Override
    public void renderAnimation(double keyframe, int brightness) {
        // Something is wrong with the maxKeyFrame
        if (keyframe < minKeyFrame || keyframe > maxKeyFrame) {
            // throw new IllegalArgumentException("Input keyframe " + keyframe + " is out of bounds!\n" + minKeyFrame + ":" + maxKeyFrame + ":");
        }
        for (BasicDagNodeRenderer dagNode : dagNodes) {
            GlStateManager.pushMatrix();
            dagNode.render(keyframe, brightness);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public double getMinKeyframe() {
        return minKeyFrame;
    }

    @Override
    public double getMaxKeyframe() {
        return maxKeyFrame;
    }

    @Override
    public void renderAnimationNode(String nodeName, double keyframe, int brightness) {
        for (BasicDagNodeRenderer dagNode : dagNodes) {
            if (dagNode.getModelName().equals(nodeName)) {
                GlStateManager.pushMatrix();
                dagNode.render(keyframe, brightness);
                GlStateManager.popMatrix();
            }
        }
    }

}
