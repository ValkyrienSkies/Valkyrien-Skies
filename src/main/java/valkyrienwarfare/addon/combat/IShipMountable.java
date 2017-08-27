package valkyrienwarfare.addon.combat;

import valkyrienwarfare.api.Vector;

public interface IShipMountable {

	public Vector getPositionInLocal();

	public boolean isMounting();
}
