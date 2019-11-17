package com.best108.atom_animation_reader;

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
