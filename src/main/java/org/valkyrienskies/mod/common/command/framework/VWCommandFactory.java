package org.valkyrienskies.mod.common.command.framework;

import net.minecraft.command.ICommandSender;
import picocli.CommandLine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class VWCommandFactory implements CommandLine.IFactory {
    private ICommandSender sender;

    VWCommandFactory(ICommandSender sender) {
        this.sender = sender;
    }

    @Override
    public <K> K create(Class<K> aClass) {
        //this is very ugly and i'd much rather use porklib unsafe here...
        try {
            Constructor<K> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            K instance = constructor.newInstance();

            for (Field field : aClass.getDeclaredFields())  {
                if (ICommandSender.class.isAssignableFrom(field.getType()))   {
                    field.setAccessible(true);
                    field.set(instance, this.sender);
                }
            }

            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)   {
            throw new IllegalStateException(String.format("Unable to initialize %s!", aClass.getCanonicalName()), e);
        }
    }
}
