/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
