package wiresegal.cmdctrl.common.commands.misc

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import wiresegal.cmdctrl.common.config.ConfigLoader

/**
 * @author WireSegal
 * Created at 5:48 PM on 12/5/16.
 */
object CommandReloadScripts : CommandBase() {

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) = ConfigLoader.loadScripts(server)
    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "reloadcommandscripts"
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.reload.usage"
}
