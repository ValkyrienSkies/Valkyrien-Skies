package ValkyrienWarfareBase.Capability;

public interface IAirshipCounterCapability {
	
	/**
	 * Returns the player's airhsip count
	 * @return
	 */
	int getAirshipCount();
	
	/**
	 * Adds one to the player's airship count
	 */
	void onCreate();
	
	/**
	 * Removes one from the player's airship count
	 */
	void onLose();
	
	/**
	 * Sets the player's airship count
	 */
	void setAirshipCount(int value);
}
