package org.valkyrienskies.mod.common.command.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;

public class ConfigCommandParentNode extends ConfigCommandNode {

    @Getter
    private Map<String, ConfigCommandNode> children;

    ConfigCommandParentNode(String name, List<ConfigCommandNode> children) {
        super(name);
        this.children = new HashMap<>(
            children.stream().collect(Collectors.toMap(c -> c.name().toLowerCase(), c -> c))
        );
    }

    ConfigCommandParentNode(String name) {
        super(name);
        this.children = new HashMap<>();
    }

    ConfigCommandParentNode(String name, Map<String, ConfigCommandNode> children) {
        super(name);
        this.children = new HashMap<>(children);
    }

    void addChild(ConfigCommandNode node) {
        children.put(node.name().toLowerCase(), node);
    }

    @Nullable
    ConfigCommandNode getChild(String name) {
        return children.get(name.toLowerCase());
    }

    List<String> childrenNames() {
        return children.values().stream().map(ConfigCommandNode::name).collect(Collectors.toList());
    }

    Collection<ConfigCommandNode> getChildrenStartingWith(String prefix) {
        return children.values().stream()
            .filter(c -> c.name().toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
