package ValkyrienWarfareBase.CoreMod;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import net.minecraft.launchwrapper.IClassTransformer;

public class ValkyrienWarfareTransformer implements IClassTransformer{

	private static final List<String> privilegedPackages = Arrays.asList("ValkyrienWarfareBase","jdk");
	
    @Override
    public byte[] transform(String name,String transformedName, byte[] classData){
    	try{
			for(String privilegedPackage:privilegedPackages ){
				if(name.startsWith(privilegedPackage)){
					return classData;
				}
			}
			TransformAdapter adapter = getAdaptor();
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
			adapter.setCV(classWriter);
			try{
				new ClassReader(classData).accept(adapter,ClassReader.EXPAND_FRAMES);
			}catch(Exception e){
//				e.printStackTrace();
			}
			//Unsafe transformed bytes, JVM needs to reformat them
			byte[] byteArray = classWriter.toByteArray();
			
//			ClassReader cr = new ClassReader(byteArray);
//            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//            ClassVisitor cv = new CheckClassAdapter(cw);
//            cr.accept(cv, 0);
//			return cw.toByteArray();
			ClassReader cr = new ClassReader(byteArray);
			ClassWriter checkedWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			CheckClassAdapter adapterChecker = new CheckClassAdapter(checkedWriter,true);
			cr.accept(adapterChecker, ClassReader.EXPAND_FRAMES);
			return checkedWriter.toByteArray();
		}catch(Throwable t){
			return classData;
		}
    }
    
    public TransformAdapter getAdaptor(){
    	return new TransformAdapter(Opcodes.ASM4,ValkyrienWarfarePlugin.isObfuscatedEnvironment);
    }
}