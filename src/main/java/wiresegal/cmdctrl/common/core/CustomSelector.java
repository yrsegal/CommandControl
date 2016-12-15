package wiresegal.cmdctrl.common.core;

import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;

import java.util.List;

public class CustomSelector {
    public static <T extends Entity> List<T> parseCustomSelector(ICommandSender sender, String token, Class<? extends T> targetClass) {
        System.out.println("Needle1");
        return Lists.newArrayList();
    }

    public static <T> boolean checkIfCustomSelector(ICommandSender sender, String token, Class<? extends T> targetClass) {
        System.out.println("Needle2");
        return true;
    }
}
