package ValkyrienWarfareBase.Capability;

public class ImplAirshipCounterCapability implements IAirshipCounterCapability {
	
	protected int airshipCount = 0;
	protected int airshipCountEver = 0;

	@Override
	public int getAirshipCount() {
		return this.airshipCount;
	}

	@Override
	public int getAirshipCountEver() {
		return airshipCountEver;
	}
	
	@Override
	public void onCreate() {
		this.airshipCount++;
		this.airshipCountEver++;
	}

	@Override
	public void onLose() {
		this.airshipCount--;
	}

	@Override
	public void setAirshipCount(int value) {
		this.airshipCount = value;
	}

	@Override
	public void setAirshipCountEver(int value) {
		this.airshipCountEver = value;
	}

}
