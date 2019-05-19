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

package valkyrienwarfare.deprecated_api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

/**
 * Call whatever method you need from here. Outside of Vector, all the objects here are generic (Ships being Entities). Just be sure not to pass something wrong into here
 *
 * @author thebest108
 */
public class ValkyrienWarfareHooks {

    // Replaced with an object that does real work in runtime
    public static DummyMethods methods = null;
    public static boolean isValkyrienWarfareInstalled = false;

    public static PhysicsWrapperEntity getShipEntityManagingPos(World worldObj, BlockPos pos) {
        return methods.getShipEntityManagingPos(worldObj, pos);
    }

    /**
     * Tells you if a block position is being used by a Ship
     *
     * @param worldObj
     * @param pos
     * @return
     */
    public static boolean isBlockPartOfShip(World worldObj, BlockPos pos) {
        return methods.isBlockPartOfShip(worldObj, pos);
    }

    /**
     * Converts a coordinate from the World space to the Ship space
     *
     * @param worldObj
     * @param shipEnt
     * @param positionInWorld
     * @return A new Vector object, not the same as the one inputed
     */
    public static Vector getPositionInShipFromReal(World worldObj, Entity shipEnt, Vector positionInWorld) {
        return methods.getPositionInShipFromReal(worldObj, shipEnt, positionInWorld);
    }

    /**
     * Converts a coordinate from the Ship space to the World space
     *
     * @param worldObj
     * @param shipEnt
     * @param positionInWorld
     * @return A new Vector object, not the same as the one inputed
     */
    public static Vector getPositionInRealFromShip(World worldObj, Entity shipEnt, Vector posInShip) {
        return methods.getPositionInRealFromShip(worldObj, shipEnt, posInShip);
    }

    /**
     * @param entityToTest
     * @return True if the entity is a Ship, false if it isn't
     */
    public static boolean isEntityAShip(Entity entityToTest) {
        return methods.isEntityAShip(entityToTest);
    }

    /**
     * @param shipEnt
     * @return The Center of Mass coordinates of a Ship in Ship space
     */
    public static Vector getShipCenterOfMass(Entity shipEnt) {
        return methods.getShipCenterOfMass(shipEnt);
    }

    /**
     * @param shipEnt
     * @param secondsToApply
     * @return A vector with the linear velocity of a Ship at that instant
     */
    public static Vector getLinearVelocity(Entity shipEnt, double secondsToApply) {
        return methods.getLinearVelocity(shipEnt, secondsToApply);
    }

    /**
     * @param shipEnt
     * @return A vector with the angular velocty of a Ship at that instant
     */
    public static Vector getAngularVelocity(Entity shipEnt) {
        return methods.getAngularVelocity(shipEnt);
    }

    /**
     * @param shipEnt
     * @return The matrix which converts local coordinates (The positions of the blocks in the world) to the entity coordinates (The position in front of the player)
     */
    public static double[] getShipTransformMatrix(Entity shipEnt) {
        return methods.getShipTransformMatrix(shipEnt);
    }

    /**
     * @param shipEnt
     * @param inBody
     * @param secondsToApply
     * @return A Vector with the velocity of a point in Ship space relative to the real world, at that instant
     */
    public static Vector getVelocityAtPoint(Entity shipEnt, Vector inBody, double secondsToApply) {
        return methods.getVelocityAtPoint(shipEnt, inBody, secondsToApply);
    }

    /**
     * @param shipEnt
     * @return The mass of the Ship
     */
    public static double getShipMass(Entity shipEnt) {
        return methods.getShipMass(shipEnt);
    }
}
