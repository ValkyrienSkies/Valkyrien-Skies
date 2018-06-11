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

// A modified version of the CPacketPlayer class that adds extra fields for local coordinates
// relative to a ship. This is the superclass of all three types of CPacketPlayer, so it is the 
// class that holds the extra fields we'll need to store this information. It does not however send
// these fields over the byteBuffers by default; because of the way Mixins handle @Overwrites and 
// @Injections we'll have to send these extra fields by overwriting the write and reads methods of
// the leaf class.

package valkyrienwarfare.mixin.network.play.client;

import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.coordinates.TransformType;
import valkyrienwarfare.mod.network.IExtendedCPacketPlayer;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer implements IExtendedCPacketPlayer {

    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;
    private int worldBelowID = -1;
    private double localX;
    private double localY;
    private double localZ;

    @Override
    public boolean hasShipWorldBelowFeet() {
        return getWorldBelowFeetID() != -1;
    }

    @Override
    public int getWorldBelowFeetID() {
        return worldBelowID;
    }

    @Override
    public void setWorldBelowFeetID(int entityID) {
        worldBelowID = entityID;
    }

    @Override
    public void setLocalCoords(double localX, double localY, double localZ) {
        this.localX = localX;
        this.localY = localY;
        this.localZ = localZ;
    }

    @Override
    public Vector getLocalCoordsVector() {
        return new Vector(localX, localY, localZ);
    }

    // TODO: This doesnt work yet
    @Overwrite
    public void processPacket(INetHandlerPlayServer handler) {
        // System.out.println(this.getLocalCoordsVector());
        if (worldBelowID != -1) {
            // System.out.println("RTest");
            try {
                NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) handler;
                World world = serverHandler.player.getEntityWorld();
                Entity theEntity = world.getEntityByID(worldBelowID);
                PhysicsWrapperEntity worldBelow = (PhysicsWrapperEntity) theEntity;
                Vector positionInGlobal = new Vector(localX, localY, localZ);
                worldBelow.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(positionInGlobal, TransformType.SUBSPACE_TO_GLOBAL);
                Vector distanceDiscrepency = new Vector(x, y, z);
                distanceDiscrepency.subtract(positionInGlobal);
                x = positionInGlobal.X;
                y = positionInGlobal.Y;
                z = positionInGlobal.Z;

                // TODO: We shouldnt trust the clients about this either
                serverHandler.floatingTickCount = 0;
                serverHandler.floating = false;
                // System.out.println("Coords transformed to <" + x + ", " + y + ", " + z +
                // ">");
                // System.out.println("Distance discrepency of " + distanceDiscrepency + ", of
                // length "+ distanceDiscrepency.length() + " meters");
            } catch (Exception e) {
                return;
            }
        }
        handler.processPlayer(CPacketPlayer.class.cast(this));
    }

    @Override
    public double getLocalX() {
        return localX;
    }

    @Override
    public double getLocalY() {
        return localY;
    }

    @Override
    public double getLocalZ() {
        return localZ;
    }

}
