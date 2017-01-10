package wiresegal.cmdctrl.common.core

import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity

object CustomSelector {

    @JvmStatic
    fun <T : Entity> handleCustomSelector(sender: ICommandSender, token: String, targetClass: Class<out T>): List<T>? {
        return null
        //if("@this" in token && sender.commandSenderEntity != null) listOf(sender.commandSenderEntity!! as T) else null
    }
}
