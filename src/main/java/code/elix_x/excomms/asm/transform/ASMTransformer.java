package code.elix_x.excomms.asm.transform;

import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import code.elix_x.excomms.asm.transform.children.NodeChildrenTransformer;

public class ASMTransformer {

	private final ImmutableList<NodeChildrenTransformer<ClassNode>> transformers;

	public ASMTransformer(NodeChildrenTransformer<ClassNode>... transformers){
		this.transformers = ImmutableList.copyOf(Lists.newArrayList(transformers).stream().filter(transformer -> transformer.getTargetType() == ClassNode.class).sorted((node1, node2) -> node1.getPriority() - node2.getPriority()).collect(Collectors.toList()));
	}

	public ClassNode transform(ClassNode node){
		for(NodeChildrenTransformer<ClassNode> transformer : transformers)
			if(transformer.accepts(node)) node = transformer.transform(node);
		return node;
	}

	public byte[] transform(byte[] bytes, int readFlags, int writeFlags){
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		classNode = transform(classNode);
		ClassWriter writer = new ClassWriter(writeFlags);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public byte[] transform(byte[] bytes){
		return transform(bytes, 0, ClassWriter.COMPUTE_MAXS);
	}

}
