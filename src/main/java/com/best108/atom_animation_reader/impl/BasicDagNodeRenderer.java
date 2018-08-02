package com.best108.atom_animation_reader.impl;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.best108.atom_animation_reader.IModelRenderer;

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

		GL11.glPushMatrix();
		for (int i = 0; i < transformations.size(); i++) {
			Vector customPivot = pivot.createMutibleVectorCopy();
			for (int j = transformations.size() - 1; j > i; j--) {
				transformations.get(j).changePivot(customPivot, keyframe);
			}
			GL11.glTranslated(customPivot.X, customPivot.Y, customPivot.Z);
			transformations.get(i).transform(keyframe);
			GL11.glTranslated(-customPivot.X, -customPivot.Y, -customPivot.Z);
		}

//		GL11.glPushMatrix();
		for (int i = transformations.size() - 1;i >= 0; i--) {
//			GL11.glTranslated(pivot.X, pivot.Y, pivot.Z);
//			transformations.get(i).transform(keyframe);
//			GL11.glTranslated(-pivot.X, -pivot.Y, -pivot.Z);
		}
		
		if (!modelName.equals("e2ngine_geo")) {
			modelRenderer.renderModel(modelName, brightness);
		}
		GL11.glPopMatrix();
	}
}
