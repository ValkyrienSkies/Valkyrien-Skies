package org.valkyrienskies.mixin.client.renderer.tileentity;

import java.util.Optional;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcher {

    @Shadow(remap = false)
    private boolean drawingBatch;
    private boolean hasChanged = false;

    @Shadow(remap = false)
    public void drawBatch(int pass) {
    }

    @Shadow(remap = false)
    public void preDrawBatch() {
    }

    @Shadow
    public abstract void render(TileEntity tileentityIn, float partialTicks, int destroyStage);

    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V",
        at = @At("HEAD"),
        cancellable = true)
    private void preRender(TileEntity tileentityIn, float partialTicks, int destroyStage,
        CallbackInfo callbackInfo) {
        if (!hasChanged) {
            BlockPos pos = tileentityIn.getPos();
            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(tileentityIn.getWorld(), tileentityIn.getPos());

            if (physicsObject.isPresent()) {
                try {
                    GlStateManager.resetColor();

                    if (drawingBatch) {
                        this.drawBatch(MinecraftForgeClient.getRenderPass());
                        this.preDrawBatch();
                    }

                    physicsObject.get()
                        .getShipRenderer()
                        .applyRenderTransform(partialTicks);

                    double playerX = TileEntityRendererDispatcher.staticPlayerX;
                    double playerY = TileEntityRendererDispatcher.staticPlayerY;
                    double playerZ = TileEntityRendererDispatcher.staticPlayerZ;

                    TileEntityRendererDispatcher.staticPlayerX = physicsObject.get()
                        .getShipRenderer().offsetPos.getX();
                    TileEntityRendererDispatcher.staticPlayerY = physicsObject.get()
                        .getShipRenderer().offsetPos.getY();
                    TileEntityRendererDispatcher.staticPlayerZ = physicsObject.get()
                        .getShipRenderer().offsetPos.getZ();

                    hasChanged = true;
                    if (drawingBatch) {
                        this.render(tileentityIn, partialTicks, destroyStage);
                        this.drawBatch(MinecraftForgeClient.getRenderPass());
                        this.preDrawBatch();
                    } else {
                        this.render(tileentityIn, partialTicks, destroyStage);
                    }
                    hasChanged = false;
                    TileEntityRendererDispatcher.staticPlayerX = playerX;
                    TileEntityRendererDispatcher.staticPlayerY = playerY;
                    TileEntityRendererDispatcher.staticPlayerZ = playerZ;

                    physicsObject.get()
                        .getShipRenderer()
                        .inverseTransform(partialTicks);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callbackInfo.cancel();
            }
        }
    }
}
