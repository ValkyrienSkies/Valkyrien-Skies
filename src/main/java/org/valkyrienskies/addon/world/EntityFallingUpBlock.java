package org.valkyrienskies.addon.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Like a regular falling block, but it falls up.
 *
 * @author thebest108
 */
public class EntityFallingUpBlock extends EntityFallingBlock {

    public EntityFallingUpBlock(World worldIn) {
        super(worldIn);
        fallTile = ValkyrienSkiesWorld.INSTANCE.valkyriumOre.getDefaultState();
    }

    public EntityFallingUpBlock(World worldIn, double x, double y, double z,
        IBlockState fallingBlockState) {
        super(worldIn, x, y, z, fallingBlockState);
    }

    @Override
    public void onUpdate() {
        Block block = this.fallTile.getBlock();

        if (this.fallTile.getMaterial() == Material.AIR) {
            this.setDead();
        } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.fallTime++ == 0) {
                BlockPos blockpos = new BlockPos(this);

                if (this.world.getBlockState(blockpos).getBlock() == block) {
                    this.world.setBlockToAir(blockpos);
                } else if (!this.world.isRemote) {
                    this.setDead();
                    return;
                }
            }

            if (!this.hasNoGravity()) {
                this.motionY += 0.03999999910593033D;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (!this.world.isRemote) {
                BlockPos blockpos1 = new BlockPos(this);

                if (!onGround && collidedVertically) {
                    IBlockState iblockstate = this.world.getBlockState(blockpos1);

                    if (this.world.isAirBlock(
                        new BlockPos(this.posX, this.posY + 1.009999999776482582D,
                            this.posZ))) // Forge: Don't indent below.
                    {
                        if (BlockFalling.canFallThrough(this.world.getBlockState(
                            new BlockPos(this.posX, this.posY + 1.009999999776482582D,
                                this.posZ)))) {
                            this.collidedVertically = false;
                            return;
                        }
                    }

                    this.motionX *= 0.699999988079071D;
                    this.motionZ *= 0.699999988079071D;
                    this.motionY *= -0.5D;

                    if (iblockstate.getBlock() != Blocks.PISTON_EXTENSION) {
                        this.setDead();

                        if (!this.dontSetBlock) {
                            if (this.world.mayPlace(block, blockpos1, true, EnumFacing.UP, null)
                                && !BlockFalling
                                .canFallThrough(this.world.getBlockState(blockpos1.up())) && world
                                .setBlockState(blockpos1, this.fallTile, 3)) {
                                if (block instanceof BlockFalling) {
                                    ((BlockFalling) block).onEndFalling(this.world, blockpos1,
                                        //not used by this xd
                                        null, null);
                                }

                                if (this.tileEntityData != null
                                    && block instanceof ITileEntityProvider) {
                                    TileEntity tileentity = this.world.getTileEntity(blockpos1);

                                    if (tileentity != null) {
                                        NBTTagCompound nbttagcompound = tileentity
                                            .writeToNBT(new NBTTagCompound());

                                        for (String s : this.tileEntityData.getKeySet()) {
                                            NBTBase nbtbase = this.tileEntityData.getTag(s);

                                            if (!"x".equals(s) && !"y".equals(s) && !"z"
                                                .equals(s)) {
                                                nbttagcompound.setTag(s, nbtbase.copy());
                                            }
                                        }

                                        tileentity.readFromNBT(nbttagcompound);
                                        tileentity.markDirty();
                                    }
                                }
                            } else if (this.shouldDropItem && this.world.getGameRules()
                                .getBoolean("doEntityDrops")) {
                                this.entityDropItem(
                                    new ItemStack(block, 1, block.damageDropped(this.fallTile)),
                                    0.0F);
                            }
                        }
                    }
                } else if (this.fallTime > 100 && (blockpos1.getY() < 1 || blockpos1.getY() > 256)
                    || this.fallTime > 600) {
                    if (this.shouldDropItem && this.world.getGameRules()
                        .getBoolean("doEntityDrops")) {
                        this.entityDropItem(
                            new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
                    }

                    this.setDead();
                }
            }
        }
    }

}
