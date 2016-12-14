package wiresegal.cmdctrl.common.commands.control

import com.udojava.evalex.Expression.ExpressionException
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer

/**
 * @author WireSegal
 * Created at 5:20 PM on 12/14/16.
 */
object CommandMath : CommandBase() {

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw CommandException(getCommandUsage(sender))
        val expression = args.joinToString(" ")
        val exprObj = ExpressionCommand(expression)
        val x = try {
            exprObj.eval()
        } catch (e: ExpressionException) {
            throw CommandException(e.message)
        }

        notifyCommandListener(sender, this, "commandcontrol.math.expr", expression)
        notifyCommandListener(sender, this, "commandcontrol.math.answer", x)
        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, x.toInt())
    }

    override fun getRequiredPermissionLevel() = 0
    override fun getCommandName() = "math"
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.math.usage"
}
