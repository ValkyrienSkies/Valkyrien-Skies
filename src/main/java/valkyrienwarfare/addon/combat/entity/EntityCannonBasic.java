/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.combat.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.fixes.IInventoryPlayerFix;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

public class EntityCannonBasic extends EntityMountingWeaponBase {

    int tickDelay = 6;
    // int currentTicksOperated = 0;
    boolean isCannonLoaded = false;

    public EntityCannonBasic(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onRiderInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (!player.world.isRemote) {
            if (canPlayerInteract(player, stack, hand)) {
                fireCannon(player, stack, hand);
            }
        }
    }

    public void fireCannon(EntityPlayer player, ItemStack stack, EnumHand hand) {
        Vec3d velocityNormal = getVectorForRotation(rotationPitch, rotationYaw);
        Vector velocityVector = new Vector(velocityNormal);

        PhysicsWrapperEntity wrapper = getParentShip();

        velocityVector.multiply(3D);
        EntityCannonBall projectile = new EntityCannonBall(world, velocityVector, this);

        Vector projectileSpawnPos = new Vector(0, .5, 0);

        if (wrapper != null) {
            wrapper.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().rotate(projectileSpawnPos, TransformType.SUBSPACE_TO_GLOBAL);
        }

        projectile.posX += projectileSpawnPos.X;
        projectile.posY += projectileSpawnPos.Y;
        projectile.posZ += projectileSpawnPos.Z;

        world.spawnEntity(projectile);

        isCannonLoaded = false;
        // worldObj.playSound(null, posX, posY, posZ, new SoundEvent(), SoundCategory.AMBIENT, volume, pitch, true);
    }

    public boolean canPlayerInteract(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (currentTicksOperated < 0) {
            currentTicksOperated++;
            return false;
        }
        if (!isCannonLoaded) {
            ItemStack cannonBallStack = new ItemStack(ValkyrienWarfareCombat.INSTANCE.cannonBall);
            ItemStack powderStack = new ItemStack(ValkyrienWarfareCombat.INSTANCE.powderPouch);

            boolean hasCannonBall = player.inventory.hasItemStack(cannonBallStack);
            boolean hasPowder = player.inventory.hasItemStack(powderStack);
            if (hasCannonBall && hasPowder || player.isCreative()) {
                for (NonNullList<ItemStack> aitemstack : IInventoryPlayerFix.getFixFromInventory(player.inventory).getAllInventories()) {
                    for (ItemStack itemstack : aitemstack) {
                        if (itemstack != null && itemstack.isItemEqual(cannonBallStack)) {
                            int itemStackSize = itemstack.getCount();
                            itemstack.setCount(itemStackSize - 1);
                            if (itemstack.getCount() <= 0) {
                                int index = player.inventory.getSlotFor(itemstack);
                                player.inventory.setInventorySlotContents(index, ItemStack.EMPTY);
                            }
                        }
                        if (itemstack != null && itemstack.isItemEqual(powderStack)) {
                            int itemStackSize = itemstack.getCount();
                            itemstack.setCount(itemStackSize - 1);
                            if (itemstack.getCount() <= 0) {
                                int index = player.inventory.getSlotFor(itemstack);
                                player.inventory.setInventorySlotContents(index, null);
                            }
                        }
                    }
                }
                isCannonLoaded = true;
            }
        } else {
            currentTicksOperated++;
            if (currentTicksOperated > tickDelay) {
                // currentTicksOperated = -4;
                return true;
            }
        }

        return false;
    }

    @Override
    public void doItemDrops() {
        ItemStack itemstack = new ItemStack(ValkyrienWarfareCombat.INSTANCE.basicCannonSpawner, 1);

        if (this.getName() != null) {
            itemstack.setStackDisplayName(this.getName());
        }

        this.entityDropItem(itemstack, 0.0F);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        isCannonLoaded = tagCompund.getBoolean("isCannonLoaded");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setBoolean("isCannonLoaded", isCannonLoaded);
    }

}
