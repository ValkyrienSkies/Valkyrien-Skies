package ValkyrienWarfareBase.CoreMod;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import code.elix_x.excomms.asm.transform.ASMTransformer;
import code.elix_x.excomms.asm.transform.InsnListBuilder;
import code.elix_x.excomms.asm.transform.children.specific.SpecificClassNodeChildrenTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificMethodNodeTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformer implements IClassTransformer {

	private ASMTransformer transformer = new ASMTransformer(new SpecificClassNodeChildrenTransformer.Builder("net/minecraftforge/common/ForgeChunkManager", 0)
			.node(SpecificMethodNodeTransformer.instructionsBeginningInserter("getPersistentChunksIterableFor", 0, new InsnListBuilder(new VarInsnNode(Opcodes.ALOAD, 1), new MethodInsnNode(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "rebuildChunkIterator", "(Ljava/util/Iterator;)Ljava/util/Iterator;", false), new VarInsnNode(Opcodes.ASTORE, 1), new LabelNode()).build()))
			.build());

	@Override
	public byte[] transform(String name, String transformedName, byte[] classData){
		return transformer.transform(classData);
	}

}