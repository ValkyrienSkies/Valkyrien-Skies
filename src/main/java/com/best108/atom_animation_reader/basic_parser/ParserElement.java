package com.best108.atom_animation_reader.basic_parser;

import java.util.ArrayList;
import java.util.List;

public class ParserElement {

	public final String name;
	public final List<String[]> properties;
	public final List<ParserElement> branches;
	
	ParserElement(String name) {
		this.name = name;
		this.properties = new ArrayList<String[]>();
		this.branches = new ArrayList<ParserElement>();
	}
	
	ParserElement() {
		this(null);
	}
}
