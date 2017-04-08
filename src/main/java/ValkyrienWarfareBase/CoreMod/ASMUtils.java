package ValkyrienWarfareBase.CoreMod;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMUtils {

	public static byte[] doAdditionalTransforms(String transformedName, byte[] byteArray){
		if(transformedName.equals(TransformAdapter.EntityClassName.replace('/', '.'))){
			byteArray = ASMUtils.patchEntityClassSuperclass(byteArray);
		}

//		if(transformedName.equals(TransformAdapter.WorldClassName.replace('/', '.'))){
//			byteArray = ASMUtils.patchWorldClassSetBlockState(byteArray);
//		}

		return byteArray;
	}

	public static byte[] patchEntityClassSuperclass(byte[] byteArray){
		ClassNode classNode = new ClassNode();

		ClassReader superClassChangerReader = new ClassReader(byteArray);

		superClassChangerReader.accept(classNode, ClassReader.EXPAND_FRAMES);

		String newSuperClass = "ValkyrienWarfareBase/Interaction/EntityDraggable";

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

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(cw);

		return cw.toByteArray();
	}

	//Unused, just here for refrence
	public static byte[] patchWorldClassSetBlockState(byte[] byteArray){
		ClassNode classNode = new ClassNode();

		ClassReader superClassChangerReader = new ClassReader(byteArray);

		superClassChangerReader.accept(classNode, ClassReader.EXPAND_FRAMES);

		for(MethodNode method : classNode.methods){
//			System.out.println(classNode.name + ":" + method.name);
			if(method.name.equals("setBlockState") || method.name.equals("func_180501_a")){
				if(method.desc.equals("(L" + TransformAdapter.BlockPosName + ";L" + TransformAdapter.IBlockStateName + ";I)Z")){

					Iterator<AbstractInsnNode> insIterator = method.instructions.iterator();

					for(int i=0;i<1000;i++){
//						System.out.println("found it");
	//					insIterator.next();
					}

//					System.out.println(method.desc);
//					System.out.println("(L" + TransformAdapter.BlockPosName + ";L" + TransformAdapter.IBlockStateName + ";I)Z");

					AbstractInsnNode targetNode = insIterator.next();

	//				method.instructions.insertBefore(targetNode, new MethodInsnNode(Opcodes.INVOKESPECIAL, ValkyrienWarfarePlugin.PathCommon, "onSetBlockState",
	//						String.format("(L%s;L"+TransformAdapter.BlockPosName+";L"+TransformAdapter.IBlockStateName+";)L"+TransformAdapter.IBlockStateName+";",
	//								TransformAdapter.ChunkName), false));


					LabelNode skipTo = new LabelNode();

					InsnList list = new InsnList();

					list.add(new LabelNode());
					list.add(new VarInsnNode(Opcodes.ALOAD, 0));
					list.add(new VarInsnNode(Opcodes.ALOAD, 1));
					list.add(new VarInsnNode(Opcodes.ALOAD, 2));
					list.add(new VarInsnNode(Opcodes.ILOAD, 3));
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSetBlockState",
							String.format("(L%s;L"+TransformAdapter.BlockPosName+";L"+TransformAdapter.IBlockStateName+";I)V",
									TransformAdapter.WorldClassName), false));
	//				list.add(new LabelNode());


					method.instructions.insert(targetNode, list);

				}
			}
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(cw);

		return cw.toByteArray();
	}

}
