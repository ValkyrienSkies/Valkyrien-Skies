package org.valkyrienskies.mod.common.capability.entity_backup;

import net.minecraft.entity.Entity;

public class ImplCapabilityEntityBackup implements ICapabilityEntityBackup {

    private RestorableEntityBackup backup;

    public ImplCapabilityEntityBackup() {
        this.backup = null;
    }

    @Override
    public void backupEntityPosition(Entity entity) {
        backup = new RestorableEntityBackup(entity);
    }

    @Override
    public void restoreEntityToBackup(Entity entity) throws IllegalStateException {
        if (backup == null) {
            throw new IllegalStateException("No backup to restore from!");
        }
        backup.restoreEntityFromBackup(entity);
        backup = null;
    }

    @Override
    public boolean hasBackupPosition() {
        return backup != null;
    }
}
