package wiresegal.cmdctrl.common.commands.control

import net.minecraft.command.*
import net.minecraft.nbt.NBTPrimitive
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.getObject

/**
 * @author WireSegal
 * Created at 9:19 AM on 12/14/16.
 */
object CommandProbeNBT : CommandBase() {
    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 2)
            throw WrongUsageException(getCommandUsage(sender))
        val selector = args[0]
        val key = args[1]

        val compound: NBTTagCompound

        if (TileSelector.isTileSelector(selector)) {
            val tile = TileSelector.matchOne(server, sender, selector) ?: throw CommandException("commandcontrol.probenbt.notile")
            compound = tile.writeToNBT(NBTTagCompound())
        } else {
            val entity = getEntity(server, sender, selector) ?: throw CommandException("commandcontrol.probenbt.noentity")
            compound = entityToNBT(entity)
        }

        val obj = compound.getObject(key) ?: throw CommandException("commandcontrol.probenbt.notag", key)

        if (obj is NBTPrimitive) sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, obj.int)
        notifyCommandListener(sender, this, "commandcontrol.probenbt.success", key, obj)
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "probenbt"
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.probenbt.usage"
}
