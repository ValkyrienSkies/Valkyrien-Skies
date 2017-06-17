package ValkyrienWarfareBase.Mixin.util;

import net.minecraft.util.HttpUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;
import java.net.ServerSocket;

@Mixin(HttpUtil.class)
public abstract class MixinHttpUtil {
    @Overwrite
    public int getSuitableLanPort() throws IOException {
        ServerSocket serversocket = null;
        int i = -1;

        try
        {
            serversocket = new ServerSocket(80);
            i = serversocket.getLocalPort();
        }
        finally
        {
            try
            {
                if (serversocket != null)
                {
                    serversocket.close();
                }
            }
            catch (IOException var8)
            {
                ;
            }
        }

        return i;
    }
}
