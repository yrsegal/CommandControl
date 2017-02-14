package wiresegal.cmdctrl.common.commands.misc

import com.teamwizardry.librarianlib.LibrarianLib
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer

/**
 * @author WireSegal
 * Created at 7:43 PM on 12/4/16.
 */
object CommandDimension : CommandBase() {

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        notifyCommandListener(sender, this, "commandcontrol.dimension.id", sender.entityWorld.provider.dimension, sender.entityWorld.provider.dimensionType.getName())
        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, sender.entityWorld.provider.dimension)
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "dimension"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.dimension.usage")
}
