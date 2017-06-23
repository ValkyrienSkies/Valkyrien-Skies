package ValkyrienWarfareControl.ControlSystems;

public enum ShipTelegraphStateEnum {

	Position(1.0D);

	private double renderRotation;

	private ShipTelegraphStateEnum(double renderRotation) {
		this.renderRotation = renderRotation;
	}

	public double getRenderRotation() {
		return renderRotation;
	}
}
