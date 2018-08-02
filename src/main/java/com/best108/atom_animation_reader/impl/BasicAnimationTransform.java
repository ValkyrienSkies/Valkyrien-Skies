package com.best108.atom_animation_reader.impl;

import java.util.List;

import org.lwjgl.opengl.GL11;

import valkyrienwarfare.math.Vector;

public class BasicAnimationTransform {

	private final String animationTransform;
	private final BasicKeyframeInterpolator interpolator;

	public BasicAnimationTransform(String animationTransform, List<String[]> keyframes) {
		this.animationTransform = animationTransform;
		this.interpolator = new BasicKeyframeInterpolator(keyframes);
	}
	
	public void transform(double keyFrame) {
		if (true) {
			//return;
		}
		// do something
		double value = interpolator.getValue(keyFrame);
		
		switch(animationTransform)
        {
            case "translateX":
                GL11.glTranslated(value, 0, 0);
                break;
            case "translateY":
                GL11.glTranslated(0, value, 0);
//                netTranslation.Y += value;
                break;
            case "translateZ":
                GL11.glTranslated(0, 0, value);
//                netTranslation.Z += value;
                break;
            case "rotateX":
                GL11.glRotated(value, 1, 0, 0);
                break;
            case "rotateY":
            	GL11.glRotated(value, 0, 1, 0);
                break;
            case "rotateZ":
            	GL11.glRotated(value, 0, 0, 1);
                break;
            case "scaleX":
//            	GL11.glScaled(value, 1, 1);
                break;
            case "scaleY":
//            	GL11.glScaled(1, value, 1);
                break;
            case "scaleZ":
//            	GL11.glScaled(1, 1, value);
                break;
        }
//		 System.out.println(animationTransform + " : " + value);
	}
	
	public void changePivot(Vector pivotChange, double keyframe) {
		double value = interpolator.getValue(keyframe);
		
		switch(animationTransform)
        {
            case "translateX":
            	pivotChange.X += value;
                break;
            case "translateY":
            	pivotChange.Y += value;
                break;
            case "translateZ":
            	pivotChange.Z += value;
                break;
        }
	}
}
