package ValkyrienWarfareBase.CoreMod;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * Basically handles all the byte transforms
 * @author thebest108
 *
 */
public class TransformAdapter extends ClassVisitor{
	private String m_className;
	public boolean m_isObfuscatedEnvironment;
	
	private final String ParticleName,RawParticleName;
	private final String ParticleManagerName,RawParticleManagerName;
	private final String SoundEventName,RawSoundEventName;
	private final String SoundCategoryName,RawSoundCategoryName;
	private final String WorldClassName,RawWorldClassName;
	private final String RenderGlobalName,RawRenderGlobalName;
	private final String EntityClassName,RawEntityClassName;
	private final String VertexBufferName,RawVertexBufferName;
	private final String TessellatorName,RawTessellatorName;
	private final String EntityPlayerName,RawEntityPlayerName;
	private final String RayTraceResult,RawRayTraceResult;
	private final String TileEntityName,RawTileEntityName;
	private final String ICameraName,RawICameraName;
	private final String IBlockStateName,RawIBlockStateName;
	private final String BlockPosName,RawBlockPosName;
	private final String WorldClientName,RawWorldClientName;
	private final String PlayerListName,RawPlayerListName;
	private final String PacketName,RawPacketName;
	private final String Vec3dName,RawVec3dName;
	private final String GameProfileName,RawGameProfileName;
	private final String EntityPlayerMPName,RawEntityPlayerMPName;
	private final String BlockRenderLayerName,RawBlockRenderLayerName;
	private final String ChunkName,RawChunkName;
	private final String ChunkProviderServerName,RawChunkProviderServerName;
	
	private boolean correctDesc,correctName,correctSuperClass;

	public TransformAdapter( int api, boolean isObfuscatedEnvironment ){
		super( api, null );
		m_isObfuscatedEnvironment = isObfuscatedEnvironment;
		m_className = null;
		
		
		
		RawEntityClassName = "net/minecraft/entity/Entity";
		RawWorldClassName = "net/minecraft/world/World";
		RawWorldClientName = "net/minecraft/client/multiplayer/WorldClient";
		RawPacketName = "net/minecraft/network/Packet";
		RawEntityPlayerName = "net/minecraft/entity/player/EntityPlayer";
		RawRenderGlobalName = "net/minecraft/client/renderer/RenderGlobal";
		RawICameraName = "net/minecraft/client/renderer/culling/ICamera";
		RawBlockRenderLayerName = "net/minecraft/util/BlockRenderLayer";
		RawChunkProviderServerName = "net/minecraft/world/gen/ChunkProviderServer";
		RawPlayerListName = "net/minecraft/server/management/PlayerList";
		RawGameProfileName = "com/mojang/authlib/GameProfile";
		RawEntityPlayerMPName = "net/minecraft/entity/player/EntityPlayerMP";
		RawChunkName = "net/minecraft/world/chunk/Chunk";
		RawRayTraceResult = "net/minecraft/util/math/RayTraceResult";
		RawVec3dName = "net/minecraft/util/math/Vec3d";
		RawIBlockStateName = "net/minecraft/block/state/IBlockState";
		RawBlockPosName = "net/minecraft/util/math/BlockPos";
		RawTileEntityName = "net/minecraft/tileentity/TileEntity";
		RawTessellatorName = "net/minecraft/client/renderer/Tessellator";
		RawVertexBufferName = "net/minecraft/client/renderer/VertexBuffer";
		RawSoundEventName = "net/minecraft/util/SoundEvent";
		RawSoundCategoryName = "net/minecraft/util/SoundCategory";
		RawParticleName = "net/minecraft/client/particle/Particle";
		RawParticleManagerName = "net/minecraft/client/particle/ParticleManager";
		
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
	}
	
	private void getClassNames(){
		
	}

	private boolean runTransformer(String calledName,String calledDesc,String calledOwner,MethodVisitor mv){
		
//		Logger log = FMLLog.getLogger();
//		log.debug("Name: "+calledName);
//		log.debug("Desc: "+calledDesc);
//		log.debug("Ownr: "+calledOwner);
//		
//		log.debug("ClassName: "+EntityPlayerName);
		
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
			isMethod(calledDesc,"(L"+RawBlockPosName+";L"+RawIBlockStateName+";)Z",calledName,RawWorldClientName,"invalidateRegionAndSetBlock","func_180503_b",calledOwner)
			){
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
			correctName = pertainsToMethod(calledName,classFrom,methodNameClear,methodNameObsf);
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
			obfuscatedClassName = clearClassName;
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