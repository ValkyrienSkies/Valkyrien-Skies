/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.physics.management;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class essentially handles all the issues with ticking and handling physics objects in the
 * given world
 *
 * @author thebest108
 */
@Deprecated
public class WorldPhysObjectManager {

    public final World worldObj;
    public final Set<PhysicsWrapperEntity> physicsEntities;

    public WorldPhysObjectManager(World toManage) {
        this.worldObj = toManage;
        this.physicsEntities = ConcurrentHashMap.newKeySet();
    }

    /**
     * Returns the list of PhysicsEntities that aren't too far away from players to justify being
     * ticked
     *
     * @return
     */
    @Deprecated
    public List<PhysicsWrapperEntity> getTickablePhysicsEntities() {
        List<PhysicsWrapperEntity> list = new ArrayList<>(physicsEntities);
        Iterator<PhysicsWrapperEntity> iterator = list.iterator();
        while (iterator.hasNext()) {
            PhysicsWrapperEntity wrapperEntity = iterator.next();
            if (!wrapperEntity.getPhysicsObject()
                .isFullyLoaded()) {
                // Don't tick ships that aren't fully loaded.
                iterator.remove();
            }
        }
        return list;
    }

    public void onLoad(PhysicsWrapperEntity loaded) {
        if (loaded.world.isRemote) {
            List<PhysicsWrapperEntity> potentialMatches = new ArrayList<PhysicsWrapperEntity>();
            for (PhysicsWrapperEntity wrapper : physicsEntities) {
                if (wrapper.getPersistentID().equals(loaded.getPersistentID())) {
                    potentialMatches.add(wrapper);
                }
            }
            for (PhysicsWrapperEntity caught : potentialMatches) {
                physicsEntities.remove(caught);
                caught.getPhysicsObject().onThisUnload();
                // System.out.println("Caught one");
            }
        }
        loaded.isDead = false;
        loaded.getPhysicsObject().resetConsecutiveProperTicks();
        physicsEntities.add(loaded);
    }

    public void onUnload(PhysicsWrapperEntity loaded) {
        if (loaded.world.isRemote) {
            loaded.isDead = true;
        }
        loaded.getPhysicsObject().onThisUnload();
        // Remove this ship from all our maps, we do not want to memory leak.
        this.physicsEntities.remove(loaded);
        loaded.getPhysicsObject().resetConsecutiveProperTicks();
    }

    public List<PhysicsWrapperEntity> getNearbyPhysObjects(AxisAlignedBB toCheck) {
        ArrayList<PhysicsWrapperEntity> ships = new ArrayList<>();
        AxisAlignedBB expandedCheck = toCheck.expand(6, 6, 6);

        for (PhysicsWrapperEntity wrapper : physicsEntities) {
            // This .expand() is only needed on server side, which tells me something is wrong with server side bounding
            // boxes
            if (wrapper.getPhysicsObject()
                .isFullyLoaded() && wrapper.getPhysicsObject()
                .getShipBoundingBox()
                .expand(2, 2, 2)
                .intersects(expandedCheck)) {
                ships.add(wrapper);
            }
        }

        return ships;
    }

}