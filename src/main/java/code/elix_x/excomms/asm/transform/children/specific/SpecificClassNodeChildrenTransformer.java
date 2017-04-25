package code.elix_x.excomms.asm.transform.children.specific;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import code.elix_x.excomms.asm.transform.NodeTransformer;
import code.elix_x.excomms.asm.transform.children.NodeChildrenTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificClassNodeTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificMethodNodeTransformer;

public class SpecificClassNodeChildrenTransformer extends NodeChildrenTransformer<ClassNode> {

	private final String target;
	private final int priority;

	public SpecificClassNodeChildrenTransformer(String target, int priority, Multimap<Function<ClassNode, Pair<Class, Collection<?>>>, NodeTransformer> nodes, Function<Triple<ClassNode, ?, ?>, ClassNode> modified){
		super(nodes, modified);
		this.target = target;
		this.priority = priority;
	}

	@Override
	public Class<ClassNode> getTargetType(){
		return ClassNode.class;
	}

	@Override
	public int getPriority(){
		return priority;
	}

	@Override
	public boolean accepts(ClassNode target){
		return this.target.equals(target.name);
	}

	public static class Builder extends NodeChildrenTransformer.Builder<ClassNode> {

		public static final Function<Triple<ClassNode, ClassNode, ClassNode>, ClassNode> CLASSNODETRANSFORMERMOD = triple -> {
			if(triple.getRight() != null) return triple.getRight();
			else throw new IllegalArgumentException("Cannot remove class node!");
		};

		public static final Function<ClassNode, Pair<Class, Collection<MethodNode>>> METHODNODETRANSFORMERP2C = node -> {
			ArrayList<MethodNode> list = Lists.newArrayList(node.methods);
			list.add(null);
			return new ImmutablePair<Class, Collection<MethodNode>>(MethodNode.class, list);
		};

		public static final Function<Triple<ClassNode, MethodNode, MethodNode>, ClassNode> METHODNODETRANSFORMERMOD = triple -> {
			if(triple.getMiddle() != triple.getRight()){
				if(triple.getLeft() != null) triple.getLeft().methods.remove(triple.getMiddle());
				if(triple.getRight() != null) triple.getLeft().methods.add(triple.getRight());
			}
			return triple.getLeft();
		};

		private String target;
		private int priority;

		public Builder(String target, int priority){
			this.target = target;
			this.priority = priority;
		}

		public Builder(){

		}

		public Builder target(String target){
			this.target = target;
			return this;
		}

		public Builder priority(int priority){
			this.priority = priority;
			return this;
		}

		public Builder node(SpecificClassNodeTransformer node){
			nodeG(null, node, CLASSNODETRANSFORMERMOD);
			return this;
		}

		public Builder node(SpecificMethodNodeTransformer node){
			nodeG(METHODNODETRANSFORMERP2C, node, METHODNODETRANSFORMERMOD);
			return this;
		}

		public SpecificClassNodeChildrenTransformer build(){
			return new SpecificClassNodeChildrenTransformer(target, priority, nodes(), modified());
		}

	}

}
