package org.valkyrienskies.mod.common.command.framework;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.minecraft.command.ICommandSender;
import picocli.CommandLine;

public class VWCommandFactory implements CommandLine.IFactory {

    private Injector injector;
    private ICommandSender sender;

    VWCommandFactory(ICommandSender sender) {
        this.sender = sender;
        this.injector = Guice.createInjector(new CommandModule());
    }

    @Override
    public <K> K create(Class<K> aClass) {
        return injector.getInstance(aClass);
    }

    private class CommandModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ICommandSender.class).toInstance(sender);
        }

    }

}
