package ValkyrienWarfareBase.Capability;

public class ImplAirshipCounterCapability implements IAirshipCounterCapability {
	
	protected int airshipCount = 0;

	@Override
	public int getAirshipCount() {
		return this.airshipCount;
	}

	@Override
	public void onCreate() {
		this.airshipCount++;
	}

	@Override
	public void onLose() {
		this.airshipCount--;
	}

	@Override
	public void setAirshipCount(int value) {
		this.airshipCount = value;
	}

}
