package org.valkyrienskies.mod.client.render;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.List;

public interface ITileEntitiesToRenderProvider {

    @Nullable
    List<TileEntity> getTileEntitiesToRender(int chunkExtendedDataIndex);
}
