package ValkyrienWarfareBase.Mixin;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

/**
 * Makes a shitton of variables public
 *
 * @author thebest108
 */
public class ValkyrienWarfareAccessTransformer extends AccessTransformer {
    public ValkyrienWarfareAccessTransformer() throws IOException {
        super("ValkyrienWarfare_At.cfg");
    }
}