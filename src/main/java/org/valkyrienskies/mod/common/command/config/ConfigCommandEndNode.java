package org.valkyrienskies.mod.common.command.config;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;

public class ConfigCommandEndNode extends ConfigCommandNode {

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
