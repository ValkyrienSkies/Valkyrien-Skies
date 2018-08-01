package com.best108.atom_animation_reader.impl;

import java.util.ArrayList;
import java.util.List;

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
		for (BasicAnimationTransform transformation : transformations) {
			transformation.transform(keyframe);
		}
		System.out.println("rendering " + modelName);
		// modelRenderer.renderModel(modelName, brightness);
	}
}
