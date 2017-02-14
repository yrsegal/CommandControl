package wiresegal.cmdctrl.common.commands.control

import com.teamwizardry.librarianlib.LibrarianLib
import com.udojava.evalex.Expression.ExpressionException
import net.minecraft.command.*
import net.minecraft.server.MinecraftServer
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import java.math.BigDecimal

/**
 * @author WireSegal
 * Created at 5:20 PM on 12/14/16.
 */
object CommandMath : CommandBase() {

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) throw CTRLUsageException(getCommandUsage(sender))
        val expression = args.joinToString(" ")
        val x = evaluate(args)

        notifyCommandListener(sender, this, "commandcontrol.math.expr", expression)
        val formattedX: Number = if (x.toLong().toDouble() == x.toDouble()) x.toLong() else x.toDouble()
        notifyCommandListener(sender, this, "commandcontrol.math.answer", formattedX)
        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, x.toInt())
    }

    fun evaluate(args: Array<out String>): BigDecimal {
        val expression = args.joinToString(" ")
        val exprObj = ExpressionCommand(expression)
        return try {
            exprObj.eval()
        } catch (e: ExpressionException) {
            throw CommandException(e.message)
        } catch (e: NumberFormatException) {
            throw CTRLException("commandcontrol.math.invalidnumber")
        }
    }

    override fun getRequiredPermissionLevel() = 0
    override fun getCommandName() = "math"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.math.usage")
}
