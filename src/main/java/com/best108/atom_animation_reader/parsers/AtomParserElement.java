package com.best108.atom_animation_reader.parsers;

import java.util.ArrayList;
import java.util.List;

public class AtomParserElement {

	public final String name;
	public final List<String[]> properties;
	public final List<AtomParserElement> branches;
	
	AtomParserElement(String name) {
		this.name = name;
		this.properties = new ArrayList<String[]>();
		this.branches = new ArrayList<AtomParserElement>();
	}
	
	AtomParserElement() {
		this(null);
	}
}
