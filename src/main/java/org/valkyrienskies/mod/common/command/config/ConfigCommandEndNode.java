package org.valkyrienskies.mod.common.command.config;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Node at the end of a config command, e.g. in <code>/vsc enginePower basicEngineForce</code>,
 * <code>basicEngineForce</code> is the End Node
 */
class ConfigCommandEndNode extends ConfigCommandNode {

    @Getter
    private Consumer<String> optionSetter;

    @Getter
    private Supplier<?> optionGetter;

    ConfigCommandEndNode(String name, Consumer<String> optionSetter, Supplier<?> optionGetter) {
        super(name);
        this.optionSetter = optionSetter;
        this.optionGetter = optionGetter;
    }

}
