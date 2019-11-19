package org.valkyrienskies.mod.common.capability.entity_backup;

import net.minecraft.entity.Entity;

public interface ICapabilityEntityBackup {

    /**
     * Creates and stores a backup for the entity that owns this capability.
     *
     * @param entity The entity that owns this capability.
     */
    void backupEntityPosition(Entity entity);

    /**
     * Restores the entity to the backup, and deletes the last backup.
     *
     * @param entity The entity that owns this capability.
     * @throws IllegalStateException Thrown iff there was no entity backup.
     */
    void restoreEntityToBackup(Entity entity) throws IllegalStateException;

    /**
     * @return true if we have a backup for the entity this capability belongs to, false otherwise.
     */
    boolean hasBackupPosition();

}
