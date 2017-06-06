package ValkyrienWarfareBase.Mixin.world;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.CallRunner;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow
    protected List<IWorldEventListener> eventListeners;

    @Shadow
    public final boolean isRemote;

    {
        isRemote = false;
        //dirty hack lol
    }

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

    @Inject(method = "getPrecipitationHeight(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;", at = @At("HEAD"), cancellable = true)
    public void preGetPrecipitationHeight(BlockPos pos, CallbackInfoReturnable callbackInfo)  {
        if (this.isRemote && ValkyrienWarfareMod.accurateRain)   {
            BlockPos accuratePos = MixinWorld.onGetPrecipitationHeightClient(World.class.cast(this), pos);
            callbackInfo.setReturnValue(accuratePos);
            callbackInfo.cancel(); //return the injected value, preventing vanilla code from running
        }
        //if vw didn't change the pos, run the vanilla code
    }

    public static BlockPos onGetPrecipitationHeightClient(World world, BlockPos posToCheck) {
        BlockPos pos = world.getPrecipitationHeight(posToCheck);
        // Servers shouldn't bother running this code

        Vector traceStart = new Vector(pos.getX() + .5D, Minecraft.getMinecraft().player.posY + 50D, pos.getZ() + .5D);
        Vector traceEnd = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);

//		System.out.println(traceStart);
//		System.out.println(traceEnd);

        RayTraceResult result = CallRunner.onRayTraceBlocks(world, traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

        if(result != null && result.typeOfHit != RayTraceResult.Type.MISS && result.getBlockPos() != null){

            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, result.getBlockPos());
            if(wrapper != null){
//				System.out.println("test");
                Vector blockPosVector = new Vector(result.getBlockPos().getX() + .5D, result.getBlockPos().getY() + .5D, result.getBlockPos().getZ() + .5D);
                wrapper.wrapping.coordTransform.fromLocalToGlobal(blockPosVector);
                BlockPos toReturn = new BlockPos(pos.getX(), blockPosVector.Y + .5D, pos.getZ());
                return toReturn;
            }
        }

        return pos;
    }
}
