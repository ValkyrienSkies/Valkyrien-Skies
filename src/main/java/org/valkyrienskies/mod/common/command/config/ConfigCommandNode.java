package org.valkyrienskies.mod.common.command.config;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = false)
abstract class ConfigCommandNode {

    @Getter
    private String name;

    ConfigCommandNode(String name) {
        this.name = name;
    }

}
