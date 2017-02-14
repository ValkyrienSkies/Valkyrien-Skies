package ValkyrienWarfareBase.CoreMod;

import java.lang.reflect.Field;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * Basically handles all the byte transforms
 * 
 * @author thebest108
 *
 */
public class TransformAdapter extends ClassVisitor {

	private static final String EntityClassName = "net/minecraft/entity/Entity";
	private static final String WorldClassName = "net/minecraft/world/World";
	private static final String WorldClientName = "net/minecraft/client/multiplayer/WorldClient";
	private static final String PacketName = "net/minecraft/network/Packet";
	private static final String EntityPlayerName = "net/minecraft/entity/player/EntityPlayer";
	private static final String RenderGlobalName = "net/minecraft/client/renderer/RenderGlobal";
	private static final String ICameraName = "net/minecraft/client/renderer/culling/ICamera";
	private static final String BlockRenderLayerName = "net/minecraft/util/BlockRenderLayer";
	private static final String ChunkProviderServerName = "net/minecraft/world/gen/ChunkProviderServer";
	private static final String PlayerListName = "net/minecraft/server/management/PlayerList";
	private static final String GameProfileName = "com/mojang/authlib/GameProfile";
	private static final String EntityPlayerMPName = "net/minecraft/entity/player/EntityPlayerMP";
	private static final String ChunkName = "net/minecraft/world/chunk/Chunk";
	private static final String RayTraceResultName = "net/minecraft/util/math/RayTraceResult";
	private static final String Vec3dName = "net/minecraft/util/math/Vec3d";
	private static final String IBlockStateName = "net/minecraft/block/state/IBlockState";
	private static final String BlockPosName = "net/minecraft/util/math/BlockPos";
	private static final String TileEntityName = "net/minecraft/tileentity/TileEntity";
	private static final String TessellatorName = "net/minecraft/client/renderer/Tessellator";
	private static final String VertexBufferName = "net/minecraft/client/renderer/VertexBuffer";
	private static final String SoundEventName = "net/minecraft/util/SoundEvent";
	private static final String SoundCategoryName = "net/minecraft/util/SoundCategory";
	private static final String ParticleName = "net/minecraft/client/particle/Particle";
	private static final String ParticleManagerName = "net/minecraft/client/particle/ParticleManager";
	private static final String ContainerName = "net/minecraft/inventory/Container";
	private static final String AxisAlignedBBName = "net/minecraft/util/math/AxisAlignedBB";
	private static final String ExplosionName = "net/minecraft/world/Explosion";
	private static final String EntityLivingBaseName = "net/minecraft/entity/EntityLivingBase";
	private static final String ViewFrustumName = "net/minecraft/client/renderer/ViewFrustum";
	private static final String EntityRendererName = "net/minecraft/client/renderer/EntityRenderer";
	private static final String FrustumName = "net/minecraft/client/renderer/culling/Frustum";

	private static final String IteratorName = "java/util/Iterator";
	private static final String PredicateName = "com/google/common/base/Predicate";
	private static final String ListName = "java/util/List";
	private static final String ClassName = "java/lang/Class";


	public TransformAdapter(int api, boolean isObfuscatedEnvironment) {
		super(api, null);
	}

