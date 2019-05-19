package valkyrienwarfare.mod.client.render;

import com.best108.atom_animation_reader.IAtomAnimation;
import com.best108.atom_animation_reader.IAtomAnimationBuilder;
import com.best108.atom_animation_reader.IModelRenderer;
import com.best108.atom_animation_reader.impl.BasicAtomAnimationBuilder;
import com.best108.atom_animation_reader.parsers.AtomParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class GibsAnimationRegistry {

    private static final Map<String, IAtomAnimation> ANIMATION_MAP = new HashMap<String, IAtomAnimation>();
    private static final IModelRenderer MODEL_RENDERER = new IModelRenderer() {
        @Override
        public void renderModel(String modelName, int renderBrightness) {
            GibsModelRegistry.renderGibsModel(modelName, renderBrightness);
        }
    };

    public static void registerAnimation(String name, ResourceLocation location) {
        try {
            IResource animationResource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            Scanner data = new Scanner(animationResource.getInputStream());
            AtomParser dataParser = new AtomParser(data);
            IAtomAnimationBuilder animationBuilder = new BasicAtomAnimationBuilder(dataParser);
            // Then register all the models associated with this animation
            Set<String> modelsUsed = animationBuilder.getModelObjsUsed();
            String resourceDomainName = location.getResourceDomain();

            String modelResourceFolder = "";
            String[] temp = location.getResourcePath().split("/");
            for (int i = 1; i < temp.length - 1; i++) {
                modelResourceFolder += temp[i] + "/";
            }

            for (String modelName : modelsUsed) {
                String modelFullPath = modelResourceFolder + modelName;
                ResourceLocation modelToRegister = new ResourceLocation(resourceDomainName, modelResourceFolder + modelName + ".obj");
                GibsModelRegistry.registerGibsModel(modelFullPath, modelToRegister);
            }

            final String modelResourceFolderFinal = modelResourceFolder;
            ANIMATION_MAP.put(name, animationBuilder.build((modelName, renderBrightness) ->
                    GibsModelRegistry.renderGibsModel(modelResourceFolderFinal + modelName, renderBrightness)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static IAtomAnimation getAnimation(String name) {
        return ANIMATION_MAP.get(name);
    }

    public static void onResourceReload() {

    }
}
