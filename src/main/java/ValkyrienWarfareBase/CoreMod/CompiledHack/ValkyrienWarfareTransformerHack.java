package ValkyrienWarfareBase.CoreMod.CompiledHack;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import ValkyrienWarfareBase.CoreMod.ValkyrienWarfarePlugin;
import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformerHack implements IClassTransformer{

	private static final List<String> privilegedPackages = Arrays.asList("ValkyrienWarfareBase");
	
    @Override
    public byte[] transform(String name,String transformedName, byte[] classData){
    	try{
			for(String privilegedPackage:privilegedPackages ){
				if(name.startsWith(privilegedPackage)){
					return classData;
				}
			}
			TransformAdapterHack adapter = new TransformAdapterHack(Opcodes.ASM4,ValkyrienWarfarePlugin.isObfuscatedEnvironment);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			adapter.setCV(classWriter);
			try{
				new ClassReader(classData).accept(adapter,ClassReader.EXPAND_FRAMES);
			}catch(Exception e){}
				return classWriter.toByteArray();
		}catch(Throwable t){
			return classData;
		}
    }
}