	public boolean runTransformer(String calledName, String calledDesc, String calledOwner, MethodVisitor mv) {
		if (isMethod(calledDesc, "(F)L" + Vec3dName + ";", calledName, EntityClassName, "getPositionEyes", "func_174824_e", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetPositionEyes", String.format("(L%s;F)L"+Vec3dName+";", EntityClassName));
			return false;
		}
		
		if (isMethod(calledDesc, "(F)L" + Vec3dName + ";", calledName, EntityClassName, "getLook", "func_70676_i", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetLook", String.format("(L%s;F)L"+Vec3dName+";", EntityClassName));
			return false;
		}
		
		if (isMethod(calledDesc, "(DF)L" + RayTraceResultName + ";", calledName, EntityClassName, "rayTrace", "func_174822_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRayTrace", String.format("(L%s;DF)L"+RayTraceResultName+";", EntityClassName));
			return false;
		}
		
		if (isMethod(calledDesc, "(DDD)V", calledName, EntityClassName, "moveEntity", "func_70091_d", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityMove", String.format("(L%s;DDD)V", EntityClassName));
			return false;
		}

		if (isMethod(calledDesc, "()L" + Vec3dName + ";", calledName, EntityClassName, "getLookVec", "RENAMEME", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetLookVec", String.format("(L%s;)L" + Vec3dName + ";", EntityClassName));
			return false;
		}

		if (isMethod(calledDesc, "(F)V", calledName, EntityRendererName, "orientCamera", "func_78467_g", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onOrientCamera", String.format("(L%s;F)V", EntityRendererName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";)L" + BlockPosName + ";", calledName, WorldClassName, "getPrecipitationHeight", "RENAMEME", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetPrecipitationHeight", String.format("(L%s;L" + BlockPosName + ";)L" + BlockPosName + ";", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(IIIIIIZ)V", calledName, ViewFrustumName, "markBlocksForUpdate", "func_187474_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onMarkBlocksForUpdate", String.format("(L%s;IIIIIIZ)V", ViewFrustumName));
			return false;
		}

		if (isMethod(calledDesc, "()Z", calledName, EntityLivingBaseName, "isOnLadder", "func_70617_f_", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onIsOnLadder", String.format("(L%s;)Z", EntityLivingBaseName));
			return false;
		}

		if (isMethod(calledDesc, "()V", calledName, ExplosionName, "doExplosionA", "func_77278_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onExplosionA", String.format("(L%s;)V", ExplosionName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";I)I", calledName, WorldClassName, "getCombinedLight", "func_175626_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetCombinedLight", String.format("(L%s;L" + BlockPosName + ";I)I", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + ClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", calledName, WorldClassName, "getEntitiesWithinAABB", "func_175647_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetEntitiesWithinAABB", String.format("(L%s;L" + ClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", calledName, WorldClassName, "getEntitiesInAABBexcluding", "func_175674_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetEntitiesInAABBexcluding", String.format("(L%s;L" + EntityClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + IteratorName + ";)L" + IteratorName + ";", calledName, WorldClassName, "getPersistentChunkIterable", "getPersistentChunkIterable", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetPersistentChunkIterable", String.format("(L%s;L" + IteratorName + ";)L" + IteratorName + ";", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";)Z", calledName, ContainerName, "canInteractWith", "func_75145_c", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onCanInteractWith", String.format("(L%s;L" + EntityPlayerName + ";)Z", ContainerName));
			return false;
		}

		if (isMethod(calledDesc, "(DDD)D", calledName, EntityClassName, "getDistanceSq", "func_70092_e", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;DDD)D", EntityClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";)D", calledName, EntityClassName, "getDistanceSq", "func_174818_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;L" + BlockPosName + ";)D", EntityClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + ParticleName + ";)V", calledName, ParticleManagerName, "addEffect", "func_78873_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onAddEffect", String.format("(L%s;L" + ParticleName + ";)V", ParticleManagerName));
			return false;
		}

		if (isMethod(calledDesc, "(DDDL" + SoundEventName + ";L" + SoundCategoryName + ";FFZ)V", calledName, WorldClassName, "playSound", "func_184134_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onPlaySound", String.format("(L%s;DDDL" + SoundEventName + ";L" + SoundCategoryName + ";FFZ)V", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + TessellatorName + ";L" + VertexBufferName + ";L" + EntityClassName + ";F)V", calledName, RenderGlobalName, "drawBlockDamageTexture", "func_174981_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawBlockDamageTexture", String.format("(L%s;L" + TessellatorName + ";L" + VertexBufferName + ";L" + EntityClassName + ";F)V", RenderGlobalName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";L" + RayTraceResultName + ";IF)V", calledName, RenderGlobalName, "drawSelectionBox", "func_72731_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawSelectionBox", String.format("(L%s;L" + EntityPlayerName + ";L" + RayTraceResultName + ";IF)V", RenderGlobalName));
			return false;
		}

		if (isMethod(calledDesc, "(DDD)D", calledName, TileEntityName, "getDistanceSq", "func_145835_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;DDD)D", TileEntityName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";L" + ICameraName + ";F)V", calledName, RenderGlobalName, "renderEntities", "func_180446_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderEntities", String.format("(L%s;L" + EntityClassName + ";L" + ICameraName + ";F)V", RenderGlobalName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";)Z", calledName, WorldClassName, "spawnEntityInWorld", "func_72838_d", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onSpawnEntityInWorld", String.format("(L%s;L" + EntityClassName + ";)Z", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";L" + IBlockStateName + ";)Z", calledName, WorldClientName, "invalidateRegionAndSetBlock", "func_180503_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onInvalidateRegionAndSetBlock", String.format("(L%s;L" + BlockPosName + ";L" + IBlockStateName + ";)Z", WorldClientName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";DDDDIL" + PacketName + ";)V", calledName, PlayerListName, "sendToAllNearExcept", "func_148543_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSendToAllNearExcept", String.format("(L%s;L" + EntityPlayerName + ";DDDDIL" + PacketName + ";)V", PlayerListName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";L" + IBlockStateName + ";I)Z", calledName, WorldClassName, "setBlockState", "func_180501_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSetBlockState", String.format("(L%s;L" + BlockPosName + ";L" + IBlockStateName + ";I)Z", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + Vec3dName + ";L" + Vec3dName + ";ZZZ)L" + RayTraceResultName + ";", calledName, WorldClassName, "rayTraceBlocks", "func_147447_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onRayTraceBlocks", String.format("(L%s;L" + Vec3dName + ";L" + Vec3dName + ";ZZZ)L" + RayTraceResultName + ";", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";)V", calledName, WorldClassName, "onEntityRemoved", "func_72847_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityRemoved", String.format("(L%s;L" + EntityClassName + ";)V", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";)V", calledName, WorldClassName, "onEntityAdded", "func_72923_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityAdded", String.format("(L%s;L" + EntityClassName + ";)V", WorldClassName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockRenderLayerName + ";DIL" + EntityClassName + ";)I", calledName, RenderGlobalName, "renderBlockLayer", "func_174977_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderBlockLayer", String.format("(L%s;L" + BlockRenderLayerName + ";DIL" + EntityClassName + ";)I", RenderGlobalName));
			return false;
		}

		if (isMethod(calledDesc, "(L" + ChunkName + ";)V", calledName, ChunkProviderServerName, "unload", "func_189549_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onChunkUnload", String.format("(L%s;L" + ChunkName + ";)V", ChunkProviderServerName));
			return false;
		}

		return true;
	}

	private boolean isMethod(String calledDesc, String methodDesc, String calledName, String classFrom, String methodNameClear, String methodNameObsf, String calledOwner) {
		boolean correctDesc = calledDesc.equals(methodDesc);
		if (correctDesc) {
			boolean correctName = pertainsToMethod(calledName, classFrom, methodNameClear, methodNameObsf) || pertainsToMethod(calledName, calledOwner, methodNameClear, methodNameObsf) || pertainsToMethod(calledName, getRuntimeClassName(classFrom), methodNameClear, methodNameObsf) || pertainsToMethod(calledName, getRuntimeClassName(calledOwner), methodNameClear, methodNameObsf);
			if (correctName) {
				boolean correctSuperClass = InheritanceUtils.extendsClass(calledOwner, classFrom)
					|| InheritanceUtils.extendsClass(calledOwner, getRuntimeClassName(classFrom)) 
					|| InheritanceUtils.extendsClass(getRuntimeClassName(calledOwner), classFrom) 
					|| InheritanceUtils.extendsClass(getRuntimeClassName(calledOwner), getRuntimeClassName(classFrom));
				return correctSuperClass;
			}
		}
		return false;
	}

	private boolean pertainsToMethod(String calledName, String classOwningMethod, String deobsfName, String obsfName) {
		String runtimeName = getRuntimeMethodName(classOwningMethod, deobsfName, obsfName);
		return calledName.equals(runtimeName) || calledName.equals(deobsfName) || calledName.equals(obsfName);
	}

	protected String getRuntimeMethodName(String runtimeClassName, String clearMethodName, String idMethodName) {
		return methodMapReverseLookup(getMethodMap(runtimeClassName), idMethodName);
	}

	private String getRuntimeClassName(String clearClassName) {
		String obfuscatedClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(clearClassName);
		if (obfuscatedClassName == null) {
			return clearClassName;
		}
		return obfuscatedClassName;
	}

	private String getClearClassName(String runtimeClassName) {
		String obfuscatedClassName = FMLDeobfuscatingRemapper.INSTANCE.map(runtimeClassName);
		if (obfuscatedClassName == null) {
			return runtimeClassName;
		}
		return obfuscatedClassName;
	}

	public void setCV(ClassWriter writer) {
		cv = writer;
	}

	private Map<String, String> getMethodMap(String obfuscatedClassName) {
		try {
			Field field = FMLDeobfuscatingRemapper.class.getDeclaredField("methodNameMaps");
			field.setAccessible(true);
			return ((Map<String, Map<String, String>>) field.get(FMLDeobfuscatingRemapper.INSTANCE)).get(obfuscatedClassName);
		} catch (Exception ex) {
			throw new Error("Unable to access FML's deobfuscation mappings!", ex);
		}
	}

	private String methodMapReverseLookup(Map<String, String> methodMap, String idMethodName) {
		if (methodMap == null) {
			return idMethodName;
		}
		for (Map.Entry<String, String> entry : methodMap.entrySet()) {
			if (entry.getValue().equals(idMethodName)) {
				String obfuscatedName = entry.getKey();
				return obfuscatedName.substring(0, obfuscatedName.indexOf("("));
			}
		}
		return "";
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, final String methodName, String methodDesc, String signature, String[] exceptions) {
		MethodVisitor toReturn = new MethodVisitor(api, cv.visitMethod(access, methodName, methodDesc, signature, exceptions)) {
			@Override
			public void visitMethodInsn(int opcode, String calledOwner, String calledName, String calledDesc) {
				if (opcode == Opcodes.INVOKEVIRTUAL) {
					if (runTransformer(calledName, calledDesc, calledOwner, mv)) {
						super.visitMethodInsn(opcode, calledOwner, calledName, calledDesc);
					}
				} else {
					super.visitMethodInsn(opcode, calledOwner, calledName, calledDesc);
				}
			}
		};
		return toReturn;
	}

}