package ValkyrienWarfareBase.API.Addons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * doesn't do anything, just practical for picking up addons during init
 * all addons must have this interface, otherwise they WILL NOT load
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VWAddon {
}
