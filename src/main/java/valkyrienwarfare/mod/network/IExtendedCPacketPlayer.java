/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

// This is used to provide extra fields to the CPacketPlayer class, in such a way that its extended
// nested classes have access to setting and getting those extra fields. It would make more sense 
// to put this in the mixins package, but SpongeForge would crash if we did, so its best to leave 
// it here.

package valkyrienwarfare.mod.network;

import valkyrienwarfare.api.Vector;

public interface IExtendedCPacketPlayer {

    // Returns true if the player is standing on a ship, false if the player isn't.
    boolean hasShipWorldBelowFeet();
    
    // Sets the EntityID of the PhysicsWrapperEntity the client claims to be standing on.
    // TODO: Add some sanity checks to prevent malicious clients from breaking things.
    void setWorldBelowFeetID(int entityID);
    
    // Gets the EntityID of the PhysicsWrapperEntity the client claims to be standing on.
    int getWorldBelowFeetID();
    
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
