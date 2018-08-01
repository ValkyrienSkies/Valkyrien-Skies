package com.best108.atom_animation_reader.impl;

import java.util.List;

public class BasicAnimationTransform {

	private final String animationTransform;
	private final BasicKeyframeInterpolator interpolator;
	
	public BasicAnimationTransform(String animationTransform, List<String[]> keyframes) {
		this.animationTransform = animationTransform;
		this.interpolator = new BasicKeyframeInterpolator(keyframes);
	}
	
	public void transform(double keyFrame) {
		// do something
		double value = interpolator.getValue(keyFrame);
		System.out.println(animationTransform + " : " + value);
	}
}
