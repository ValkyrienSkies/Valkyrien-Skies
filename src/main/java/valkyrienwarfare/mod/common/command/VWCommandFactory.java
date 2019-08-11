package valkyrienwarfare.mod.common.command;

import net.minecraft.command.ICommandSender;
import picocli.CommandLine;

public class VWCommandFactory implements CommandLine.IFactory {

    ICommandSender sender;

    VWCommandFactory(ICommandSender sender) {
        this.sender = sender;
    }

    @Override
    public <K> K create(Class<K> aClass) throws Exception {
        try {
            return aClass
                    .getDeclaredConstructor(ICommandSender.class)
                    .newInstance(sender);
        } catch (NoSuchMethodException ex) {
            return aClass
                    .getDeclaredConstructor(new Class[]{})
                    .newInstance();
        }
    }

}
