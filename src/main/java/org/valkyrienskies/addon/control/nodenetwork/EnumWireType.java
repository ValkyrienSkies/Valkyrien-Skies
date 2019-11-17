package org.valkyrienskies.addon.control.nodenetwork;

public enum EnumWireType {
	RELAY("relay_wire"), VANISHING("vanishing_wire");

	private String name;

	EnumWireType(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
