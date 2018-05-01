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

package valkyrienwarfare.addon.control.controlsystems;

public enum ShipTelegraphState {

    ÄUSSERSTE_0(1.0D),
    VOLLE_KRAFT_0(2.0D),
    HALBE_KRAFT_0(3.0D),
    LANGSAM_0(4.0D),
    MASCHINE_ACH_0(5.0D),
    MASCHINE_FER_0(6.0D),
    HALT(7.0D),
    MASCHINE_ACH_1(8.0D),
    GANZ_LANGSAM(9.0D),
    LANGSAM_1(10.0D),
    HALBE_KRAFT_1(11.0D),
    VOLLE_KRAFT_1(12.0D),
    ÄUSSERSTE_1(13.0D);

    private double renderRotation;

    private ShipTelegraphState(double renderRotation) {
        this.renderRotation = renderRotation;
    }

    public double getRenderRotation() {
        return renderRotation;
    }
}
