package org.valkyrienskies.addon.control.renderer.atom_animation_parser;

/**
 * Completely immutable, allows the client to render the animation at a given keyframe.
 *
 * @author thebest108
 */
public interface IAtomAnimation {

    /**
     * Renders the animation at the given keyframe. If the Atom file does not provide information
     * for every possible keyframe, this method will interpolate between the two closest keyframes.
     *
     * @param keyframe must be greater or equal to getMinKeyframe() and less than or equal to
     *                 getMaxKeyframe(). If not then an IllegalArgumentException is thrown.
     */
    void renderAnimation(double keyframe, int brightness);

    /**
     * @return the lowest possible value for a keyframe.
     */
    double getMinKeyframe();

    /**
     * @return the greatest possible value for a keyframe.
     */
    double getMaxKeyframe();

    /**
     * Renders the animation node with the given model name. If there is no node with the given
     * model name, then nothing is rendered.
     */
    void renderAnimationNode(String nodeName, double keyframe, int brightness);
}
