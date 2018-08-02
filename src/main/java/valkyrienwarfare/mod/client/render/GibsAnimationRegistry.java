package valkyrienwarfare.mod.client.render;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.best108.atom_animation_reader.IAtomAnimation;
import com.best108.atom_animation_reader.IAtomAnimationBuilder;
import com.best108.atom_animation_reader.IModelRenderer;
import com.best108.atom_animation_reader.impl.BasicAtomAnimationBuilder;
import com.best108.atom_animation_reader.parsers.AtomParser;
import com.best108.atom_animation_reader.parsers.PivotParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import valkyrienwarfare.mod.coordinates.VectorImmutable;

public class GibsAnimationRegistry {

	private static final Map<String, IAtomAnimation> ANIMATION_MAP = new HashMap<String, IAtomAnimation>();
	private static final Map<String, VectorImmutable> MODEL_NAME_TO_PIVOTS = new HashMap<String, VectorImmutable>();
	private static final IModelRenderer MODEL_RENDERER = new IModelRenderer() {
		@Override
		public void renderModel(String modelName, int renderBrightness) {
			GibsModelRegistry.renderGibsModel(modelName, renderBrightness);
		}
	};
	
	public static void registerAnimation(String name, ResourceLocation location) {
		try {
			IResource animationResource = Minecraft.getMinecraft().getResourceManager().getResource(location);
			Scanner data = new Scanner(animationResource.getInputStream());
			AtomParser dataParser = new AtomParser(data);
			IAtomAnimationBuilder animationBuilder = new BasicAtomAnimationBuilder(dataParser);
			ANIMATION_MAP.put(name, animationBuilder.build(MODEL_RENDERER));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void registerPivots(ResourceLocation location) {
		try {
			IResource animationResource = Minecraft.getMinecraft().getResourceManager().getResource(location);
			Scanner data = new Scanner(animationResource.getInputStream());
			PivotParser pivotParser = new PivotParser(data);
			pivotParser.registerPivots();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void registerPivot(String modelName, VectorImmutable pivotPoint) {
		if (MODEL_NAME_TO_PIVOTS.containsKey(modelName)) {
			throw new IllegalArgumentException();
		}
		System.out.println("Registered the pivot " + pivotPoint.createMutibleVectorCopy().toRoundedString() + " for " + modelName);
		MODEL_NAME_TO_PIVOTS.put(modelName, pivotPoint);
	}
	
	public static IAtomAnimation getAnimation(String name) {
		return ANIMATION_MAP.get(name);
	}
	
	public static VectorImmutable getPivot(String modelName) {
		if (MODEL_NAME_TO_PIVOTS.containsKey(modelName)) {
			return MODEL_NAME_TO_PIVOTS.get(modelName);
		} else {
			return VectorImmutable.ZERO_VECTOR;
		}
	}
}
