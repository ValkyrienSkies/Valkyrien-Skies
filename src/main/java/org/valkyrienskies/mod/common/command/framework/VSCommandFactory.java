package org.valkyrienskies.mod.common.command.framework;

import net.minecraft.command.ICommandSender;
import org.valkyrienskies.mod.common.config.VSConfig;
import picocli.CommandLine;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class VSCommandFactory implements CommandLine.IFactory {
    private ICommandSender sender;

    VSCommandFactory(ICommandSender sender) {
        this.sender = sender;
    }

    @Override
    public <K> K create(Class<K> aClass) {
        try {
            K instance;

            if (VSConfig.showAnnoyingDebugOutput) {
                System.out.println("PicoCLI is instantiating " + aClass.getName());
            }

            // Instantiate constructor with ICommandSender if applicable
            try {
                Constructor<K> constructor = aClass.getDeclaredConstructor(ICommandSender.class);
                constructor.setAccessible(true);
                instance = constructor.newInstance(sender);
            } catch (NoSuchMethodException ex) {
                Constructor<K> constructor = aClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance();
            }

            for (Field field : aClass.getDeclaredFields())  {
                if (ICommandSender.class.isAssignableFrom(field.getType()) &&
                    field.getAnnotation(Inject.class) != null) {
                    field.setAccessible(true);
                    field.set(instance, this.sender);
                }
            }

            return instance;
        } catch (NoSuchMethodException | InstantiationException |
            IllegalAccessException | InvocationTargetException e)   {
            throw new IllegalStateException(
                String.format("Unable to initialize %s!", aClass.getCanonicalName()), e);
        }
    }
}
