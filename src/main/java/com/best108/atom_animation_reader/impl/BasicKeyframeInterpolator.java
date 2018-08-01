package com.best108.atom_animation_reader.impl;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

/**
 * A linear basic keyframe interpolator.
 * @author thebest108
 *
 */
public class BasicKeyframeInterpolator {

	private final List<Pair<Double, Double>> knownFrames;
	
	public BasicKeyframeInterpolator(List<String[]> keyframes) {
		this.knownFrames = new ArrayList<Pair<Double, Double>>();
		for (String[] frame : keyframes) {
			Pair<Double, Double> framePair = new Pair<Double, Double>(Double.valueOf(frame[0]), Double.valueOf(frame[1]));
			knownFrames.add(framePair);
		}
	}
	
	public double getValue(double keyframe) {
		for (int i = 0; i < knownFrames.size(); i++) {
			double currentKeyframe = knownFrames.get(i).getKey();
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
}
