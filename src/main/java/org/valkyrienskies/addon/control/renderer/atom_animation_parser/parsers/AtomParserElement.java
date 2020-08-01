package org.valkyrienskies.addon.control.renderer.atom_animation_parser.parsers;

import java.util.ArrayList;
import java.util.List;

public class AtomParserElement {

    public final String name;
    public final List<String[]> properties;
    public final List<AtomParserElement> branches;

    AtomParserElement(String name) {
        this.name = name;
        this.properties = new ArrayList<>();
        this.branches = new ArrayList<>();
    }

    AtomParserElement() {
        this(null);
    }
}
