package org.valkyrienskies.mod.common.command.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * Any node which is not a {@link ConfigCommandEndNode}
 */
class ConfigCommandParentNode extends ConfigCommandNode {

    @Getter
    private Map<String, ConfigCommandNode> children;

    /**
     * Create a parent node with an existing list of children.
     *
     * @param name The name of the parent node
     * @param children The nodes which should become children of this parent node
     */
    ConfigCommandParentNode(String name, List<ConfigCommandNode> children) {
        super(name);
        this.children = new HashMap<>(
            children.stream().collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c))
        );
    }

    /**
     * Create a parent node
     *
     * @param name The name of the parent node
     */
    ConfigCommandParentNode(String name) {
        super(name);
        this.children = new HashMap<>();
    }

    /**
     * It is recommended that you instead use {@link #ConfigCommandParentNode(String, List)}. Create
     * a parent node with an existing map of (lowercase) names to children
     *
     * @param name     The name of the parent node
     * @param children The map of lowercase names to children
     */
    ConfigCommandParentNode(String name, Map<String, ConfigCommandNode> children) {
        super(name);
        this.children = new HashMap<>(children);
    }

    /**
     * Adds a node as a child. E.g., <code>/vsc enginePower basicEnginePower</code>,
     * basicEnginePower is the child of enginePower
     *
     * @param node The node to add as a child.
     */
    void addChild(ConfigCommandNode node) {
        children.put(node.getName().toLowerCase(), node);
    }

    /**
     * Gets a child node by name, case insensitive
     *
     * @param name The name of the child
     * @return The child node, or null if not present
     */
    @Nullable
    ConfigCommandNode getChild(String name) {
        return children.get(name.toLowerCase());
    }

    /**
     * @return the name of the child nodes as a list
     */
    List<String> childrenNames() {
        return children.values().stream().map(ConfigCommandNode::getName).collect(Collectors.toList());
    }

    /**
     * Get a list of the child nodes with names starting with a prefix, case insensitive
     *
     * @param prefix The prefix their names must be starting with, case insensitive
     * @return The list of child nodes with names starting with the prefix
     */
    List<ConfigCommandNode> getChildrenStartingWith(String prefix) {
        return children.values().stream()
            .filter(c -> c.getName().toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
