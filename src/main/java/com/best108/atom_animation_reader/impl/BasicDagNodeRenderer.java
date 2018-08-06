package com.best108.atom_animation_reader.impl;

import java.util.List;

import com.best108.atom_animation_reader.IModelRenderer;

import net.minecraft.client.renderer.GlStateManager;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;
import valkyrienwarfare.mod.coordinates.VectorImmutable;

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
		VectorImmutable pivot = GibsAnimationRegistry.getPivot(modelName);

		for (int i = 0; i < transformations.size(); i++) {
			Vector customPivot = pivot.createMutibleVectorCopy();
			for (int j = transformations.size() - 1; j > i; j--) {
				transformations.get(j).changePivot(customPivot, keyframe);
			}
			GlStateManager.translate(customPivot.X, customPivot.Y, customPivot.Z);
			transformations.get(i).transform(keyframe);
			GlStateManager.translate(-customPivot.X, -customPivot.Y, -customPivot.Z);
		}
		modelRenderer.renderModel(modelName, brightness);
	}
}
