package ValkyrienWarfareBase.CoreMod;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import code.elix_x.excomms.asm.transform.ASMTransformer;
import code.elix_x.excomms.asm.transform.InsnListBuilder;
import code.elix_x.excomms.asm.transform.children.specific.SpecificClassNodeChildrenTransformer;
import code.elix_x.excomms.asm.transform.specific.SpecificMethodNodeTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformer implements IClassTransformer {

	private ASMTransformer transformer = new ASMTransformer(
			new SpecificClassNodeChildrenTransformer.Builder("net/minecraftforge/common/ForgeChunkManager", 0)
			.node(SpecificMethodNodeTransformer.instructionsBeginningInserter("getPersistentChunksIterableFor", 0, new InsnListBuilder(new VarInsnNode(ALOAD, 1), new MethodInsnNode(INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "rebuildChunkIterator", "(Ljava/util/Iterator;)Ljava/util/Iterator;", false), new VarInsnNode(ASTORE, 1), new LabelNode()).build()))
			.build(),
			new SpecificClassNodeChildrenTransformer.Builder("net/minecraft/entity/player/EntityPlayer", 0)
			.node(SpecificMethodNodeTransformer.instructionsInserterBeforeReturn(ValkyrienWarfarePlugin.isObfuscatedEnvironment ? "func_180467_a" : "getBedSpawnLocation", 0, () -> new InsnListBuilder(new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1), new VarInsnNode(ILOAD, 2), new MethodInsnNode(INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "getBedSpawnLocation", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)Lnet/minecraft/util/math/BlockPos;", false)).build()))
			.build());

	@Override
	public byte[] transform(String name, String transformedName, byte[] classData){
		return transformer.transform(classData);
	}

}