package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld implements IBlockAccess, net.minecraftforge.common.capabilities.ICapabilityProvider {

    @Shadow
    protected List<IWorldEventListener> eventListeners;

    @Overwrite
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2){
//		System.out.println((x2-x1)*(y2-y1)*(z2-z1));
//		System.out.println(x1+":"+x2+":"+y1+":"+y2+":"+z1+":"+z2);

        //Stupid OpenComputers fix, blame those assholes
        if(x2 == 1 && y1 == 0 && z2 == 1){
            x2 = x1 + 1;
            x1--;

            y1 = y2 - 1;
            y2++;

            z2 = z1 + 1;
            z2--;
        }

        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        int midZ = (z1 + z2) / 2;
        BlockPos newPos = new BlockPos(midX, midY, midZ);
        //.....................................................................................Don't mind this ugly fix, the mixin technically isn't a World so i need this
        //TODO: test ugly fix
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), newPos);
        if (wrapper != null && wrapper.wrapping.renderer != null) {
            wrapper.wrapping.renderer.updateRange(x1-1, y1-1, z1-1, x2+1, y2+1, z2+1);
        }

        if(wrapper == null){
            this.markBlockRangeForRenderUpdateOriginal(x1, y1, z1, x2, y2, z2);
        }
    }

    public void markBlockRangeForRenderUpdateOriginal(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        for (int i = 0; i < this.eventListeners.size(); ++i)
        {
            ((IWorldEventListener)this.eventListeners.get(i)).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
        }
    }
}
