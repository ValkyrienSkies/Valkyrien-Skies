package com.best108.atom_animation_reader.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.best108.atom_animation_reader.IAtomAnimation;
import com.best108.atom_animation_reader.IAtomAnimationBuilder;
import com.best108.atom_animation_reader.IModelRenderer;
import com.best108.atom_animation_reader.parsers.AtomParser;
import com.best108.atom_animation_reader.parsers.AtomParserElement;

import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.client.render.GibsAnimationRegistry;

public class BasicAtomAnimationBuilder implements IAtomAnimationBuilder {

	private final int minKeyFrame;
	private final int maxKeyFrame;
	private final List<DagNode> renderNodes;
	
	public BasicAtomAnimationBuilder(AtomParser parser) {
		minKeyFrame = Integer.valueOf(parser.head.properties.get(6)[1]);
		maxKeyFrame = Integer.valueOf(parser.head.properties.get(7)[1]);
		this.renderNodes = new ArrayList<DagNode>();
		
		for (AtomParserElement dagNodesParsed : parser.head.branches) {
			renderNodes.add(new DagNode(dagNodesParsed));
		}
		
		// Print shit
		for (DagNode dagNode : renderNodes) {
			// System.out.println(dagNode.modelName);
			for (AnimationDataNode animationNode : dagNode.animationNodes) {
				// System.out.println("     " + animationNode.animationType);
				for (String[] keyFrame : animationNode.animKeyframes.keyframes) {
					// System.out.println("          " + Arrays.toString(keyFrame));
				}
			}
		}
	}
	
	@Override
	public IAtomAnimation build(IModelRenderer modelRenderer) {
		// Generate the compiled IAtomAnimation
		List<BasicDagNodeRenderer> dagNodeRenderers = new ArrayList<BasicDagNodeRenderer>();
		for (DagNode dagNode : renderNodes) {
			// This hacky bit of code is kinda crap, maybe one day Ill move the pivot detection somewhere else.
			if (dagNode.modelName.endsWith("_pivot")) {
				// This is a pivot, do something else.
				Vector pivotPoint = new Vector();
				for (AnimationDataNode animData : dagNode.animationNodes) {
					if (animData.animationType.equals("translateX")) {
						pivotPoint.X = Double.valueOf(animData.animKeyframes.keyframes.get(0)[1]);
					}
					if (animData.animationType.equals("translateY")) {
						pivotPoint.Y = Double.valueOf(animData.animKeyframes.keyframes.get(0)[1]);
					}
					if (animData.animationType.equals("translateZ")) {
						pivotPoint.Z = Double.valueOf(animData.animKeyframes.keyframes.get(0)[1]);
					}
				}
				GibsAnimationRegistry.registerPivot(dagNode.modelName.substring(0, dagNode.modelName.length() - 6), pivotPoint.toImmutable());
			} else {
				List<BasicAnimationTransform> animations = new ArrayList<BasicAnimationTransform>();
				for (AnimationDataNode animationNode : dagNode.animationNodes) {
					BasicAnimationTransform basicTransform = new BasicAnimationTransform(animationNode.animationType, animationNode.animKeyframes.keyframes);
					animations.add(basicTransform);
				}
				BasicDagNodeRenderer dagRenderer = new BasicDagNodeRenderer(dagNode.modelName, animations, modelRenderer);
				dagNodeRenderers.add(dagRenderer);
			}
		}
		
		return new BasicAtomAnimation(dagNodeRenderers, minKeyFrame, maxKeyFrame);
	}
	
	class DagNode {
		final String modelName;
		// Nodes in the order their transform will be applied.
		final List<AnimationDataNode> animationNodes;
		
		DagNode(AtomParserElement parserElement) {
			List<String[]> properties = parserElement.properties;
			if (!properties.get(0)[0].contains("|")) {
				this.modelName = properties.get(0)[0];
			} else {
				// To handle any group names in the atom files.
				this.modelName = properties.get(0)[0].split(Pattern.quote("|"))[1];
			}
			
			this.animationNodes = new ArrayList<AnimationDataNode>();
			int currentBranch = 0;
			for (String[] line : properties) {
				if (line[0].equals("anim")) {
					// Create a new animation node.
					List<String[]> animKeyframes = parserElement.branches.get(currentBranch).properties;
					Keyframes keyFrames = new Keyframes(parserElement.branches.get(currentBranch).branches.get(0).properties);
					AnimationDataNode animationNode = new AnimationDataNode(line[2], keyFrames);
					animationNodes.add(animationNode);
					currentBranch++;
				}
			}
		}
	}
	
	class AnimationDataNode {
		final String animationType;
		final Keyframes animKeyframes;
		
		AnimationDataNode(String animationType, Keyframes animKeyframes) {
			this.animationType = animationType;
			this.animKeyframes = animKeyframes;
		}
	}
	
	class Keyframes {
		final List<String[]> keyframes;
		
		Keyframes(List<String[]> keyframes) {
			this.keyframes = keyframes;
		}
	}

}
