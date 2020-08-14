package org.valkyrienskies.mod.common.util.jackson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is just like {@link com.fasterxml.jackson.annotation.JsonIgnore}, but the property is
 * specifically only ignored during VS network transmissions.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR,
    ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketIgnore {

}
