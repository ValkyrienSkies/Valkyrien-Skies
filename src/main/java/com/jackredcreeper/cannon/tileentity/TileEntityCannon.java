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

package com.jackredcreeper.cannon.tileentity;

import com.jackredcreeper.cannon.blocks.BlockCannon;
import com.jackredcreeper.cannon.entities.EntityCannonball;
import com.jackredcreeper.cannon.entities.EntityExplosiveball;
import com.jackredcreeper.cannon.entities.EntityGrapeshot;
import com.jackredcreeper.cannon.entities.EntitySolidball;
import com.jackredcreeper.cannon.init.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class TileEntityCannon extends TileEntity {

    int CannonCooldown = 0;
    int CC = 0;
    boolean CannonReady = false;
    float Angle = 0;
    int Ammo = 0;


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        super.writeToNBT(compound);
        compound.setInteger("CannonCooldown", CannonCooldown);
        compound.setBoolean("CannonReady", CannonReady);
        compound.setFloat("Angle", Angle);
        compound.setInteger("Ammo", Ammo);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        super.readFromNBT(compound);
        CannonCooldown = compound.getInteger("CannonCooldown");
        CannonReady = compound.getBoolean("CannonReady");
        Angle = compound.getFloat("Angle");
        Ammo = compound.getInteger("Ammo");
    }

    public void fireCannon(World worldIn, EntityPlayer playerIn, BlockPos pos, IBlockState state, EnumFacing side) {

        if (CannonReady) {
            firecannon(worldIn, pos, side, state);
            CannonReady = false;
            CannonCooldown = 0;
        } else {
            playerIn.sendMessage(new TextComponentString("Cannon needs to be loaded!"));
        }
    }


    public void loadCannon(World worldIn, EntityPlayer playerIn) {
        if (Ammo != 0) {

            if (CannonCooldown == CC) {
                CannonReady = true;
                worldIn.playSound((EntityPlayer) null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 3, 1);
            } else {
                CannonCooldown++;
            }
        } else {
            playerIn.sendMessage(new TextComponentString("Cannon needs ammo!"));
        }
    }


////////////////////////////////////// Make shit go boom

    public void firecannon(World worldIn, BlockPos pos, EnumFacing side, IBlockState state) {

        //direction
        EnumFacing enumfacing = (EnumFacing) state.getValue(BlockCannon.LOOKING);
        double d0 = pos.getX() + 0.5D + (float) enumfacing.getFrontOffsetX();
        double d1 = pos.getY() + 0.4D + (float) enumfacing.getFrontOffsetY();
        double d2 = pos.getZ() + 0.5D + (float) enumfacing.getFrontOffsetZ();

        EntityCannonball entitycannonball = new EntityCannonball(worldIn, d0, d1, d2);
        EntityExplosiveball entityexplosiveball = new EntityExplosiveball(worldIn, d0, d1, d2);
        EntityGrapeshot entitygrapeshot1 = new EntityGrapeshot(worldIn, d0, d1, d2);
        EntityGrapeshot entitygrapeshot2 = new EntityGrapeshot(worldIn, d0, d1, d2);
        EntityGrapeshot entitygrapeshot3 = new EntityGrapeshot(worldIn, d0, d1, d2);
        EntityGrapeshot entitygrapeshot4 = new EntityGrapeshot(worldIn, d0, d1, d2);
        EntityGrapeshot entitygrapeshot5 = new EntityGrapeshot(worldIn, d0, d1, d2);
        EntitySolidball entitysolidball = new EntitySolidball(worldIn, d0, d1, d2);


        if (Ammo == 1) {
            entitycannonball.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 1);
            worldIn.spawnEntity(entitycannonball);
        }
        if (Ammo == 2) {
            entityexplosiveball.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 1);
            worldIn.spawnEntity(entityexplosiveball);
        }
        if (Ammo == 3) {
            entitygrapeshot5.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 4.0F);
            entitygrapeshot4.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 4.0F);
            entitygrapeshot3.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 4.0F);
            entitygrapeshot2.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 4.0F);
            entitygrapeshot1.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 3, 4.0F);
            worldIn.spawnEntity(entitygrapeshot1);
            worldIn.spawnEntity(entitygrapeshot2);
            worldIn.spawnEntity(entitygrapeshot3);
            worldIn.spawnEntity(entitygrapeshot4);
            worldIn.spawnEntity(entitygrapeshot5);
        }
        if (Ammo == 4) {
            entitysolidball.shoot((double) enumfacing.getFrontOffsetX(), (double) ((float) enumfacing.getFrontOffsetY() + (Angle / 100)), (double) enumfacing.getFrontOffsetZ(), 2, 1);
            worldIn.spawnEntity(entitysolidball);
        }


        worldIn.playSound((EntityPlayer) null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 2, 1.5F);

        Ammo = 0;

    }

/////////////////////////////////////// End of making shit go boom

/////////////////////////////////////// Angle 'n' ammo

    public void setAngle(EntityPlayer playerIn, double angle) {
        Angle += angle;
        if (Angle > 15F) {
            Angle = -15F;
        }
        if (Angle < -15F) {
            Angle = 15F;
        }
        playerIn.sendMessage(new TextComponentString("Extra Y velocity = " + Float.toString(Angle / 100)));
    }

    public void setAmmo(ItemStack heldItem) {
        Item item = heldItem.getItem();
        if (item == ModItems.cannonball) {
            Ammo = 1;
            CC = 7;
        }
        if (item == ModItems.explosiveball) {
            Ammo = 2;
            CC = 14;
        }
        if (item == ModItems.grapeshot) {
            Ammo = 3;
            CC = 7;
        }
        if (item == ModItems.solidball) {
            Ammo = 4;
            CC = 7;
        }
        CannonReady = false;
    }


}