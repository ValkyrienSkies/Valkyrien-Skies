package valkyrienwarfare.physics.collision;

import valkyrienwarfare.api.Vector;

public class PolygonCollisionPointFinder {

    // TODO: This algorithm isn't correct, fix it later on!
    public static Vector[] getPointsOfCollisionForPolygons(PhysPolygonCollider collider,
            PhysCollisionObject somethingElse, Vector velocity) {
        double minSecondsAgo = 69D;
        int minCollisionIndex = 0;

        for (int cont = 0; cont < collider.collisions.length; cont++) {
            PhysCollisionObject toCollideWith = collider.collisions[cont];
            Vector axis = somethingElse.axis;
            double reverseVelocityAlongAxis = -velocity.dot(axis);
            double minDot = 9999999999999D;
            int minIndex = 0;

            for (int i = 0; i < 8; i++) {
                Vector vertice = toCollideWith.movable.getVertices()[i];
                double dot = vertice.dot(axis) * reverseVelocityAlongAxis;
                if (dot < minDot) {
                    minDot = dot;
                    minIndex = i;
                }
            }

            Vector contactPoint = toCollideWith.movable.getVertices()[minIndex];
            double secondsAgo = 69;
            if (Math.signum(reverseVelocityAlongAxis) == 1.0D) {
                secondsAgo = toCollideWith.movMinFixMax / reverseVelocityAlongAxis;
            } else {
                secondsAgo = toCollideWith.movMaxFixMin / reverseVelocityAlongAxis;
            }

            if (secondsAgo < minSecondsAgo) {
                minSecondsAgo = secondsAgo;
                minCollisionIndex = cont;
            }
        }

        return new Vector[] {
                // contactPoint
                collider.collisions[minCollisionIndex].firstContactPoint,
                collider.collisions[minCollisionIndex].getSecondContactPoint() };
    }

}