package code.elix_x.excomms.asm.transform;

import java.util.Collection;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class InsnListBuilder {

	private InsnList insn = new InsnList();

	public InsnListBuilder(InsnList insn){
		this.insn = insn;
	}

	public InsnListBuilder(Collection<AbstractInsnNode> nodes){
		for(AbstractInsnNode node : nodes)
			insn.add(node);
	}

	public InsnListBuilder(AbstractInsnNode... nodes){
		for(AbstractInsnNode node : nodes)
			insn.add(node);
	}

	public InsnListBuilder add(AbstractInsnNode node){
		insn.add(node);
		return this;
	}

	public InsnListBuilder add(InsnList insn){
		this.insn.add(insn);
		return this;
	}

	public InsnListBuilder insert(AbstractInsnNode node){
		insn.insert(node);
		return this;
	}

	public InsnListBuilder insert(InsnList insn){
		this.insn.insert(insn);
		return this;
	}

	public InsnList build(){
		return insn;
	}

}
