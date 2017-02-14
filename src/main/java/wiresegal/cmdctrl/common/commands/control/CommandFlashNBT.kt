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
            val tiles = TileSelector.matchTiles(server, sender, selector)
            var toThrow: Throwable? = null
            var throws = 0
            for (tile in tiles) {
                val orig = tile.writeToNBT(NBTTagCompound())
                val toNBT = orig.copy()

                if (toNBT.setObject(key, NBTTagDouble(math.toDouble())) && toNBT != orig) {
                    tile.readFromNBT(toNBT)
                    notifyCommandListener(sender, this, "commands.blockdata.success", toNBT)
                } else {
                    toThrow = CommandException("commands.entitydata.failed", toNBT)
                    throws++
                }
            }
            if (toThrow != null && throws == tiles.size)
                throw toThrow
        } else {
            val entity = getEntity(server, sender, selector)
            if (entity is EntityPlayer)
                throw CommandException("commands.entitydata.noPlayers", entity.getDisplayName())

            val orig = entityToNBT(entity)
            val toNBT = orig.copy()

            if (toNBT.setObject(key, NBTTagDouble(math.toDouble())) && toNBT != orig) {
                entity.readFromNBT(toNBT)
                notifyCommandListener(sender, this, "commands.entitydata.success", toNBT)
            } else
                throw CommandException("commands.entitydata.failed", toNBT)
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "flashnbt"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.flashnbt.usage")

    override fun isUsernameIndex(args: Array<String>, index: Int) = index == 0
}
