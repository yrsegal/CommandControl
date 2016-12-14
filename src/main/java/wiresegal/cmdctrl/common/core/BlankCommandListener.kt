package wiresegal.cmdctrl.common.core

import net.minecraft.command.ICommand
import net.minecraft.command.ICommandListener
import net.minecraft.command.ICommandSender

/**
 * @author WireSegal
 * Created at 6:09 PM on 12/14/16.
 */
object BlankCommandListener : ICommandListener {
    override fun notifyListener(sender: ICommandSender?, command: ICommand?, flags: Int, translationKey: String?, vararg translationArgs: Any?) {
        // NO-OP
    }
}
