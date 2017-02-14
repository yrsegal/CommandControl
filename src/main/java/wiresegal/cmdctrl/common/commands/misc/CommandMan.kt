package wiresegal.cmdctrl.common.commands.misc

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.PlayerNotFoundException
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.man.ManEntry

/**
 * @author WireSegal
 * Created at 5:48 PM on 12/5/16.
 */
object CommandMan : CommandBase() {

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (sender is EntityPlayerMP || sender is MinecraftServer) {
            val key = if (args.isEmpty()) "man" else args[0]
            val command = server.commandManager.commands[key]
            if (command !in server.commandManager.commands.values)
                throw CTRLException("commandcontrol.man.wtfisthis", key)
            if (command !in getSortedPossibleCommands(sender, server))
                throw CTRLException("commandcontrol.man.youaintking", key)

            val manEntry = ManEntry(sender, command, if (args.size > 1) args[1] else null)
            val components = manEntry.asTextComponents
            if (components.isEmpty())
                throw CTRLException("commandcontrol.man.nodocs", key)
            if (sender.sendCommandFeedback()) {
                if (manEntry.hasSub)
                    sender.addChatMessage(TextComponentTranslation("commandcontrol.man.headersub", key, manEntry.subcom))
                else
                    sender.addChatMessage(TextComponentTranslation("commandcontrol.man.header", key))
                components.forEach { sender.addChatMessage(it) }
            }
        } else
            throw PlayerNotFoundException("commandcontrol.man.handsoff")
    }

    fun getSortedPossibleCommands(sender: ICommandSender, server: MinecraftServer)
            = server.getCommandManager().getPossibleCommands(sender).sorted()

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return if (args.size == 1) getListOfStringsMatchingLastWord(args, getSortedPossibleCommands(sender, server).map { it.commandName }) else emptyList()
    }

    override fun getRequiredPermissionLevel() = 0
    override fun getCommandName() = "man"
    override fun getCommandAliases() = listOf("documentation", "manual")
    override fun getCommandUsage(sender: ICommandSender?) = CommandControl.translate("commandcontrol.man.usage")
}
