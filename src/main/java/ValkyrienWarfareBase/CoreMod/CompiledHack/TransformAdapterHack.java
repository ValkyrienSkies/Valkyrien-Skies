package ValkyrienWarfareBase.CoreMod.CompiledHack;

import ValkyrienWarfareBase.CoreMod.TransformAdapter;

/**
 * Same thing, but works specifically with other mods
 * 
 * @author thebest108
 *
 */
public class TransformAdapterHack extends TransformAdapter {

	public TransformAdapterHack(int api, boolean isObfuscatedEnvironment) {
		super(api, isObfuscatedEnvironment);
	}

	@Override
	protected String getRuntimeClassName(String clearClassName) {
		return clearClassName;
	}

}