package code.elix_x.excomms.asm.transform;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Opcodes;

public class OpcodeGroups {

	public static final ImmutableList<Integer> RETURN = ImmutableList.of(Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN, Opcodes.RETURN);

}
