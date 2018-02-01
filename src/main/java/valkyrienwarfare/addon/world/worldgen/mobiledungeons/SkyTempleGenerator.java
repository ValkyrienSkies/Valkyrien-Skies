/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.world.worldgen.mobiledungeons;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.world.ValkyrienWarfareWorld;
import valkyrienwarfare.addon.world.tileentity.TileEntitySkyTempleController;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.physicsmanagement.ShipType;
import valkyrienwarfare.schematics.SchematicReader;
import valkyrienwarfare.schematics.SchematicReader.Schematic;

import java.util.Random;

public class SkyTempleGenerator {

    final static BlockPos skyControllerPos = new BlockPos(7, 15, 8);
    final static BlockPos skulkerBoxPos = new BlockPos(7, 16, 8);

    public static void runGenerator(World world, int chunkX, int chunkZ, double random) {
        if (random < ValkyrienWarfareMod.shipmobs_spawnrate) {
            Schematic lootGet = SchematicReader.get("flying_temple_real.schemat");

            if (lootGet == null) {
                return;
            }

            PhysicsWrapperEntity wrapperEntity = new PhysicsWrapperEntity(world, chunkX << 4, 150, chunkZ << 4, ShipType.Dungeon_Sky, lootGet);

            runFinishingTouches(wrapperEntity, lootGet);

            //do it
            wrapperEntity.forceSpawn = true;

            wrapperEntity.posY = 50D;

            world.spawnEntity(wrapperEntity);

            wrapperEntity.posY = 50D;
        }
    }

    public static void runFinishingTouches(PhysicsWrapperEntity wrapperEntity, Schematic lootGet) {
        BlockPos centerInSchematic = new BlockPos(-(lootGet.width / 2), 128 - (lootGet.height / 2), -(lootGet.length / 2));

        BlockPos centerDifference = wrapperEntity.wrapping.refrenceBlockPos.subtract(centerInSchematic);

        BlockPos realSkyControllerPos = skyControllerPos.add(centerDifference);
        BlockPos realSkulkerBoxPos = skulkerBoxPos.add(centerDifference);

        wrapperEntity.world.setBlockState(realSkyControllerPos, ValkyrienWarfareWorld.INSTANCE.skydungeon_controller.getDefaultState());

        wrapperEntity.yaw = Math.random() * 360D;

        TileEntityShulkerBox skulkerTile = (TileEntityShulkerBox) wrapperEntity.world.getTileEntity(realSkulkerBoxPos);

        ItemStack stack = ValkyrienWarfareWorld.INSTANCE.etheriumCrystal.getDefaultInstance().copy();
        stack.stackSize = 5;

        skulkerTile.setInventorySlotContents(new Random().nextInt(26), stack);

        TileEntitySkyTempleController skyTile = (TileEntitySkyTempleController) wrapperEntity.world.getTileEntity(realSkyControllerPos);
        skyTile.setOriginPos(new Vector(wrapperEntity.posX, wrapperEntity.posY, wrapperEntity.posZ));
    }
}
