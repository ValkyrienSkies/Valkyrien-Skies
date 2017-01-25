package com.jackredcreeper.cannon;

public class Reference {

	public static final String MOD_ID = "can";
	public static final String NAME = "Cannons!";
	public static final String VERSION = "0.7";
			
	public static final String CLIENT = "com.jackredcreeper.cannon.proxy.ClientProxy";
	public static final String SERVER = "com.jackredcreeper.cannon.proxy.ServerProxy";
	
	public static enum ModItems {
		PRIMER("PRIMER","ItemPrimer"),
		LOADER("LOADER","ItemLoader"),
		TUNER("TUNER","ItemTuner"),
		CANNONBALL("CANNONBALL","ItemCannonball"),
		EXPLOSIVEBALL("EXPLOSIVEBALL","ItemExplosiveball");
		
		private String unlocalizedName;
		private String registryName;
		
		ModItems(String unlocalizedName, String registryName) {
			this.unlocalizedName = unlocalizedName;
			this.registryName = registryName;
			
		}
		
		public String getUnlocalizedName() {
			return unlocalizedName;
		}
		
		public String getRegistryName() {
			return registryName;
		}
	}	
	
	public static enum ModBlocks {
		CANNON("CANNON","BlockCannon");
		
		private String unlocalizedName;
		private String registryName;
		
		ModBlocks(String unlocalizedName, String registryName) {
			this.unlocalizedName = unlocalizedName;
			this.registryName = registryName;
			
		}
		
		public String getUnlocalizedName() {
			return unlocalizedName;
		}
		
		public String getRegistryName() {
			return registryName;
		}	
	}
	
	
	
}
