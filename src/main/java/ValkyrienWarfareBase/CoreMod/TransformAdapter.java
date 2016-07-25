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
 * @author thebest108
 *
 */
public class TransformAdapter extends ClassVisitor{
	private String m_className;
	public boolean m_isObfuscatedEnvironment;
	
	private static final String RawEntityClassName = "net/minecraft/entity/Entity";
	private static final String RawWorldClassName = "net/minecraft/world/World";
	private static final String RawWorldClientName = "net/minecraft/client/multiplayer/WorldClient";
	private static final String RawPacketName = "net/minecraft/network/Packet";
	private static final String RawEntityPlayerName = "net/minecraft/entity/player/EntityPlayer";
	private static final String RawRenderGlobalName = "net/minecraft/client/renderer/RenderGlobal";
	private static final String RawICameraName = "net/minecraft/client/renderer/culling/ICamera";
	private static final String RawBlockRenderLayerName = "net/minecraft/util/BlockRenderLayer";
	private static final String RawChunkProviderServerName = "net/minecraft/world/gen/ChunkProviderServer";
	private static final String RawPlayerListName = "net/minecraft/server/management/PlayerList";
	private static final String RawGameProfileName = "com/mojang/authlib/GameProfile";
	private static final String RawEntityPlayerMPName = "net/minecraft/entity/player/EntityPlayerMP";
	private static final String RawChunkName = "net/minecraft/world/chunk/Chunk";
	private static final String RawRayTraceResult = "net/minecraft/util/math/RayTraceResult";
	private static final String RawVec3dName = "net/minecraft/util/math/Vec3d";
	private static final String RawIBlockStateName = "net/minecraft/block/state/IBlockState";
	private static final String RawBlockPosName = "net/minecraft/util/math/BlockPos";
	private static final String RawTileEntityName = "net/minecraft/tileentity/TileEntity";
	private static final String RawTessellatorName = "net/minecraft/client/renderer/Tessellator";
	private static final String RawVertexBufferName = "net/minecraft/client/renderer/VertexBuffer";
	private static final String RawSoundEventName = "net/minecraft/util/SoundEvent";
	private static final String RawSoundCategoryName = "net/minecraft/util/SoundCategory";
	private static final String RawParticleName = "net/minecraft/client/particle/Particle";
	private static final String RawParticleManagerName = "net/minecraft/client/particle/ParticleManager";
	private static final String RawContainerName = "net/minecraft/inventory/Container";
	private static final String RawAxisAlignedBBName = "net/minecraft/util/math/AxisAlignedBB";
	
	private static final String IteratorName = "java/util/Iterator";
	private static final String PredicateName = "com/google/common/base/Predicate";
	private static final String ListName = "java/util/List";
	private static final String ClassName = "java/lang/Class";
	
	private final String ParticleName;
	private final String ParticleManagerName;
	private final String SoundEventName;
	private final String SoundCategoryName;
	private final String WorldClassName;
	private final String RenderGlobalName;
	private final String EntityClassName;
	private final String VertexBufferName;
	private final String TessellatorName;
	private final String EntityPlayerName;
	private final String RayTraceResult;
	private final String TileEntityName;
	private final String ICameraName;
	private final String IBlockStateName;
	private final String BlockPosName;
	private final String WorldClientName;
	private final String PlayerListName;
	private final String PacketName;
	private final String Vec3dName;
	private final String GameProfileName;
	private final String EntityPlayerMPName;
	private final String BlockRenderLayerName;
	private final String ChunkName;
	private final String ChunkProviderServerName;
	private final String ContainerName;
	private final String AxisAlignedBBName;
	
	private boolean correctDesc,correctName,correctSuperClass;

