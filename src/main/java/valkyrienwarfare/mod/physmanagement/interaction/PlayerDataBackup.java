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

public class PlayerDataBackup {

    private final EntityPlayer playerToBackup;
    private double posX, posY, posZ;
    private double lastTickPosX, lastTickPosY, lastTickPosZ;
    private float rotationYaw, rotationPitch;
    private float prevRotationYaw, prevRotationPitch;
    private double motionX, motionY, motionZ;

    public PlayerDataBackup(EntityPlayer playerToBackup) {
        this.playerToBackup = playerToBackup;
        generatePlayerBackup();
    }

    public void generatePlayerBackup() {
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
        playerToBackup.posX = posX;
        playerToBackup.lastTickPosX = lastTickPosX;
        playerToBackup.posY = posY;
        playerToBackup.lastTickPosY = lastTickPosY;
        playerToBackup.posZ = posZ;
        playerToBackup.lastTickPosZ = lastTickPosZ;
        playerToBackup.rotationYaw = rotationYaw;
        playerToBackup.rotationPitch = rotationPitch;
        playerToBackup.prevRotationYaw = prevRotationYaw;
        playerToBackup.prevRotationPitch = prevRotationPitch;
        playerToBackup.setPosition(posX, posY, posZ);
        playerToBackup.motionX = motionX;
        playerToBackup.motionY = motionY;
        playerToBackup.motionZ = motionZ;
    }
}
