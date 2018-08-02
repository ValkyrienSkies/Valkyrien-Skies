package com.best108.atom_animation_reader.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * A linear basic keyframe interpolator.
 * @author thebest108
 *
 */
public class BasicKeyframeInterpolator {

	private final List<KeyFrame> knownFrames;
	
	public BasicKeyframeInterpolator(List<String[]> keyframes) {
		this.knownFrames = new ArrayList<KeyFrame>();
		for (String[] frame : keyframes) {
			KeyFrame framePair = new KeyFrame(Double.valueOf(frame[0]), Double.valueOf(frame[1]));
			knownFrames.add(framePair);
		}
	}
	
	public double getValue(double keyframe) {
		if (knownFrames.size() == 1) {
			return knownFrames.get(0).getValue();
		}
		for (int i = 0; i < knownFrames.size(); i++) {
			double currentKeyframe = knownFrames.get(i).getKey();
			if (currentKeyframe == keyframe) {
				return knownFrames.get(i).getValue();
			}
			if (currentKeyframe > keyframe) {
				double lastKeyFrame = knownFrames.get(i - 1).getKey();
				double currentKeyFrameValue = knownFrames.get(i).getValue();
				double lastKeyFrameValue = knownFrames.get(i - 1).getValue();
				
				double frameDelta = (keyframe - lastKeyFrame) / (currentKeyframe - lastKeyFrame);
				
				return lastKeyFrameValue + ((currentKeyFrameValue - lastKeyFrameValue) * frameDelta);
			}
		}
		return 0;
	}
	
	private class KeyFrame {
		final double keyframe;
		final double value;
		
		KeyFrame(double keyframe, double value) {
			this.keyframe = keyframe;
			this.value = value;
		}
		
		double getKey()	{
			return keyframe;
		}
		
		double getValue() {
			return value;
		}
	}
}
