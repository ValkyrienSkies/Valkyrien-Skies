package com.jackredcreeper.cannon;

public class CannonModRefrence {

	public static final String MOD_ID = "cannons";
	public static final String NAME = "Cannons!";
	public static final String VERSION = "1";

	public static final String CLIENT = "com.jackredcreeper.cannon.proxy.ClientProxy";
	public static final String SERVER = "com.jackredcreeper.cannon.proxy.ServerProxy";

	public static enum ModItems {
		PRIMER("PRIMER","itemprimer"),
		LOADER("LOADER","itemloader"),
		TUNER("TUNER","itemtuner"),
		CANNONBALL("CANNONBALL","itemcannonball"),
		EXPLOSIVEBALL("EXPLOSIVEBALL","itemexplosiveball"),
		GRAPESHOT("GRAPESHOT","itemgrapeshot"),
		SOLIDBALL("SOLIDBALL","itemsolidball");

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
		CANNON("CANNON","blockcannon");

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
