package ValkyrienWarfareBase.Mixin.client.multiplayer;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {

    @Shadow
    @Final
    private final NetHandlerPlayClient connection;
    @Shadow
    protected Set<ChunkPos> viewableChunks;
    ;
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private int ambienceTicks;

    public MixinWorldClient(NetHandlerPlayClient netHandler, WorldSettings settings, int dimension, EnumDifficulty difficulty, Profiler profilerIn) {
        super(new SaveHandlerMP(), new WorldInfo(settings, "MpServer"), net.minecraftforge.common.DimensionManager.createProviderFor(dimension), profilerIn, true);
        this.ambienceTicks = this.rand.nextInt(12000);
        this.viewableChunks = Sets.<ChunkPos>newHashSet();
        this.connection = netHandler;
        this.getWorldInfo().setDifficulty(difficulty);
        this.provider.setWorld(this);
        this.setSpawnPoint(new BlockPos(8, 64, 8)); //Forge: Moved below registerWorld to prevent NPE in our redirect.
        this.chunkProvider = this.createChunkProvider();
        this.mapStorage = new SaveDataMemoryStorage();
        this.calculateInitialSkylight();
        this.calculateInitialWeather();
        this.initCapabilities();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(this));
    }

    @Overwrite
    public void doVoidFogParticles(int posX, int posY, int posZ) {
        if (ValkyrienWarfareMod.shipsSpawnParticles) {
            int range = 15;
            AxisAlignedBB aabb = new AxisAlignedBB(posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range);
            List<PhysicsWrapperEntity> physEntities = ValkyrienWarfareMod.physicsManager.getManagerForWorld(WorldClient.class.cast(this)).getNearbyPhysObjects(aabb);
            for (PhysicsWrapperEntity wrapper : physEntities) {
                Vector playPosInShip = new Vector(posX + .5D, posY + .5D, posZ + .5D);
                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, playPosInShip);
                this.doVoidFogParticlesOriginal(MathHelper.floor(playPosInShip.X), MathHelper.floor(playPosInShip.Y), MathHelper.floor(playPosInShip.Z));
            }
        }
        this.doVoidFogParticlesOriginal(posX, posY, posZ);
    }

    @Shadow
    public abstract void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos);

    public void doVoidFogParticlesOriginal(int posX, int posY, int posZ) {
        int i = 32;
        Random random = new Random();
        ItemStack itemstack = this.mc.player.getHeldItemMainhand();
        boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j) {
            this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }
}