	public TransformAdapter( int api, boolean isObfuscatedEnvironment ){
		super( api, null );
		m_isObfuscatedEnvironment = isObfuscatedEnvironment;
		m_className = null;
		
		EntityClassName = getRuntimeClassName(RawEntityClassName);
		WorldClassName = getRuntimeClassName(RawWorldClassName);
		WorldClientName = getRuntimeClassName(RawWorldClientName);
		PacketName = getRuntimeClassName(RawPacketName);
		EntityPlayerName = getRuntimeClassName(RawEntityPlayerName);
		RenderGlobalName = getRuntimeClassName(RawRenderGlobalName);
		ICameraName = getRuntimeClassName(RawICameraName);
		BlockRenderLayerName = getRuntimeClassName(RawBlockRenderLayerName);
		ChunkProviderServerName = getRuntimeClassName(RawChunkProviderServerName);
		PlayerListName = getRuntimeClassName(RawPlayerListName);
		GameProfileName = getRuntimeClassName(RawGameProfileName);
		EntityPlayerMPName = getRuntimeClassName(RawEntityPlayerMPName);
		ChunkName = getRuntimeClassName(RawChunkName);
		RayTraceResult = getRuntimeClassName(RawRayTraceResult);
		Vec3dName = getRuntimeClassName(RawVec3dName);
		IBlockStateName = getRuntimeClassName(RawIBlockStateName);
		BlockPosName = getRuntimeClassName(RawBlockPosName);
		TileEntityName = getRuntimeClassName(RawTileEntityName);
		TessellatorName = getRuntimeClassName(RawTessellatorName);
		VertexBufferName = getRuntimeClassName(RawVertexBufferName);
		SoundEventName = getRuntimeClassName(RawSoundEventName);
		SoundCategoryName = getRuntimeClassName(RawSoundCategoryName);
		ParticleName = getRuntimeClassName(RawParticleName);
		ParticleManagerName = getRuntimeClassName(RawParticleManagerName);
		ContainerName = getRuntimeClassName(RawContainerName);
		AxisAlignedBBName = getRuntimeClassName(RawAxisAlignedBBName);
	}

