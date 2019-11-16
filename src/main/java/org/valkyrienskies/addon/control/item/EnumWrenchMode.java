package org.valkyrienskies.addon.control.item;

public enum EnumWrenchMode {
	private String name;

	EnumWrenchMode(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

	// Construct not Assemble because it would prob be mixed up with infuser
	CONSTRUCT("construct"), DECONSTRUCT("deconstruct");
}
