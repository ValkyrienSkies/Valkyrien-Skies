package org.valkyrienskies.mod.common.physics.collision;

import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;

public class btVSCompoundShape extends btCompoundShape {

    public btVSCompoundShape(boolean enableDynamicAabbTree) {
        super(enableDynamicAabbTree);
        this.children.ordered = false;
    }

}
