package org.valkyrienskies.mod.common.command.config;

import lombok.Getter;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Node at the end of a config command, e.g. in <code>/vsc enginePower basicEngineForce</code>,
 * <code>basicEngineForce</code> is the End Node
 */
class ConfigCommandEndNode extends ConfigCommandNode {

    @Getter
    private final Consumer<String> optionSetter;

    @Getter
    private final Supplier<?> optionGetter;

    @Getter
    private final List<String> autocompletions;

    ConfigCommandEndNode(Field field, @Nullable Object object) {
        this(
            getName(field),
            str -> ConfigCommandUtils.setFieldFromString(str, field, object),
            () -> ConfigCommandUtils.getStringFromField(field, object),
            ConfigCommandUtils.getAutocompletions(field)
        );
    }

    private static String getName(Field field) {
        return Optional.ofNullable(field.getAnnotation(ShortName.class))
            .map(ShortName::value)
            .orElseGet(field::getName);
    }

    ConfigCommandEndNode(String name, Consumer<String> optionSetter, Supplier<?> getOption,
                         List<String> autocompletions) {
        super(name);
        this.optionSetter = optionSetter;
        this.optionGetter = getOption;
        this.autocompletions = autocompletions;
    }

}
