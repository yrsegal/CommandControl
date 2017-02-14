package wiresegal.cmdctrl.common.core

import net.minecraft.command.*
import net.minecraft.util.text.ITextComponent
import wiresegal.cmdctrl.common.CommandControl

/**
 * @author WireSegal
 * Created at 6:20 PM on 2/13/17.
 */
class CTRLException(key: String, vararg args: Any?) : CommandException(CommandControl.translate(key, *args.map { if (it is ITextComponent) it.formattedText else it }.toTypedArray()))

class CTRLUsageException(key: String, vararg args: Any?) : WrongUsageException(CommandControl.translate(key, *args.map { if (it is ITextComponent) it.formattedText else it }.toTypedArray()))

fun notifyCTRLListener(sender: ICommandSender, command: ICommand, translationKey: String, vararg translationArgs: Any?) {
    CommandBase.notifyCommandListener(sender, command, CommandControl.translate(translationKey, *translationArgs.map { if (it is ITextComponent) it.formattedText else it }.toTypedArray()))
}
