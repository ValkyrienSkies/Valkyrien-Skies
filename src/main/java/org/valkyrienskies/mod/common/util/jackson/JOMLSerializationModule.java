package org.valkyrienskies.mod.common.util.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class JOMLSerializationModule extends SimpleModule {

    public JOMLSerializationModule() {
        super.addAbstractTypeMapping(Vector3dc.class, Vector3d.class);
        super.addAbstractTypeMapping(Quaterniondc.class, Quaterniond.class);
        super.addAbstractTypeMapping(Matrix4dc.class, Matrix4d.class);
        super.addAbstractTypeMapping(Matrix3dc.class, Matrix3d.class);
    }

}
