/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package code.elix_x.excomms.asm.transform.children.specific;

import code.elix_x.excomms.asm.transform.NodeTransformer;
import code.elix_x.excomms.asm.transform.children.NodeChildrenTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificClassNodeTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificMethodNodeTransformer;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class SpecificClassNodeChildrenTransformer extends NodeChildrenTransformer<ClassNode> {
	
	private final String target;
	private final int priority;
	
	public SpecificClassNodeChildrenTransformer(String target, int priority, Multimap<Function<ClassNode, Pair<Class, Collection<?>>>, NodeTransformer> nodes, Function<Triple<ClassNode, ?, ?>, ClassNode> modified) {
		super(nodes, modified);
		this.target = target;
		this.priority = priority;
	}
	
	@Override
	public Class<ClassNode> getTargetType() {
		return ClassNode.class;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public boolean accepts(ClassNode target) {
		return this.target.equals(target.name);
	}
	
	public static class Builder extends NodeChildrenTransformer.Builder<ClassNode> {
		
		public static final Function<Triple<ClassNode, ClassNode, ClassNode>, ClassNode> CLASSNODETRANSFORMERMOD = triple -> {
			if (triple.getRight() != null) return triple.getRight();
			else throw new IllegalArgumentException("Cannot remove class node!");
		};
		
		public static final Function<ClassNode, Pair<Class, Collection<MethodNode>>> METHODNODETRANSFORMERP2C = node -> {
			ArrayList<MethodNode> list = Lists.newArrayList(node.methods);
			list.add(null);
			return new ImmutablePair<Class, Collection<MethodNode>>(MethodNode.class, list);
		};
		
		public static final Function<Triple<ClassNode, MethodNode, MethodNode>, ClassNode> METHODNODETRANSFORMERMOD = triple -> {
			if (triple.getMiddle() != triple.getRight()) {
				if (triple.getLeft() != null) triple.getLeft().methods.remove(triple.getMiddle());
				if (triple.getRight() != null) triple.getLeft().methods.add(triple.getRight());
			}
			return triple.getLeft();
		};
		
		private String target;
		private int priority;
		
		public Builder(String target, int priority) {
			this.target = target;
			this.priority = priority;
		}
		
		public Builder() {
			
		}
		
		public Builder target(String target) {
			this.target = target;
			return this;
		}
		
		public Builder priority(int priority) {
			this.priority = priority;
			return this;
		}
		
		public Builder node(SpecificClassNodeTransformer node) {
			nodeG(null, node, CLASSNODETRANSFORMERMOD);
			return this;
		}
		
		public Builder node(SpecificMethodNodeTransformer node) {
			nodeG(METHODNODETRANSFORMERP2C, node, METHODNODETRANSFORMERMOD);
			return this;
		}
		
		public SpecificClassNodeChildrenTransformer build() {
			return new SpecificClassNodeChildrenTransformer(target, priority, nodes(), modified());
		}
		
	}
	
}
