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

	public static final String EntityClassName = "net/minecraft/entity/Entity";
	public static final String WorldClassName = "net/minecraft/world/World";
	public static final String WorldClientName = "net/minecraft/client/multiplayer/WorldClient";
	public static final String PacketName = "net/minecraft/network/Packet";
	public static final String EntityPlayerName = "net/minecraft/entity/player/EntityPlayer";
	public static final String RenderGlobalName = "net/minecraft/client/renderer/RenderGlobal";
	public static final String ICameraName = "net/minecraft/client/renderer/culling/ICamera";
	public static final String BlockRenderLayerName = "net/minecraft/util/BlockRenderLayer";
	public static final String PlayerListName = "net/minecraft/server/management/PlayerList";
	public static final String ChunkName = "net/minecraft/world/chunk/Chunk";
	public static final String RayTraceResultName = "net/minecraft/util/math/RayTraceResult";
	public static final String Vec3dName = "net/minecraft/util/math/Vec3d";
	public static final String IBlockStateName = "net/minecraft/block/state/IBlockState";
	public static final String BlockPosName = "net/minecraft/util/math/BlockPos";
	public static final String TileEntityName = "net/minecraft/tileentity/TileEntity";
	public static final String TessellatorName = "net/minecraft/client/renderer/Tessellator";
	public static final String VertexBufferName = "net/minecraft/client/renderer/VertexBuffer";
	public static final String SoundEventName = "net/minecraft/util/SoundEvent";
	public static final String SoundCategoryName = "net/minecraft/util/SoundCategory";
	public static final String ParticleName = "net/minecraft/client/particle/Particle";
	public static final String ParticleManagerName = "net/minecraft/client/particle/ParticleManager";
	public static final String ContainerName = "net/minecraft/inventory/Container";
	public static final String AxisAlignedBBName = "net/minecraft/util/math/AxisAlignedBB";
	public static final String ExplosionName = "net/minecraft/world/Explosion";
	public static final String EntityLivingBaseName = "net/minecraft/entity/EntityLivingBase";
	public static final String ViewFrustumName = "net/minecraft/client/renderer/ViewFrustum";
	public static final String EntityRendererName = "net/minecraft/client/renderer/EntityRenderer";
	public static final String IChunkGeneratorName = "net/minecraft/world/chunk/IChunkGenerator";
	public static final String RenderManagerName = "net/minecraft/client/renderer/entity/RenderManager";
	public static final String TileEntityRendererDispatcherName = "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher";

	public static final String PredicateName = "com/google/common/base/Predicate";
	public static final String ListName = "java/util/List";
	public static final String ClassName = "java/lang/Class";

	public String className;

	public TransformAdapter(int api, boolean isObfuscatedEnvironment, String className) {
		super(api, null);
		this.className = className;
	}

	public boolean runTransformer(int opcode, String calledName, String calledDesc, String calledOwner, MethodVisitor mv, boolean itf) {
		if (isMethod(calledDesc, "()L"+AxisAlignedBBName+";", calledName, TileEntityName, "getRenderBoundingBox", "func_184177_bl", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "getRenderBoundingBox", String.format("(L%s;)L"+AxisAlignedBBName+";", TileEntityName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(DDD)D", calledName, TileEntityName, "getDistanceSq", "func_145835_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "getDistanceSq", String.format("(L%s;DDD)D", TileEntityName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L"+TileEntityName+";FI)V", calledName, TileEntityRendererDispatcherName, "renderTileEntity", "func_180546_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "renderTileEntity", String.format("(L%s;L"+TileEntityName+";FI)V", TileEntityRendererDispatcherName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L"+EntityClassName+";DDDFFZ)V", calledName, RenderManagerName, "doRenderEntity", "func_188391_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "doRenderEntity", String.format("(L%s;L"+EntityClassName+";DDDFFZ)V", RenderManagerName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(IIIIII)V", calledName, WorldClassName, "markBlockRangeForRenderUpdate", "func_147458_c", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "markBlockRangeForRenderUpdate", String.format("(L%s;IIIIII)V", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L"+IChunkGeneratorName+";)V", calledName, ChunkName, "populateChunk", "func_186034_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onPopulateChunk", String.format("(L%s;L"+IChunkGeneratorName+";)V", ChunkName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L"+EntityClassName+";)V", calledName, ChunkName, "addEntity", "func_76612_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onAddEntity", String.format("(L%s;L"+EntityClassName+";)V", ChunkName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(F)L" + Vec3dName + ";", calledName, EntityClassName, "getPositionEyes", "func_174824_e", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetPositionEyes", String.format("(L%s;F)L"+Vec3dName+";", EntityClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(F)L" + Vec3dName + ";", calledName, EntityClassName, "getLook", "func_70676_i", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetLook", String.format("(L%s;F)L"+Vec3dName+";", EntityClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(DDD)V", calledName, EntityClassName, "moveEntity", "func_70091_d", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityMove", String.format("(L%s;DDD)V", EntityClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(F)V", calledName, EntityRendererName, "orientCamera", "func_78467_g", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onOrientCamera", String.format("(L%s;F)V", EntityRendererName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";)L" + BlockPosName + ";", calledName, WorldClassName, "getPrecipitationHeight", "func_175725_q", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetPrecipitationHeight", String.format("(L%s;L" + BlockPosName + ";)L" + BlockPosName + ";", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "()Z", calledName, EntityLivingBaseName, "isOnLadder", "func_70617_f_", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onIsOnLadder", String.format("(L%s;)Z", EntityLivingBaseName), itf);
			return false;
		}

		if (isMethod(calledDesc, "()V", calledName, ExplosionName, "doExplosionA", "func_77278_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onExplosionA", String.format("(L%s;)V", ExplosionName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";I)I", calledName, WorldClassName, "getCombinedLight", "func_175626_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetCombinedLight", String.format("(L%s;L" + BlockPosName + ";I)I", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + ClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", calledName, WorldClassName, "getEntitiesWithinAABB", "func_175647_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetEntitiesWithinAABB", String.format("(L%s;L" + ClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", calledName, WorldClassName, "getEntitiesInAABBexcluding", "func_175674_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetEntitiesInAABBexcluding", String.format("(L%s;L" + EntityClassName + ";L" + AxisAlignedBBName + ";L" + PredicateName + ";)L" + ListName + ";", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";)Z", calledName, ContainerName, "canInteractWith", "func_75145_c", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onCanInteractWith", String.format("(L%s;L" + EntityPlayerName + ";)Z", ContainerName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(DDD)D", calledName, EntityClassName, "getDistanceSq", "func_70092_e", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;DDD)D", EntityClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockPosName + ";)D", calledName, EntityClassName, "getDistanceSq", "func_174818_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;L" + BlockPosName + ";)D", EntityClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + ParticleName + ";)V", calledName, ParticleManagerName, "addEffect", "func_78873_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onAddEffect", String.format("(L%s;L" + ParticleName + ";)V", ParticleManagerName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(DDDL" + SoundEventName + ";L" + SoundCategoryName + ";FFZ)V", calledName, WorldClassName, "playSound", "func_184134_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onPlaySound", String.format("(L%s;DDDL" + SoundEventName + ";L" + SoundCategoryName + ";FFZ)V", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + TessellatorName + ";L" + VertexBufferName + ";L" + EntityClassName + ";F)V", calledName, RenderGlobalName, "drawBlockDamageTexture", "func_174981_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawBlockDamageTexture", String.format("(L%s;L" + TessellatorName + ";L" + VertexBufferName + ";L" + EntityClassName + ";F)V", RenderGlobalName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";L" + RayTraceResultName + ";IF)V", calledName, RenderGlobalName, "drawSelectionBox", "func_72731_b", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawSelectionBox", String.format("(L%s;L" + EntityPlayerName + ";L" + RayTraceResultName + ";IF)V", RenderGlobalName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(DDD)D", calledName, TileEntityName, "getDistanceSq", "func_145835_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onGetDistanceSq", String.format("(L%s;DDD)D", TileEntityName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityClassName + ";L" + ICameraName + ";F)V", calledName, RenderGlobalName, "renderEntities", "func_180446_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderEntities", String.format("(L%s;L" + EntityClassName + ";L" + ICameraName + ";F)V", RenderGlobalName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + EntityPlayerName + ";DDDDIL" + PacketName + ";)V", calledName, PlayerListName, "sendToAllNearExcept", "func_148543_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSendToAllNearExcept", String.format("(L%s;L" + EntityPlayerName + ";DDDDIL" + PacketName + ";)V", PlayerListName), itf);
			return false;
		}

		//These Opcode instructions act to copy the elements of the stack, allowing the setBlockState() method to effectively be called twice
		if (isMethod(calledDesc, "(L" + BlockPosName + ";L" + IBlockStateName + ";I)Z", calledName, WorldClassName, "setBlockState", "func_180501_a", calledOwner)) {
			//RRRI
			mv.visitInsn(Opcodes.DUP2_X2);
			//RIRRRI
			mv.visitInsn(Opcodes.DUP2_X2);
			//RIRIRRRI
			mv.visitInsn(Opcodes.POP2);
			//RIRIRR
			mv.visitInsn(Opcodes.DUP2_X2);
			//RIRRRIRR
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSetBlockState", "(L" + IBlockStateName + ";IL" + WorldClassName + ";L" + BlockPosName + ";)V", itf);
			//RIRR
			mv.visitInsn(Opcodes.DUP2_X2);
			//RRRIRR
			mv.visitInsn(Opcodes.POP2);
			//RRRI
			mv.visitMethodInsn(opcode, calledOwner, calledName, calledDesc, itf);

			return false;
		}

		if (isMethod(calledDesc, "(L" + Vec3dName + ";L" + Vec3dName + ";ZZZ)L" + RayTraceResultName + ";", calledName, WorldClassName, "rayTraceBlocks", "func_147447_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onRayTraceBlocks", String.format("(L%s;L" + Vec3dName + ";L" + Vec3dName + ";ZZZ)L" + RayTraceResultName + ";", WorldClassName), itf);
			return false;
		}

		if (isMethod(calledDesc, "(L" + BlockRenderLayerName + ";DIL" + EntityClassName + ";)I", calledName, RenderGlobalName, "renderBlockLayer", "func_174977_a", calledOwner)) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderBlockLayer", String.format("(L%s;L" + BlockRenderLayerName + ";DIL" + EntityClassName + ";)I", RenderGlobalName), itf);
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
	public MethodVisitor visitMethod(int access, final String methodName, String methodDesc, String signature, String[] exceptions) {
		MethodVisitor toReturn = new MethodVisitor(api, cv.visitMethod(access, methodName, methodDesc, signature, exceptions)) {
			@Override
			public void visitMethodInsn(int opcode, String calledOwner, String calledName, String calledDesc, boolean itf) {
				if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE || opcode == Opcodes.H_INVOKEVIRTUAL) {
					if (runTransformer(opcode, calledName, calledDesc, calledOwner, mv, itf)) {
						super.visitMethodInsn(opcode, calledOwner, calledName, calledDesc, itf);
					}
				} else {
					super.visitMethodInsn(opcode, calledOwner, calledName, calledDesc, itf);
				}
			}
		};
		return toReturn;
	}

}