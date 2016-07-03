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
	private final String EntityClassName;
	private final String WorldClassName;
	private final String ExplosionClassName;
	private final String BlockPosClassName;
	private final String EnumSkyBlockName;
	private final String WorldClientName;
	private final String EntityLivingBaseName;
//	private final String progressManager;
//	private final String progressBar;
	private final String PacketName;
	private final String EntityPlayerName;
	private final String EntityRendererClassName;
	private final String ServerConfigurationManagerName;
	private String m_className;
	public boolean m_isObfuscatedEnvironment;

	private final String ChunkRenderContainerName;
	private final String RenderChunkName;
	private final String RenderGlobalName;
	private final String ICameraName;
	private final String BlockRenderLayerName;
	private final String WorldProviderName;
	private final String ChunkProviderServerName;
	private final String EntityRendererName;
	private final String PlayerListName;
	private final String GameProfileName;
	private final String EntityPlayerMPName;
	private final String NetHandlerPlayClientName;
	private final String SPacketJoinGameName;
	private final String INetHandlerPlayClientName;
	private final String ChunkName;
	private final String MinecraftName;
	private final String RayTraceResult;
	private final String Vec3dName;
	private final String IBlockStateName;
	private final String BlockPosName;
	
	public TransformAdapter( int api, boolean isObfuscatedEnvironment ){
		super( api, null );
		m_isObfuscatedEnvironment = isObfuscatedEnvironment;
		m_className = null;
		EntityClassName = getRuntimeClassName("net/minecraft/entity/Entity");
		WorldClassName = getRuntimeClassName("net/minecraft/world/World");
		ExplosionClassName = getRuntimeClassName("net/minecraft/world/Explosion");
		BlockPosClassName = getRuntimeClassName("net/minecraft/util/BlockPos");
		WorldClientName = getRuntimeClassName("net/minecraft/client/multiplayer/WorldClient");
		EntityLivingBaseName = getRuntimeClassName("net/minecraft/entity/EntityLivingBase");
		EnumSkyBlockName = getRuntimeClassName("net/minecraft/world/EnumSkyBlock");
		EntityRendererClassName = getRuntimeClassName("net/minecraft/client/renderer/EntityRenderer");
		PacketName = getRuntimeClassName("net/minecraft/network/Packet");
		EntityPlayerName = getRuntimeClassName("net/minecraft/entity/player/EntityPlayer");
		ServerConfigurationManagerName = getRuntimeClassName("net/minecraft/server/management/ServerConfigurationManager");
		ChunkRenderContainerName = getRuntimeClassName("net/minecraft/client/renderer/ChunkRenderContainer");
		RenderChunkName = getRuntimeClassName("net/minecraft/client/renderer/chunk/RenderChunk");
		RenderGlobalName = getRuntimeClassName("net/minecraft/client/renderer/RenderGlobal");
		ICameraName = getRuntimeClassName("net/minecraft/client/renderer/culling/ICamera");
		BlockRenderLayerName = getRuntimeClassName("net/minecraft/util/BlockRenderLayer");
		WorldProviderName = getRuntimeClassName("net/minecraft/world/WorldProvider");
		ChunkProviderServerName = getRuntimeClassName("net/minecraft/world/gen/ChunkProviderServer");
		EntityRendererName = getRuntimeClassName("net/minecraft/client/renderer/EntityRenderer");
		
		PlayerListName = getRuntimeClassName("net/minecraft/server/management/PlayerList");
		GameProfileName = getRuntimeClassName("com/mojang/authlib/GameProfile");
		EntityPlayerMPName = getRuntimeClassName("net/minecraft/entity/player/EntityPlayerMP");
		NetHandlerPlayClientName = getRuntimeClassName("net/minecraft/client/network/NetHandlerPlayClient");
		SPacketJoinGameName = getRuntimeClassName("net/minecraft/network/play/server/SPacketJoinGame");
		INetHandlerPlayClientName = getRuntimeClassName("net/minecraft/network/play/INetHandlerPlayClient");
		ChunkName = getRuntimeClassName("net/minecraft/world/chunk/Chunk");
		MinecraftName = getRuntimeClassName("net/minecraft/client/Minecraft");
		
		RayTraceResult = getRuntimeClassName("net/minecraft/util/math/RayTraceResult");
		Vec3dName = getRuntimeClassName("net/minecraft/util/math/Vec3d");
		IBlockStateName = getRuntimeClassName("net/minecraft/block/state/IBlockState");
		BlockPosName = getRuntimeClassName("net/minecraft/util/math/BlockPos");
	}

	private boolean runTransformer(String calledName,String calledDesc,String calledOwner,MethodVisitor mv){
		if(calledDesc.equals("(L"+BlockPosName+";L"+IBlockStateName+";I)Z")
			&& calledName.equals( getRuntimeMethodName( calledOwner, "setBlockState", "RENAMEME" ) )
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onSetBlockState", String.format( "(L%s;L"+BlockPosName+";L"+IBlockStateName+";I)Z", WorldClassName ) );
				return false;
		}

		if(calledDesc.equals("(IIIIII)V")
			&& calledName.equals( getRuntimeMethodName( calledOwner, "markBlockRangeForRenderUpdate", "func_147458_c" ) )
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onMarkBlockRangeForRenderUpdate", String.format( "(L%s;IIIIII)V", WorldClassName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+Vec3dName+";L"+Vec3dName+";ZZZ)L"+RayTraceResult+";")
			&& calledName.equals(getRuntimeMethodName(calledOwner,"rayTraceBlocks","func_147447_a"))
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onRayTraceBlocks", String.format( "(L%s;L"+Vec3dName+";L"+Vec3dName+";ZZZ)L"+RayTraceResult+";", WorldClassName ) );
				return false;
		}
		
		//Method that creates a playerInteractionManager
		if(calledDesc.equals("(L"+GameProfileName+";)L"+EntityPlayerMPName+";")
			&& calledName.equals(getRuntimeMethodName(calledOwner,"createPlayerForUser","func_148545_a"))
			&& (InheritanceUtils.extendsClass( calledOwner, PlayerListName)||InheritanceUtils.extendsClass( m_className, PlayerListName))){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onCreatePlayerForUser", String.format( "(L%s;L"+GameProfileName+";)L"+EntityPlayerMPName+";", PlayerListName ) );
				return false;
		}
		//Method that creates a playerInteractionManager
		if(calledDesc.equals("(L"+EntityPlayerMPName+";IZ)L"+EntityPlayerMPName+";")
			&& calledName.equals(getRuntimeMethodName(calledOwner,"recreatePlayerEntity","func_72368_a"))
			&& InheritanceUtils.extendsClass( calledOwner, PlayerListName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onRecreatePlayerEntity", String.format( "(L%s;L"+EntityPlayerMPName+";IZ)L"+EntityPlayerMPName+";", PlayerListName ) );
				return false;
		}

		if(calledDesc.equals("(L"+EntityClassName+";)V")
			&& calledName.equals(getRuntimeMethodName(calledOwner,"onEntityRemoved","func_72847_b"))
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityRemoved", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}
		if(calledDesc.equals("(L"+EntityClassName+";)V")
			&& calledName.equals(getRuntimeMethodName(calledOwner,"onEntityAdded","func_72923_a"))
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityAdded", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I")
			&& ( calledName.equals(getRuntimeMethodName(calledOwner,"renderBlockLayer","func_174977_a")) || calledName.equals(getRuntimeMethodName(calledOwner,"renderBlockLayer","func_174977_a")) )
			&& InheritanceUtils.extendsClass( calledOwner, RenderGlobalName)){
			mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderBlockLayer", String.format( "(L%s;L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I", RenderGlobalName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+ChunkName+";)V")
			&& calledName.equals( getRuntimeMethodName( calledOwner, "unload", "func_189549_a" ) )
			&& InheritanceUtils.extendsClass( calledOwner, ChunkProviderServerName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onChunkUnload", String.format( "(L%s;L"+ChunkName+";)V", ChunkProviderServerName ) );
				return false;
		}
		
		if(calledDesc.equals("(DDD)V")
			&& calledName.equals( getRuntimeMethodName( calledOwner, "moveEntity", "func_70091_d" ) )
			&& InheritanceUtils.extendsClass( calledOwner, EntityClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityMove", String.format( "(L%s;DDD)V", EntityClassName ) );
				return false;
		}

		return true;
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
		super.visit( version, access, name, signature, superName, interfaces );
		m_className = name;
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