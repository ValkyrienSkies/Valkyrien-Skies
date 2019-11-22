package org.valkyrienskies.mod.common.util.jackson.annotations;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class VSAnnotationIntrospector extends JacksonAnnotationIntrospector {

    public static final VSAnnotationIntrospector instance = new VSAnnotationIntrospector();

    private VSAnnotationIntrospector() {}

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        if (m.hasAnnotation(PacketIgnore.class)) return true;
        return super.hasIgnoreMarker(m);
    }

}
