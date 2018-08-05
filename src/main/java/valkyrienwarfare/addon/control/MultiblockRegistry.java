package valkyrienwarfare.addon.control;

import java.util.HashMap;
import java.util.Map;

import valkyrienwarfare.addon.control.block.multiblocks.IMulitblockSchematic;

public class MultiblockRegistry {

	private static final Map<Integer, IMulitblockSchematic> MULTIBLOCK_ID_MAP = new HashMap<Integer, IMulitblockSchematic>();
	
	public static void registerSchematic(int schematicID, IMulitblockSchematic schematic) {
		if (MULTIBLOCK_ID_MAP.containsKey(schematicID)) {
			throw new IllegalArgumentException();
		}
		if (schematicID == -1) {
			throw new IllegalArgumentException("The ID -1 is reserved!");
		}
		schematic.registerMultiblockSchematic(schematicID);
		MULTIBLOCK_ID_MAP.put(schematicID, schematic);
	}
	
	public static IMulitblockSchematic getSchematicByID(int schematicID) {
		if (schematicID == -1) {
			return null;
		}
		return MULTIBLOCK_ID_MAP.get(schematicID);
	}
}
