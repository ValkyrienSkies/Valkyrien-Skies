package code.elix_x.excomms.asm.transform.specific;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import code.elix_x.excomms.asm.transform.OpcodeGroups;

public class SpecificMethodNodeTransformer extends SpecificNodeTransformer<MethodNode> {

	public static SpecificMethodNodeTransformer instructionsTransformer(String target, int priority, Function<InsnList, InsnList> insnOld2new){
		return new SpecificMethodNodeTransformer(priority, target, node -> node.instructions = insnOld2new.apply(node.instructions));
	}

	public static SpecificMethodNodeTransformer instructionsTransformer(int priority, String target, Consumer<InsnList> insnConsumer){
		return new SpecificMethodNodeTransformer(priority, target, node -> insnConsumer.accept(node.instructions));
	}

	public static SpecificMethodNodeTransformer instructionsBeginningInserter(String target, int priority, InsnList insn){
		return instructionsTransformer(priority, target, instructions -> instructions.insert(insn));
	}

	public static SpecificMethodNodeTransformer instructionsInserter(String target, int priority, Function<InsnList, AbstractInsnNode> where, InsnList insn, boolean before){
		return instructionsTransformer(priority, target, instructions -> {
			if(before) instructions.insertBefore(where.apply(instructions), insn);
			else instructions.insert(where.apply(instructions), insn);
		});
	}

	public static SpecificMethodNodeTransformer instructionsInserter(String target, int priority, Function<AbstractInsnNode, Pair<InsnList, Boolean>> inserter){
		return instructionsTransformer(priority, target, instructions -> {
			for(AbstractInsnNode node : instructions.toArray()){
				Pair<InsnList, Boolean> pair = inserter.apply(node);
				if(pair != null) if(pair.getRight()) instructions.insertBefore(node, pair.getLeft());
				else instructions.insert(node, pair.getLeft());
			}
		});
	}

	public static SpecificMethodNodeTransformer instructionsInserterBeforeReturn(String target, int priority, Supplier<InsnList> insn){
		return instructionsInserter(target, priority, node -> OpcodeGroups.RETURN.contains(node.getOpcode()) ? new ImmutablePair<>(insn.get(), true) : null);
	}

	public static SpecificMethodNodeTransformer instructionsNodesTransformer(String target, int priority, Function<AbstractInsnNode, AbstractInsnNode> insnOld2new){
		return instructionsTransformer(priority, target, instructions -> {
			for(AbstractInsnNode node : instructions.toArray()){
				AbstractInsnNode res = insnOld2new.apply(node);
				if(res != null) instructions.set(node, res);
				else instructions.remove(node);
			}
		});
	}

	private final String target;

	public SpecificMethodNodeTransformer(String target, int priority, Function<MethodNode, MethodNode> transform){
		super(MethodNode.class, priority, transform);
		this.target = target;
	}

	public SpecificMethodNodeTransformer(int priority, String target, Consumer<MethodNode> transform){
		super(priority, MethodNode.class, transform);
		this.target = target;
	}

	@Override
	public boolean accepts(MethodNode target){
		return target != null && (this.target.contains("(") ? this.target.equals(target.name + target.desc) : this.target.equals(target.name));
	}

}
