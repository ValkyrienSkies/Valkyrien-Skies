package ValkyrienWarfareBase.Capability;

public interface IAirshipCounterCapability {
	
	/**
	 * Returns the player's current airship count
	 * @return
	 */
	int getAirshipCount();
	
	/**
	 * Returns the player's total airship count ever created
	 * @return
	 */
	int getAirshipCountEver();
	
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
	
	/**
	 * Sets the player's total airship ever created count
	 */
	void setAirshipCountEver(int value);
}
