package code.elix_x.excomms.asm.transform;

import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableList;

public class OpcodeGroups {

	public static final ImmutableList<Integer> RETURN = ImmutableList.of(Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN);

}
