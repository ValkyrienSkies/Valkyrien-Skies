package valkyrienwarfare.physics.collision.polygons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import valkyrienwarfare.api.Vector;

public class ClippedPolygon extends Polygon {

    private final List<Vector> clippedVertices;
    private final List<Vector> clippedVerticesUnmodifiable;
    
    public ClippedPolygon(Polygon other, Vector planeNormal, Vector planePos) {
        super(other);
        this.clippedVertices = new ArrayList<Vector>();
        for (Vector originalVertice : other.getVertices()) {
            if (isVerticeInFrontOfCullingPlane(originalVertice, planeNormal, planePos)) {
                // If the vertice is in front of the culling plane, we do not need to cull it.
                clippedVertices.add(originalVertice);
            } else {
                // If its behind the culling plane, we do need to cull it.
                // *Insert Culling Here*
            }
        }
        // Nothing else should be editing this list.
        clippedVerticesUnmodifiable = Collections.unmodifiableList(clippedVertices);
    }
    
    public List<Vector> getUnmodifiableClippedVertices() {
        return clippedVerticesUnmodifiable;
    }
    
    private boolean isVerticeInFrontOfCullingPlane(Vector vertice, Vector planeNormal, Vector planePos) {
        Vector difference = vertice.getSubtraction(planePos);
        // If its less than zero, we are behind the culling plane, so we do not cull this point
        return difference.dot(planeNormal) < 0;
    }
}
