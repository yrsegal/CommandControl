package wiresegal.cmdctrl.mixins;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wiresegal.cmdctrl.common.core.CustomSelector;

import java.util.List;

@Mixin(EntitySelector.class)
public abstract class MixinEntitySelector {
    @Inject(method = "func_179656_b", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void checkForCustom(ICommandSender sender, String token, Class<? extends T> targetClass, CallbackInfoReturnable<List<T>> cb) {
        List<T> custom = CustomSelector.handleCustomSelector(sender, token, targetClass);
        if (custom != null) cb.setReturnValue(custom);
    }
}
