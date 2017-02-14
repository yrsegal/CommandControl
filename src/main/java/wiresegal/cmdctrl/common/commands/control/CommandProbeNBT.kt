package wiresegal.cmdctrl.common.commands.control

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.nbt.NBTPrimitive
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import wiresegal.cmdctrl.common.core.getObject
import wiresegal.cmdctrl.common.core.notifyCTRLListener

/**
 * @author WireSegal
 * Created at 9:19 AM on 12/14/16.
 */
object CommandProbeNBT : CommandBase() {
    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty())
            throw CTRLUsageException(getCommandUsage(sender))
        val selector = args[0]
        val key = if (args.size > 1) args[1] else ""
        val displayKey = if (key.isBlank()) "commandcontrol.probenbt.blank" else key

        val compound: NBTTagCompound

        if (TileSelector.isTileSelector(selector)) {
            val tile = TileSelector.matchOne(server, sender, selector) ?: throw CTRLException("commandcontrol.probenbt.notile")
            compound = tile.writeToNBT(NBTTagCompound())
        } else {
            val entity = getEntity(server, sender, selector) ?: throw CTRLException("commandcontrol.probenbt.noentity")
            compound = entityToNBT(entity)
        }

        val obj = if (key.isNotBlank())
            compound.getObject(key) ?: throw CTRLException("commandcontrol.probenbt.notag", key)
        else compound

        if (obj is NBTPrimitive) sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, obj.int)
        notifyCTRLListener(sender, this, "commandcontrol.probenbt.success", TextComponentTranslation(displayKey), obj)
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "probenbt"
    override fun getCommandUsage(sender: ICommandSender?) = CommandControl.translate("commandcontrol.probenbt.usage")

    override fun isUsernameIndex(args: Array<out String>?, index: Int) = index == 0
}