	private boolean runTransformer(String calledName,String calledDesc,String calledOwner,MethodVisitor mv){
		//TBA
		if(calledName.equals("getEntitiesWithinAABB")){
			for(int i=0;i<100;i++){
//				System.out.println(calledDesc);
			}
		}
		if(isMethod(calledDesc,"(L"+BlockPosName+";I)I",calledName,WorldClassName,"getCombinedLight","func_175626_b",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawBlockPosName+";I)I",calledName,RawWorldClassName,"getCombinedLight","func_175626_b",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetCombinedLight", String.format( "(L%s;L"+BlockPosName+";I)I", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+ClassName+";L"+AxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";", calledName,WorldClassName,"getEntitiesWithinAABB","func_175647_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+ClassName+";L"+RawAxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";",calledName,RawWorldClassName,"getEntitiesWithinAABB","func_175647_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetEntitiesWithinAABB", String.format( "(L%s;L"+ClassName+";L"+AxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityClassName+";L"+AxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";", calledName,WorldClassName,"getEntitiesInAABBexcluding","func_175674_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityClassName+";L"+RawAxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";",calledName,RawWorldClassName,"getEntitiesInAABBexcluding","func_175674_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetEntitiesInAABBexcluding", String.format( "(L%s;L"+EntityClassName+";L"+AxisAlignedBBName+";L"+PredicateName+";)L"+ListName+";", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+IteratorName+";)L"+IteratorName+";",calledName,WorldClassName,"getPersistentChunkIterable","getPersistentChunkIterable",calledOwner)
			||
			isMethod(calledDesc,"(L"+IteratorName+";)L"+IteratorName+";",calledName,RawWorldClassName,"getPersistentChunkIterable","getPersistentChunkIterable",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetPersistentChunkIterable", String.format( "(L%s;L"+IteratorName+";)L"+IteratorName+";", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityPlayerName+";)Z",calledName,ContainerName,"canInteractWith","func_75145_c",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerName+";)Z",calledName,RawContainerName,"canInteractWith","func_75145_c",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onCanInteractWith", String.format( "(L%s;L"+EntityPlayerName+";)Z", ContainerName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(DDD)D",calledName,EntityClassName,"getDistanceSq","func_70092_e",calledOwner)
			||
			isMethod(calledDesc,"(DDD)D",calledName,RawEntityClassName,"getDistanceSq","func_70092_e",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetDistanceSq", String.format( "(L%s;DDD)D", EntityClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+BlockPosName+";)D",calledName,EntityClassName,"getDistanceSq","func_174818_b",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawBlockPosName+";)D",calledName,RawEntityClassName,"getDistanceSq","func_174818_b",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetDistanceSq", String.format( "(L%s;L"+BlockPosName+";)D", EntityClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityPlayerName+";DDDL"+SoundEventName+";L"+SoundCategoryName+";FF)V",calledName,WorldClassName,"playSound","func_184148_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerName+";DDDL"+RawSoundEventName+";L"+RawSoundCategoryName+";FF)V",calledName,RawWorldClassName,"playSound","func_184148_a",calledOwner)	){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onPlaySound2", String.format( "(L%s;L"+EntityPlayerName+";DDDL"+SoundEventName+";L"+SoundCategoryName+";FF)V", WorldClassName) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityPlayerName+";L"+BlockPosName+";L"+SoundEventName+";L"+SoundCategoryName+";FF)V",calledName,WorldClassName,"playSound","func_184133_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerName+";L"+RawBlockPosName+";L"+RawSoundEventName+";L"+RawSoundCategoryName+";FF)V",calledName,RawWorldClassName,"playSound","func_184133_a",calledOwner)	){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onPlaySound1", String.format( "(L%s;L"+EntityPlayerName+";L"+BlockPosName+";L"+SoundEventName+";L"+SoundCategoryName+";FF)V", WorldClassName) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+ParticleName+";)V",calledName,ParticleManagerName,"addEffect","func_78873_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawParticleName+";)V",calledName,RawParticleManagerName,"addEffect","func_78873_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onAddEffect", String.format( "(L%s;L"+ParticleName+";)V", ParticleManagerName) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(DDDL"+SoundEventName+";L"+SoundCategoryName+";FFZ)V",calledName,WorldClassName,"playSound","func_184134_a",calledOwner)
			||
			isMethod(calledDesc,"(DDDL"+RawSoundEventName+";L"+RawSoundCategoryName+";FFZ)V",calledName,RawWorldClassName,"playSound","func_184134_a",calledOwner)	){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onPlaySound", String.format( "(L%s;DDDL"+SoundEventName+";L"+SoundCategoryName+";FFZ)V", WorldClassName) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+TessellatorName+";L"+VertexBufferName+";L"+EntityClassName+";F)V",calledName,RenderGlobalName,"drawBlockDamageTexture","func_174981_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawTessellatorName+";L"+RawVertexBufferName+";L"+RawEntityClassName+";F)V",calledName,RawRenderGlobalName,"drawBlockDamageTexture","func_174981_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawBlockDamageTexture", String.format( "(L%s;L"+TessellatorName+";L"+VertexBufferName+";L"+EntityClassName+";F)V", RenderGlobalName) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+EntityPlayerName+";L"+RayTraceResult+";IF)V",calledName,RenderGlobalName,"drawSelectionBox","func_72731_b",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerName+";L"+RawRayTraceResult+";IF)V",calledName,RawRenderGlobalName,"drawSelectionBox","func_72731_b",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onDrawSelectionBox", String.format( "(L%s;L"+EntityPlayerName+";L"+RayTraceResult+";IF)V", RenderGlobalName) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(DDD)D",calledName,TileEntityName,"getDistanceSq","func_145835_a",calledOwner)
			||
			isMethod(calledDesc,"(DDD)D",calledName,RawTileEntityName,"getDistanceSq","func_145835_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onGetDistanceSq", String.format( "(L%s;DDD)D", TileEntityName) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+EntityClassName+";L"+ICameraName+";F)V",calledName,RenderGlobalName,"renderEntities","func_180446_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityClassName+";L"+RawICameraName+";F)V",calledName,RawRenderGlobalName,"renderEntities","func_180446_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderEntities", String.format( "(L%s;L"+EntityClassName+";L"+ICameraName+";F)V", RenderGlobalName) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+EntityClassName+";)Z",calledName,WorldClassName,"spawnEntityInWorld","func_72838_d",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityClassName+";)Z",calledName,RawWorldClassName,"spawnEntityInWorld","func_72838_d",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onSpawnEntityInWorld", String.format( "(L%s;L"+EntityClassName+";)Z", WorldClassName ) );
				return false;
		}
		
		if(isMethod(calledDesc,"(L"+BlockPosName+";L"+IBlockStateName+";)Z",calledName,WorldClientName,"invalidateRegionAndSetBlock","func_180503_b",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawBlockPosName+";L"+RawIBlockStateName+";)Z",calledName,RawWorldClientName,"invalidateRegionAndSetBlock","func_180503_b",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onInvalidateRegionAndSetBlock", String.format( "(L%s;L"+BlockPosName+";L"+IBlockStateName+";)Z", WorldClientName) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityPlayerName+";DDDDIL"+PacketName+";)V",calledName,PlayerListName,"sendToAllNearExcept","func_148543_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerName+";DDDDIL"+RawPacketName+";)V",calledName,RawPlayerListName,"sendToAllNearExcept","func_148543_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onSendToAllNearExcept", String.format( "(L%s;L"+EntityPlayerName+";DDDDIL"+PacketName+";)V", PlayerListName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+BlockPosName+";L"+IBlockStateName+";I)Z",calledName,WorldClassName,"setBlockState","func_180501_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawBlockPosName+";L"+RawIBlockStateName+";I)Z",calledName,RawWorldClassName,"setBlockState","func_180501_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onSetBlockState", String.format( "(L%s;L"+BlockPosName+";L"+IBlockStateName+";I)Z", WorldClassName ) );
				return false;
		}

		if(isMethod(calledDesc,"(IIIIII)V",calledName,WorldClassName,"markBlockRangeForRenderUpdate","func_147458_c",calledOwner)
			||
			isMethod(calledDesc,"(IIIIII)V",calledName,RawWorldClassName,"markBlockRangeForRenderUpdate","func_147458_c",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onMarkBlockRangeForRenderUpdate", String.format( "(L%s;IIIIII)V", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+Vec3dName+";L"+Vec3dName+";ZZZ)L"+RayTraceResult+";",calledName,WorldClassName,"rayTraceBlocks","func_147447_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawVec3dName+";L"+RawVec3dName+";ZZZ)L"+RawRayTraceResult+";",calledName,RawWorldClassName,"rayTraceBlocks","func_147447_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRayTraceBlocks", String.format( "(L%s;L"+Vec3dName+";L"+Vec3dName+";ZZZ)L"+RayTraceResult+";", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+GameProfileName+";)L",calledName,EntityPlayerMPName,"createPlayerForUser","func_148545_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawGameProfileName+";)L",calledName,RawEntityPlayerMPName,"createPlayerForUser","func_148545_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onCreatePlayerForUser", String.format( "(L%s;L"+GameProfileName+";)L"+EntityPlayerMPName+";", PlayerListName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityPlayerMPName+";IZ)L"+EntityPlayerMPName+";",calledName,PlayerListName,"recreatePlayerEntity","func_72368_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityPlayerMPName+";IZ)L"+RawEntityPlayerMPName+";",calledName,RawPlayerListName,"recreatePlayerEntity","func_72368_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRecreatePlayerEntity", String.format( "(L%s;L"+EntityPlayerMPName+";IZ)L"+EntityPlayerMPName+";", PlayerListName ) );
				return false;
		}

		//TBA
		if(isMethod(calledDesc,"(L"+EntityClassName+";)V",calledName,WorldClassName,"onEntityRemoved","func_72847_b",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityClassName+";)V",calledName,RawWorldClassName,"onEntityRemoved","func_72847_b",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onEntityRemoved", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+EntityClassName+";)V",calledName,WorldClassName,"onEntityAdded","func_72923_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawEntityClassName+";)V",calledName,RawWorldClassName,"onEntityAdded","func_72923_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onEntityAdded", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}

		if(isMethod(calledDesc,"(L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I",calledName,RenderGlobalName,"renderBlockLayer","func_174977_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawBlockRenderLayerName+";DIL"+RawEntityClassName+";)I",calledName,RawRenderGlobalName,"renderBlockLayer","func_174977_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderBlockLayer", String.format( "(L%s;L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I", RenderGlobalName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(L"+ChunkName+";)V",calledName,ChunkProviderServerName,"unload","func_189549_a",calledOwner)
			||
			isMethod(calledDesc,"(L"+RawChunkName+";)V",calledName,RawChunkProviderServerName,"unload","func_189549_a",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onChunkUnload", String.format( "(L%s;L"+ChunkName+";)V", ChunkProviderServerName ) );
				return false;
		}
		
		//TBA
		if(isMethod(calledDesc,"(DDD)V",calledName,EntityClassName,"moveEntity","func_70091_d",calledOwner)
			||
			isMethod(calledDesc,"(DDD)V",calledName,RawEntityClassName,"moveEntity","func_70091_d",calledOwner)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onEntityMove", String.format( "(L%s;DDD)V", EntityClassName ) );
				return false;
		}

		return true;
	}
	
	private boolean isMethod(String calledDesc,String methodDesc,String calledName,String classFrom,String methodNameClear,String methodNameObsf,String calledOwner){
		correctDesc = calledDesc.equals(methodDesc);
		if(correctDesc){
			correctName = pertainsToMethod(calledName,classFrom,methodNameClear,methodNameObsf)||pertainsToMethod(calledOwner,classFrom,methodNameClear,methodNameObsf);
			if(correctName){
				correctSuperClass = InheritanceUtils.extendsClass(calledOwner, classFrom);
				return correctSuperClass;
			}
		}
		return false;
	}
	
	private boolean pertainsToMethod(String calledName,String classOwningMethod,String deobsfName,String obsfName){
		if(!m_isObfuscatedEnvironment ){
			return calledName.equals(deobsfName);
		}
		String runtimeName = getRuntimeMethodName(classOwningMethod,deobsfName,obsfName);
		return calledName.equals(runtimeName)||calledName.equals(deobsfName)||calledName.equals(obsfName);
	}

	protected String getRuntimeClassName( String clearClassName ){
		if(m_isObfuscatedEnvironment ){
			return getObfuscatedClassName( clearClassName );
		}else{
			return clearClassName;
		}
	}

	protected String getRuntimeMethodName( String runtimeClassName, String clearMethodName, String idMethodName ){
		if(m_isObfuscatedEnvironment ){
//			return idMethodName;
			return methodMapReverseLookup( getMethodMap( runtimeClassName ), idMethodName );
		}else{
			return clearMethodName;
		}
	}

	private String getObfuscatedClassName( String clearClassName ){
		String obfuscatedClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap( clearClassName );
		if( obfuscatedClassName == null ){
			return clearClassName;
		}
		return obfuscatedClassName;
	}

	public void setCV(ClassWriter writer){
		cv = writer;
	}

	@SuppressWarnings( "unchecked" )
	private Map<String,String> getMethodMap( String obfuscatedClassName ){
		try{
			Field field = FMLDeobfuscatingRemapper.class.getDeclaredField( "methodNameMaps" );
			field.setAccessible( true );
			return ((Map<String,Map<String,String>>)field.get( FMLDeobfuscatingRemapper.INSTANCE )).get( obfuscatedClassName );
		}catch( Exception ex ){
			throw new Error( "Unable to access FML's deobfuscation mappings!", ex );
		}
	}

	private String methodMapReverseLookup( Map<String,String> methodMap, String idMethodName ){
		// did we not get a method map? just pass through the method name
		if( methodMap == null ){
			return idMethodName;
		}
		// methodNameMaps = Map<obfuscated class name,Map<obfuscated method name + signature,id method name>>
		for( Map.Entry<String,String> entry : methodMap.entrySet() ){
			if( entry.getValue().equals( idMethodName ) ){
				String obfuscatedName = entry.getKey();
				// chop off the signature
				// ie, turn "a(III)V" into just "a"
				return obfuscatedName.substring( 0, obfuscatedName.indexOf( "(" ) );
			}
		}
		// no method was found
		// return empty string so it fails comparisons with expected values, but doesn't throw exceptions
		return "";
	}
	

	@Override
	public void visit( int version, int access, String name, String signature, String superName, String[] interfaces ){
		m_className = name;
		super.visit( version, access, name, signature, superName, interfaces );
	}

	@Override
	public MethodVisitor visitMethod( int access, final String methodName, String methodDesc, String signature, String[] exceptions ){
		return new MethodVisitor(api, cv.visitMethod( access, methodName, methodDesc, signature, exceptions)){
			@Override
			public void visitMethodInsn( int opcode, String calledOwner, String calledName, String calledDesc ){
				if( opcode == Opcodes.INVOKEVIRTUAL){
					if(runTransformer(calledName,calledDesc,calledOwner,mv)){
						super.visitMethodInsn( opcode, calledOwner, calledName, calledDesc );
					}					
				}else{
					super.visitMethodInsn( opcode, calledOwner, calledName, calledDesc );
				}
			}
		};
	}


}