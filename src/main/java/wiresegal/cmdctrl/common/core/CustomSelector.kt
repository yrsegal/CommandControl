package wiresegal.cmdctrl.common.core

import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity

object CustomSelector {

    val THIS_MATCHER = "@s(?:\\[\\])".toRegex()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> handleCustomSelector(sender: ICommandSender, token: String, targetClass: Class<out T>): List<T>? {
        if (THIS_MATCHER.matches(token)) {
            val senderEntity = sender.commandSenderEntity
            if (senderEntity != null && targetClass.isInstance(senderEntity))
                return listOf(senderEntity as T)
        }
        return null
        //if("@this" in token && sender.commandSenderEntity != null) listOf(sender.commandSenderEntity!! as T) else null
    }
}
