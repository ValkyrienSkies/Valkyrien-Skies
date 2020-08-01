package org.valkyrienskies.addon.control.renderer.atom_animation_parser;

/**
 * Must be implemented by the client. This is what the AtomAnimation uses to render its models.
 *
 * @author thebest108
 */
public interface IModelRenderer {

    /**
     * This render method must render the model at the origin.
     */
    void renderModel(String modelName, int renderBrightness);
}
