package org.valkyrienskies.mod.common.command.config;

import lombok.Getter;

abstract class ConfigCommandNode {

    @Getter
    private String name;

    ConfigCommandNode(String name) {
        this.name = name;
    }

}
