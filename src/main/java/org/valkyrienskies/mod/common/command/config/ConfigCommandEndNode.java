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
    private Consumer<String> setOption;

    @Getter
    private Supplier<?> getOption;

    ConfigCommandEndNode(String name, Consumer<String> setOption, Supplier<?> getOption) {
        super(name);
        this.setOption = setOption;
        this.getOption = getOption;
    }

}
