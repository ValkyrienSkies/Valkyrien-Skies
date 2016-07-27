package ValkyrienWarfareBase.CoreMod.CompiledHack;

import java.lang.reflect.Field;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ValkyrienWarfareBase.CoreMod.InheritanceUtils;
import ValkyrienWarfareBase.CoreMod.TransformAdapter;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/**
 * Basically handles all the byte transforms
 * @author thebest108
 *
 */
public class TransformAdapterHack extends TransformAdapter{

	public TransformAdapterHack(int api, boolean isObfuscatedEnvironment) {
		super(api, isObfuscatedEnvironment);
	}

	@Override
	protected String getRuntimeClassName( String clearClassName ){
		return clearClassName;
	}

}