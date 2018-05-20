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

package valkyrienwarfare.mod.physmanagement.interaction;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.concurrent.Immutable;

/**
 * PlayerDataBackup creates a copy of player position, rotation and velocity
 * information, and provides functionality to reset the player to that given
 * state. Used when transforming the player to local coordinates for block
 * placing and breaking.
 *
 * @author thebest108
 */
@Immutable
public class PlayerDataBackup {

    private final EntityPlayer player;
    private final double posX, posY, posZ;
    private final double lastTickPosX, lastTickPosY, lastTickPosZ;
    private final float rotationYaw, rotationPitch;
    private final float prevRotationYaw, prevRotationPitch;
    private final double motionX, motionY, motionZ;

    public PlayerDataBackup(EntityPlayer playerToBackup) {
        player = playerToBackup;
        // Set all the variables past here.
        posX = playerToBackup.posX;
        lastTickPosX = playerToBackup.lastTickPosX;
        posY = playerToBackup.posY;
        lastTickPosY = playerToBackup.lastTickPosY;
        posZ = playerToBackup.posZ;
        lastTickPosZ = playerToBackup.lastTickPosZ;
        rotationYaw = playerToBackup.rotationYaw;
        rotationPitch = playerToBackup.rotationPitch;
        prevRotationYaw = playerToBackup.prevRotationYaw;
        prevRotationPitch = playerToBackup.prevRotationPitch;
        motionX = playerToBackup.motionX;
        motionY = playerToBackup.motionY;
        motionZ = playerToBackup.motionZ;
    }

    public void restorePlayerToBackup() {
        player.posX = posX;
        player.lastTickPosX = lastTickPosX;
        player.posY = posY;
        player.lastTickPosY = lastTickPosY;
        player.posZ = posZ;
        player.lastTickPosZ = lastTickPosZ;
        player.rotationYaw = rotationYaw;
        player.rotationPitch = rotationPitch;
        player.prevRotationYaw = prevRotationYaw;
        player.prevRotationPitch = prevRotationPitch;
        player.setPosition(posX, posY, posZ);
        player.motionX = motionX;
        player.motionY = motionY;
        player.motionZ = motionZ;
    }
}
