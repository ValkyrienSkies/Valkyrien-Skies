package valkyrienwarfare.fixes;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.util.IThreadListener;

public class SpongeStrangenessFixes {

    
    /**
     * Immune to the evil of Sponge!
     * @param packetIn
     * @param processor
     * @param scheduler
     * @throws ThreadQuickExitException
     */
    public static <T extends INetHandler> void checkThreadAndEnqueue_SpongeFree(final Packet<T> packetIn, final T processor, IThreadListener scheduler) throws ThreadQuickExitException
    {
        if (!scheduler.isCallingFromMinecraftThread())
        {
            scheduler.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    packetIn.processPacket(processor);
                }
            });
            throw ThreadQuickExitException.INSTANCE;
        }
    }
}
