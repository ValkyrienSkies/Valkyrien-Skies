package com.best108.atom_animation_reader.impl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.best108.atom_animation_reader.IModelRenderer;

public class BasicDagNodeRenderer {

	private String modelName;
	private List<BasicAnimationTransform> transformations;
	private IModelRenderer modelRenderer;
	
	public BasicDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations, IModelRenderer modelRenderer) {
		this.modelName = modelName;
		this.transformations = transformations;
		this.modelRenderer = modelRenderer;
	}
	
	public void render(double keyframe, int brightness) {
		// GL11.glTranslated(0, 0.5, 0.5);
		for (BasicAnimationTransform transformation : transformations) {
			transformation.transform(keyframe);
		}
		for (int i = transformations.size() - 1;i >= 0; i--) {
//			transformations.get(i).transform(keyframe);
		}
		// System.out.println("rendering " + modelName);
		
		modelRenderer.renderModel(modelName, brightness);
	}
}
