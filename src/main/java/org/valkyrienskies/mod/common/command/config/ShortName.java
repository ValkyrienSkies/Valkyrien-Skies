package org.valkyrienskies.mod.common.command.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Use @ShortName on fields inside of a config class, like VSConfig, to set their name on the config
 * command. For example,
 *
 * <pre>{@code
 * @ShortName("rotation")
 * public static boolean DO_ROTATION = false;
 * }</pre>
 *
 * will allow you to use the command <code>/configcommand rotation false</code> rather than
 * <code>/configcommand do_rotation false</code>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShortName {

    String value();

}
