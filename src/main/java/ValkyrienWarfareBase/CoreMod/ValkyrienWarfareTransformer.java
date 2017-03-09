package ValkyrienWarfareBase.CoreMod;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformer implements IClassTransformer {

	private static final List<String> privilegedPackages = Arrays.asList("ValkyrienWarfareBase", "jdk");

	@Override
	public byte[] transform(String name, String transformedName, byte[] classData) {
		if(classData == null){
			return classData;
		}

		try {
			for (String privilegedPackage : privilegedPackages) {
				if (name.startsWith(privilegedPackage)) {
					return classData;
				}
			}

			TransformAdapter adapter = new TransformAdapter(Opcodes.ASM5, ValkyrienWarfarePlugin.isObfuscatedEnvironment, transformedName);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			adapter.setCV(classWriter);
			try {
				new ClassReader(classData).accept(adapter, ClassReader.EXPAND_FRAMES);
				byte[] byteArray = classWriter.toByteArray();
				//Extra transforms that don't fall into typical scenarios go here; like changing the SuperClass of Entity
				byteArray = ASMUtils.doAdditionalTransforms(transformedName, byteArray);

				return byteArray;
				//Performs sanity checks and frame stack recalculations before pushing the new bytecode
//				ClassReader cr = new ClassReader(byteArray);
//				ClassWriter checkedWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//				CheckClassAdapter adapterChecker = new CheckClassAdapter(checkedWriter, true);
//				cr.accept(adapterChecker, ClassReader.EXPAND_FRAMES);
//				return checkedWriter.toByteArray();
			} catch (Exception e) {
//				System.out.println(transformedName);
				e.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return classData;
	}

}