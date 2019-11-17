package org.valkyrienskies.addon.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.valkyrienskies.addon.control.block.multiblocks.IMultiblockSchematic;

public class MultiblockRegistry {

    public static final String EMPTY_SCHEMATIC_ID = "unknown";
    private static final Map<String, IMultiblockSchematic> MULTIBLOCK_ID_MAP = new HashMap<String, IMultiblockSchematic>();
    private static final Map<String, List<IMultiblockSchematic>> MULTIBLOCK_PREFIX_TO_VARIENTS = new HashMap<String, List<IMultiblockSchematic>>();

    public static void registerSchematic(IMultiblockSchematic schematic) {
        String schematicID = schematic.getSchematicID();
        if (MULTIBLOCK_ID_MAP.containsKey(schematicID)) {
            throw new IllegalArgumentException("Duplicate entry for " + schematicID);
        }
        if (schematicID.equals(EMPTY_SCHEMATIC_ID)) {
            throw new IllegalArgumentException(
                "The ID \"" + EMPTY_SCHEMATIC_ID + "\" is reserved!");
        }
        MULTIBLOCK_ID_MAP.put(schematicID, schematic);
    }

    public static IMultiblockSchematic getSchematicByID(String schematicID) {
        if (schematicID.equals(EMPTY_SCHEMATIC_ID)) {
            return null;
        }
        return MULTIBLOCK_ID_MAP.get(schematicID);
    }

    public static List<IMultiblockSchematic> getSchematicsWithPrefix(String schematicPrefix) {
        if (MULTIBLOCK_PREFIX_TO_VARIENTS.containsKey(schematicPrefix)) {
            return MULTIBLOCK_PREFIX_TO_VARIENTS.get(schematicPrefix);
        }
        return new ArrayList<IMultiblockSchematic>();
    }

    static void registerAllPossibleSchematicVariants(Class<? extends IMultiblockSchematic> class1) {
        try {
            // Its a dummy, don't use it for anything that isn't dumb!
            IMultiblockSchematic dummyInstance = class1.newInstance();
            List<IMultiblockSchematic> possibilities = dummyInstance.generateAllVariants();
            for (IMultiblockSchematic schematic : possibilities) {
                registerSchematic(schematic);
            }
            if (MULTIBLOCK_PREFIX_TO_VARIENTS.containsKey(dummyInstance.getSchematicPrefix())) {
                throw new IllegalArgumentException(
                    "Duplicate multiblock prefix registered!\n" + dummyInstance
                        .getSchematicPrefix());
            }
            MULTIBLOCK_PREFIX_TO_VARIENTS.put(dummyInstance.getSchematicPrefix(),
                Collections.unmodifiableList(possibilities));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
