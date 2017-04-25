package ValkyrienWarfareBase.CoreMod;

import code.elix_x.excomms.asm.transform.ASMTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformer implements IClassTransformer {

	private ASMTransformer transformer = new ASMTransformer();

	@Override
	public byte[] transform(String name, String transformedName, byte[] classData){
		return transformer.transform(classData);
	}

}