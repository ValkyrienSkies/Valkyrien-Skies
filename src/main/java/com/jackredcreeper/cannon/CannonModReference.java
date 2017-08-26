package com.jackredcreeper.cannon;

public class CannonModReference {

	public static final String MOD_ID = "cannons";
	public static final String NAME = "Cannons!";
	public static final String VERSION = "1";

	public static final String CLIENT = "com.jackredcreeper.cannon.proxy.ClientProxy";
	public static final String SERVER = "com.jackredcreeper.cannon.proxy.ServerProxy";

	public static enum ModItems {
		PRIMER("primer", "itemprimer"),
		LOADER("loader", "itemloader"),
		TUNER("tuner", "itemtuner"),
		CANNONBALL("cannonball", "itemcannonball"),
		EXPLOSIVEBALL("explosiveball", "itemexplosiveball"),
		GRAPESHOT("grapeshot", "itemgrapeshot"),
		SOLIDBALL("solidball", "itemsolidball");

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
		CANNON("cannon", "blockcannon"),
		AIRMINE("airmine", "blockairmine");

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
