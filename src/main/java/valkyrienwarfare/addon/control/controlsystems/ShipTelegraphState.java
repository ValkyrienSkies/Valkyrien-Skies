package valkyrienwarfare.addon.control.controlsystems;

public enum ShipTelegraphState {

	AUSSERSTE_0(1.0D),
	VOLLE_KRAFT_0(2.0D),
	HALBE_KRAFT_0(3.0D),
	LANGSAM_0(4.0D),
	MASCHINE_ACH_0(5.0D),
	MASCHINE_FER_0(6.0D),
	HALT(7.0D),
	MASCHINE_ACH_1(8.0D),
	GANZ_LANGSAM(9.0D),
	LANGSAM_1(10.0D),
	HALBE_KRAFT_1(11.0D),
	VOLLE_KRAFT_1(12.0D),
	AUSSERSTE_1(13.0D);

	private double renderRotation;

	private ShipTelegraphState(double renderRotation) {
		this.renderRotation = renderRotation;
	}

	public double getRenderRotation() {
		return renderRotation;
	}
}
