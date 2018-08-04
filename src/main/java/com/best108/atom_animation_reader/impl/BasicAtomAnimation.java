package com.best108.atom_animation_reader.impl;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.best108.atom_animation_reader.IAtomAnimation;

/**
 * A very basic implementation of the IAtomAnimation interface.
 * @author thebest108
 *
 */
public class BasicAtomAnimation implements IAtomAnimation {

	private List<BasicDagNodeRenderer> dagNodes;
	private double minKeyFrame;
	private double maxKeyFrame;
	
	public BasicAtomAnimation(List<BasicDagNodeRenderer> dagNodes, double minKeyFrame, double maxKeyFrame) {
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
			GL11.glPushMatrix();
			dagNode.render(keyframe, brightness);
			GL11.glPopMatrix();
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

}
