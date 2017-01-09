package wiresegal.cmdctrl.common.core;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;

import java.util.List;

public class CustomSelector {

    public static <T extends Entity> List<T> handleCustomSelector(ICommandSender sender, String token, Class<? extends T> targetClass) {
        return null;
    }
}
