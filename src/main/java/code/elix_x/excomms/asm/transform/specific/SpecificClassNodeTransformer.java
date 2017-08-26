package code.elix_x.excomms.asm.transform.specific;

import org.objectweb.asm.tree.ClassNode;

import java.util.function.Consumer;
import java.util.function.Function;

public class SpecificClassNodeTransformer extends SpecificNodeTransformer<ClassNode> {
	
	private final String target;
	
	public SpecificClassNodeTransformer(String target, int priority, Function<ClassNode, ClassNode> transform) {
		super(ClassNode.class, priority, transform);
		this.target = target;
	}
	
	public SpecificClassNodeTransformer(int priority, String target, Consumer<ClassNode> transform) {
		super(priority, ClassNode.class, transform);
		this.target = target;
	}
	
	public static SpecificClassNodeTransformer setParent(String target, int priority, Function<String, String> old2newParent) {
		return new SpecificClassNodeTransformer(priority, target, node -> node.superName = old2newParent.apply(node.superName));
	}
	
	public static SpecificClassNodeTransformer addInterface(String target, int priority, String iface) {
		return new SpecificClassNodeTransformer(priority, target, node -> node.interfaces.add(iface));
	}
	
	public static SpecificClassNodeTransformer removeInterface(String target, int priority, String iface) {
		return new SpecificClassNodeTransformer(priority, target, node -> node.interfaces.remove(iface));
	}
	
	@Override
	public boolean accepts(ClassNode target) {
		return this.target.equals(target.name);
	}
	
}