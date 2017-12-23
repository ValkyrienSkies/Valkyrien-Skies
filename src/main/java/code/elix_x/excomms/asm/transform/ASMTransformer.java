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

package code.elix_x.excomms.asm.transform;

import code.elix_x.excomms.asm.transform.children.NodeChildrenTransformer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.stream.Collectors;

public class ASMTransformer {

	private final ImmutableList<NodeChildrenTransformer<ClassNode>> transformers;

	public ASMTransformer(NodeChildrenTransformer<ClassNode>... transformers) {
		this.transformers = ImmutableList.copyOf(Lists.newArrayList(transformers).stream().filter(transformer -> transformer.getTargetType() == ClassNode.class).sorted((node1, node2) -> node1.getPriority() - node2.getPriority()).collect(Collectors.toList()));
	}

	public ClassNode transform(ClassNode node) {
		for (NodeChildrenTransformer<ClassNode> transformer : transformers)
			if (transformer.accepts(node)) node = transformer.transform(node);
		return node;
	}

	public byte[] transform(byte[] bytes, int readFlags, int writeFlags) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		classNode = transform(classNode);
		ClassWriter writer = new ClassWriter(writeFlags);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public byte[] transform(byte[] bytes) {
		return transform(bytes, 0, ClassWriter.COMPUTE_MAXS);
	}

}
