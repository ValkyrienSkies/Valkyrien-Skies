package org.valkyrienskies.mod.common.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

public class JOML {

    // region To JOML

    public static Vector3f convert(org.lwjgl.util.vector.Vector3f v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    public static Vector3d convert(net.minecraft.client.renderer.Vector3d v) {
        return new Vector3d(v.x, v.y, v.z);
    }

    public static Quaternionf convert(org.lwjgl.util.vector.Quaternion q) {
        return new Quaternionf(q.x, q.y, q.z, q.w);
    }

    // endregion

    // region From JOML

    public static Vec3d toMinecraft(Vector3dc v) {
        return new Vec3d(v.x(), v.y(), v.z());
    }

    public static Vec3i toMinecraft(Vector3ic v) {
        return new Vec3i(v.x(), v.y(), v.z());
    }

    public static net.minecraft.client.renderer.Vector3d toMinecraftRenderer(Vector3dc v) {
        net.minecraft.client.renderer.Vector3d vec = new net.minecraft.client.renderer.Vector3d();
        vec.x = v.x();
        vec.y = v.y();
        vec.z = v.z();
        return vec;
    }

    public static org.lwjgl.util.vector.Vector3f toLWJGL(Vector3fc v) {
        return new org.lwjgl.util.vector.Vector3f(v.x(), v.y(), v.z());
    }

    // endregion

    // region Cast

    public static Vector3d castDouble(Vector3fc v) {
        return new Vector3d(v.x(), v.y(), v.z());
    }

    public static Vector3d castDouble(Vector3ic v) {
        return new Vector3d(v.x(), v.y(), v.z());
    }

    public static Quaterniondc castDouble(Quaternionfc q) {
        return new Quaterniond(q.x(), q.y(), q.z(), q.w());
    }

    public static Quaternionf castFloat(Quaterniondc q) {
        return new Quaternionf((float) q.x(), (float) q.y(), (float) q.z(), (float) q.w());
    }

    public static Vector3f castFloat(Vector3dc v) {
        return new Vector3f((float) v.x(), (float) v.y(), (float) v.y());
    }

    public static Vector3f castFloat(Vector3ic v) {
        return new Vector3f(v.x(), v.y(), v.z());
    }


    // endregion

}
