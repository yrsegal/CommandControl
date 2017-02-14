package wiresegal.cmdctrl.common.commands.control

import com.teamwizardry.librarianlib.LibrarianLib
import net.minecraft.command.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.*
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.*

/**
 * @author WireSegal
 * Created at 9:19 AM on 12/14/16.
 */
object CommandFlashNBT : CommandBase() {
    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 3)
            throw CTRLUsageException(getCommandUsage(sender))

        val selector = args[0]
        val key = args[1]
        val math = CommandMath.evaluate(args.slice(2 until args.size).toTypedArray())

        if (TileSelector.isTileSelector(selector)) {
            val tile = TileSelector.matchOne(server, sender, selector) ?: throw CTRLException("commandcontrol.probenbt.notile")

            val orig = tile.writeToNBT(NBTTagCompound())
            val toNBT = orig.copy()

            val k: String
            if (key.startsWith("nbt.")) k = key.removePrefix("nbt.")
            else k = key.removePrefix("nbtliteral.")
            if (toNBT.setObject(k, NBTTagDouble(math.toDouble())) && toNBT != orig) {
                tile.readFromNBT(toNBT)
                notifyCommandListener(sender, this, "commands.blockdata.success", toNBT)
            } else
                throw CommandException("commands.entitydata.failed", toNBT)
        } else {
            val entity = getEntity(server, sender, selector)
            if (entity is EntityPlayer)
                throw CommandException("commands.entitydata.noPlayers", entity.getDisplayName())

            val orig = entityToNBT(entity)
            val toNBT = orig.copy()

            val k: String
            if (key.startsWith("nbt.")) k = key.removePrefix("nbt.")
            else k = key.removePrefix("nbtliteral.")
            if (toNBT.setObject(k, NBTTagDouble(math.toDouble())) && toNBT != orig) {
                entity.readFromNBT(toNBT)
                notifyCommandListener(sender, this, "commands.entitydata.success", toNBT)
            } else
                throw CommandException("commands.entitydata.failed", toNBT)
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "flashnbt"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.flashnbt.usage")
}
