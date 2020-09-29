package org.valkyrienskies.mod.common.collision;

import lombok.Getter;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.entity_interaction.EntityCollisionObject;

/**
 * Heavily modified version of the original Polygon Collider, with checks on polygons passing
 * through eachother, normals not to be considered, and a consideration for Net Velocity between the
 * polygons
 *
 * @author thebest108
 */
public class EntityPolygonCollider {

    @Getter
    private final Vector3dc[] collisionAxes;
    @Getter
    private final EntityCollisionObject[] collisions;
    private final Polygon entity;
    private final Polygon block;
    private final Vector3dc entityVelocity;
    private boolean separated = false;
    @Getter
    private int minDistanceIndex;
    private boolean originallySeparated;

    public EntityPolygonCollider(Polygon movable, Polygon stationary, Vector3dc[] axes,
        Vector3dc entityVel) {
        collisionAxes = axes;
        entity = movable;
        block = stationary;
        entityVelocity = entityVel;
        collisions = new EntityCollisionObject[collisionAxes.length];
        processData();
    }

    public void processData() {
        separated = false;
        for (int i = 0; i < collisions.length; i++) {
            if (!separated) {
                collisions[i] = new EntityCollisionObject(entity, block, collisionAxes[i],
                    entityVelocity);
                if (collisions[i].arePolygonsSeperated()) {
                    separated = true;
                    break;
                }
                if (!collisions[i].werePolygonsInitiallyColliding()) {
                    originallySeparated = true;
                }
            }
        }
        if (!separated) {
            double minDistance = 420;
            for (int i = 0; i < collisions.length; i++) {
                if (originallySeparated) {
                    if (Math.abs((collisions[i].getCollisionPenetrationDistance() - collisions[i]
                        .getVelDot()) / collisions[i].getVelDot()) < minDistance && !collisions[i]
                        .werePolygonsInitiallyColliding()) {
                        minDistanceIndex = i;
                        minDistance = Math.abs(
                            (collisions[i].getCollisionPenetrationDistance() - collisions[i]
                                .getVelDot()) / collisions[i].getVelDot());
                    }
                } else {
                    // This is wrong
                    if (Math.abs(collisions[i].getCollisionPenetrationDistance()) < minDistance) {
                        minDistanceIndex = i;
                        minDistance = Math.abs(collisions[i].getCollisionPenetrationDistance());
                    }
                }
            }
        }
    }

    public boolean arePolygonsSeparated() {
        return separated;
    }
}