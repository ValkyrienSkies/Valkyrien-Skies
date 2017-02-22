package ValkyrienWarfareBase.CoreMod;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMUtils {
	
	public static byte[] doAdditionalTransforms(String transformedName, byte[] byteArray){
		if(transformedName.equals(TransformAdapter.EntityClassName.replace('/', '.'))){
			byteArray = ASMUtils.patchEntityClassSuperclass(byteArray);
		}
		
		return byteArray;
	}
	
	public static byte[] patchEntityClassSuperclass(byte[] byteArray){
		ClassNode classNode = new ClassNode();
		
		ClassReader superClassChangerReader = new ClassReader(byteArray);
		
		superClassChangerReader.accept(classNode, ClassReader.EXPAND_FRAMES);
		
		String newSuperClass = "ValkyrienWarfareBase/EntityMultiWorldFixes/EntityDraggable";
		
		classNode.superName = newSuperClass;
		
		for(MethodNode method : classNode.methods){
			if(method.name.equals("<init>")){
				Iterator<AbstractInsnNode> insIterator = method.instructions.iterator();
				AbstractInsnNode targetNode = null;
				//Finds the 1st node that calls InvokeSpecial, this node calls directly to the Superclass <init> method, so it has to be changed
				while(insIterator.hasNext()){
					AbstractInsnNode node = insIterator.next();
					if(node.getOpcode() == Opcodes.INVOKESPECIAL){
						targetNode = node;
						break;
					}
				}
				//Removes the superclass <init> call that goes to Object.class that was in Entity because Object was the superclass, instead it is replaced with
				//the EntityDraggable <init>
				method.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESPECIAL, newSuperClass, "<init>", "()V", false));
				//Removes the old <init> call going to Object
				method.instructions.remove(targetNode);
			}
		}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(cw);
		
		return cw.toByteArray();
	}
	
}
