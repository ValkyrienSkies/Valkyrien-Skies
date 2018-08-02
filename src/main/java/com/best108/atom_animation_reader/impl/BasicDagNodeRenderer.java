package com.best108.atom_animation_reader.impl;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.best108.atom_animation_reader.IModelRenderer;

import valkyrienwarfare.math.Vector;

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
//		keyframe = 1;
		
		// GL11.glTranslated(0, 0.5, 0.5);
		Vector pivot = new Vector();
		if (modelName.equals("enginemaincog_geo")) {
			pivot.X =  0.252; pivot.Y = 0.3; pivot.Z = 0.697;
			
		}
		if (modelName.equals("engineconnectionrod_geo")) {;
			pivot.X =  0.592; pivot.Y = 0.241; pivot.Z = 0.621;
		}
		if (modelName.equals("enginepiston_geo")) {
			pivot.X =  0.592; pivot.Y = 0.241; pivot.Z = 0.622;
		}
		if (modelName.equals("engine_geo")) {
			pivot.X =  0.517; pivot.Y = 0.267; pivot.Z = 0.748;
		}
		if (modelName.equals("enginepowercog_geo")) {
			pivot.X =  0.474; pivot.Y = 0.166; pivot.Z = 0.833;
		}
		if (modelName.equals("enginevalvewheel_geo")) {
			pivot.X =  0.593; pivot.Y = 0.355; pivot.Z = 0.714;
		}
		GL11.glPushMatrix();
		for (int i = 0; i < transformations.size(); i++) {
			Vector customPivot = new Vector(pivot);
			for (int j = i + 1; j < transformations.size(); j++) {
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
