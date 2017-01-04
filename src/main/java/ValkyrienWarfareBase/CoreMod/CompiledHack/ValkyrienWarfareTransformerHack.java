package ValkyrienWarfareBase.CoreMod.CompiledHack;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import ValkyrienWarfareBase.CoreMod.ValkyrienWarfarePlugin;
import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformerHack implements IClassTransformer {

	private static final List<String> privilegedPackages = Arrays.asList("ValkyrienWarfareBase", "jdk");

	@Override
	public byte[] transform(String name, String transformedName, byte[] classData) {
		try {
			for (String privilegedPackage : privilegedPackages) {
				if (name.startsWith(privilegedPackage)) {
					return classData;
				}
			}
			TransformAdapterHack adapter = new TransformAdapterHack(Opcodes.ASM4, ValkyrienWarfarePlugin.isObfuscatedEnvironment);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			adapter.setCV(classWriter);
			try {
				new ClassReader(classData).accept(adapter, ClassReader.EXPAND_FRAMES);
				// Unsafe transformed bytes, JVM needs to reformat them
				byte[] byteArray = classWriter.toByteArray();
				ClassReader cr = new ClassReader(byteArray);
				ClassWriter checkedWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				CheckClassAdapter adapterChecker = new CheckClassAdapter(checkedWriter, true);
				cr.accept(adapterChecker, ClassReader.EXPAND_FRAMES);
				return checkedWriter.toByteArray();
			} catch (Exception e) {
			}
		} catch (Throwable t) {
		}
		return classData;
	}
}