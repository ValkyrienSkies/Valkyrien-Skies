// This is used to provide extra fields to the CPacketPlayer class, in such a way that its extended
// nested classes have access to setting and getting those extra fields. It would make more sense 
// to put this in the mixins package, but SpongeForge would crash if we did, so its best to leave 
// it here.

package org.valkyrienskies.mod.common.network;

import org.valkyrienskies.mod.common.math.Vector;

public interface IExtendedCPacketPlayer {

    // Returns true if the player is standing on a ship, false if the player isn't.
    boolean hasShipWorldBelowFeet();

    // Gets the EntityID of the PhysicsWrapperEntity the client claims to be standing on.
    int getWorldBelowFeetID();

    // Sets the EntityID of the PhysicsWrapperEntity the client claims to be standing on.
    // TODO: Add some sanity checks to prevent malicious clients from breaking things.
    void setWorldBelowFeetID(int entityID);

    // Sets the local coordinates.
    void setLocalCoords(double localX, double localY, double localZ);

    // Returns a vector copy with the local xyz coordinates with reference to the ship.
    Vector getLocalCoordsVector();

    // Returns the local X coord.
    double getLocalX();

    // Returns the local y coord.
    double getLocalY();

    // Returns the local z coord.
    double getLocalZ();
}
