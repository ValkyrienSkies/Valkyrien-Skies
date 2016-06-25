package ValkyrienWarfareBase.CoreMod;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
//		progressManager = getRuntimeClassName("net/minecraftforge/fml/common/ProgressManager");
//		progressBar = getRuntimeClassName("net/minecraftforge/fml/common/ProgressManager$ProgressBar");
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

	private boolean runTransformer(String calledName,String calledDesc,String calledOwner,MethodVisitor mv){
		if(calledDesc.equals("(L"+EntityClassName+";)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"onEntityRemoved","func_72847_b"))
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityRemoved", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}
		if(calledDesc.equals("(L"+EntityClassName+";)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"onEntityAdded","func_72923_a"))
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityAdded", String.format( "(L%s;L"+EntityClassName+";)V", WorldClassName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I")
			&& calledName.equals(getRuntimeMethodName(m_className,"renderBlockLayer","func_174977_a"))
			&& InheritanceUtils.extendsClass( calledOwner, RenderGlobalName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onRenderBlockLayer", String.format( "(L%s;L"+BlockRenderLayerName+";DIL"+EntityClassName+";)I", RenderGlobalName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+RenderChunkName+";)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"preRenderChunk","func_178003_a"))
			&& InheritanceUtils.extendsClass( calledOwner, ChunkRenderContainerName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onPreRenderChunk", String.format( "(L%s;L"+RenderChunkName+";)V", ChunkRenderContainerName ) );
				return false;
		}
		
		if(calledDesc.equals("(L"+EntityClassName+";DL"+ICameraName+";IZ)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"setupTerrain","func_174970_a"))
			&& InheritanceUtils.extendsClass( calledOwner, RenderGlobalName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathClient, "onSetupTerrain", String.format( "(L%s;L"+EntityClassName+";DL"+ICameraName+";IZ)V", RenderGlobalName ) );
				return false;
		}
		
		
		if(calledDesc.equals("(II)V")
			&& calledName.equals( getRuntimeMethodName( m_className, "dropChunk", "func_73241_b" ) )
			&& InheritanceUtils.extendsClass( calledOwner, ChunkProviderServerName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onDropChunk", String.format( "(L%s;II)V", ChunkProviderServerName ) );
				return false;
		}
		
		
		if(calledDesc.equals("(DDD)V")
			&& calledName.equals( getRuntimeMethodName( m_className, "moveEntity", "func_70091_d" ) )
			&& InheritanceUtils.extendsClass( calledOwner, EntityClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.PathCommon, "onEntityMove", String.format( "(L%s;DDD)V", EntityClassName ) );
				return false;
		}
		/*if(calledDesc.equals("(L"+EntityPlayerName+";DDDDIL"+PacketName+";)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"sendToAllNearExcept","func_148543_a"))
			&& InheritanceUtils.extendsClass( calledOwner, ServerConfigurationManagerName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "onSendToAllNearExcept", String.format( "(L%s;L"+EntityPlayerName+";DDDDIL"+PacketName+";)V", ServerConfigurationManagerName ) );
				return false;
		}*/
		/*if(calledDesc.equals("(IFJ)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"renderWorldPass","func_175068_a"))
			&& InheritanceUtils.extendsClass( calledOwner, EntityRendererClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "onRenderWorldPass", String.format( "(L%s;IFJ)V", EntityRendererClassName ) );
				return false;
		}
		if(calledDesc.equals("(F)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"getMouseOver","func_78473_a"))
			&& InheritanceUtils.extendsClass( calledOwner, EntityRendererClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "onGetMouseOver", String.format( "(L%s;F)V", EntityRendererClassName ) );
				return false;
		}
		if(calledDesc.equals("(F)V")
			&& calledName.equals(getRuntimeMethodName(m_className,"orientCamera","func_78467_g"))
			&& InheritanceUtils.extendsClass( calledOwner, EntityRendererClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "onOrientCamera", String.format( "(L%s;F)V", EntityRendererClassName ) );
				return false;
		}*/
		/*if(calledDesc.equals("(DDD)V")
			&& calledName.equals( getRuntimeMethodName( m_className, "moveEntity", "func_70091_d" ) )
			&& InheritanceUtils.extendsClass( calledOwner, EntityClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "onEntityMove", String.format( "(L%s;DDD)V", EntityClassName ) );
				return false;
		}
		if(calledDesc.equals("()Z")
				&& calledName.equals( getRuntimeMethodName( EntityLivingBaseName, "isOnLadder", "func_70617_f_" ) )
				&& InheritanceUtils.extendsClass( calledOwner, EntityClassName)){
					mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "isEntityOnLadder", String.format("(L%s;)Z", EntityLivingBaseName ) );
					return false;
			}
		if(calledDesc.equals("()V")
			&& calledName.equals( getRuntimeMethodName( EntityClassName, "onUpdate", "func_70071_h_" ) )
			&& InheritanceUtils.extendsClass( calledOwner, EntityClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "onEntityUpdate", String.format( "(L%s;)V", EntityClassName ) );
				return false;
		}
		if(calledDesc.equals("(L"+BlockPosClassName+";I)I")
			&& calledName.equals( getRuntimeMethodName( WorldClassName, "getCombinedLight", "func_175626_b" ) )
			&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "onGetCombinedLight", String.format( "(L%s;L"+BlockPosClassName+";I)I", WorldClassName ) );
				return false;
		}
		if(calledDesc.equals("()V")
			&& calledName.equals(getRuntimeMethodName(ExplosionClassName,"doExplosionA","func_77278_a"))
			&& InheritanceUtils.extendsClass( calledOwner, ExplosionClassName)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrienWarfarePlugin.Path, "onExplosionA", String.format("(L%s;)V", ExplosionClassName ) );
				return false;	
		}

		/*if(calledDesc.equals("()Ljava/lang/String;")
			&& calledName.equals("getTitle")
			&& InheritanceUtils.extendsClass( calledOwner, progressBar)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "getProgressTitle", String.format( "(L%s;)Ljava/lang/String;", progressBar ) );
				return false;
		}
		if(calledDesc.equals("()Ljava/lang/String;")
			&& calledName.equals("getMessage")
			&& InheritanceUtils.extendsClass( calledOwner, progressBar)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "getProgressMessage", String.format( "(L%s;)Ljava/lang/String;", progressBar ) );
				return false;
		}
		if(calledDesc.equals("()I")
			&& calledName.equals("getStep")
			&& InheritanceUtils.extendsClass( calledOwner, progressBar)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "getProgressStep", String.format( "(L%s;)I", progressBar ) );
				return false;
		}
		if(calledDesc.equals("()I")
			&& calledName.equals("getSteps")
			&& InheritanceUtils.extendsClass( calledOwner, progressBar)){
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "getProgressSteps", String.format( "(L%s;)I", progressBar ) );
				return false;
		}*/
		
		
		
		/*if(calledDesc.equals("(L"+EnumSkyBlockName+";L"+BlockPosClassName+";)Z")
				&& calledName.equals( getRuntimeMethodName( WorldClassName, "checkLightFor", "func_180500_c"))
				&& InheritanceUtils.extendsClass( calledOwner, WorldClassName)){
					mv.visitMethodInsn( Opcodes.INVOKESTATIC, ValkyrianWarfarePlugin.Path, "onCheckLight", String.format( "(L%s;L"+EnumSkyBlockName+";L"+BlockPosClassName+";)Z", WorldClassName ) );
					return false;
			}*/
		return true;
	}

	protected String getRuntimeClassName( String clearClassName ){
		if( m_isObfuscatedEnvironment ){
			return getObfuscatedClassName( clearClassName );
		}else{
			return clearClassName;
		}
	}

	protected String getRuntimeMethodName( String runtimeClassName, String clearMethodName, String idMethodName ){
		if( m_isObfuscatedEnvironment ){
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

}