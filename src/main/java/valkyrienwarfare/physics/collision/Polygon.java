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

package valkyrienwarfare.physics.collision;

import net.minecraft.util.math.AxisAlignedBB;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.data.ShipTransform;
import valkyrienwarfare.physics.data.TransformType;

/**
 * Stores vertices for a polygon, and also has some other operations
 *
 * @author thebest108
 */
public class Polygon {

	private final Vector[] vertices;
	private final Vector velocity;
	private final Vector[] normals;

	public Polygon(AxisAlignedBB bb) {
		this.vertices = getCornersForAABB(bb);
		this.velocity = new Vector();
		this.normals = Vector.generateAxisAlignedNorms();
	}

	public Polygon(AxisAlignedBB bb, ShipTransform transformation, TransformType transformType) {
		this(bb);
		for (int i = 0; i < vertices.length; i++) {
		    transformation.transform(vertices[i], transformType);
		}
		for (Vector normal : normals) {
		    transformation.rotate(normal, transformType);
		}
	}
	
	// Copies one polygon onto another.
	protected Polygon(Polygon other) {
	    this.velocity = other.velocity;
	    this.vertices = new Vector[other.vertices.length];
	    this.normals = other.normals;
	}

	public Vector[] getVertices() {
		return vertices;
	}
	
	public Vector[] getNormals() {
	    return normals;
	}

	public double[] getProjectionOnVector(Vector axis) {
		double[] distances = new double[vertices.length];
		for (int i = 0; i < vertices.length; i++) {
			distances[i] = axis.dot(vertices[i]);
		}
		return distances;
	}

	public Vector getCenter() {
		Vector center = new Vector();
		for (Vector v : vertices) {
			center.add(v);
		}
		center.divide(vertices.length);
		return center;
	}

	public AxisAlignedBB getEnclosedAABB() {
		Vector vector = vertices[0];
		double mnX = vector.X;
		double mnY = vector.Y;
		double mnZ = vector.Z;
		double mxX = vector.X;
		double mxY = vector.Y;
		double mxZ = vector.Z;
		for (int i = 1; i < vertices.length; i++) {
			vector = vertices[i];
			mnX = Math.min(mnX, vector.X);
			mnY = Math.min(mnY, vector.Y);
			mnZ = Math.min(mnZ, vector.Z);
			mxX = Math.max(mxX, vector.X);
			mxY = Math.max(mxY, vector.Y);
			mxZ = Math.max(mxZ, vector.Z);
		}
		return new AxisAlignedBB(mnX, mnY, mnZ, mxX, mxY, mxZ);
	}

	private static Vector[] getCornersForAABB(AxisAlignedBB bb) {
		return new Vector[] { new Vector(bb.minX, bb.minY, bb.minZ), new Vector(bb.minX, bb.maxY, bb.minZ),
				new Vector(bb.minX, bb.minY, bb.maxZ), new Vector(bb.minX, bb.maxY, bb.maxZ),
				new Vector(bb.maxX, bb.minY, bb.minZ), new Vector(bb.maxX, bb.maxY, bb.minZ),
				new Vector(bb.maxX, bb.minY, bb.maxZ), new Vector(bb.maxX, bb.maxY, bb.maxZ) };
	}

}