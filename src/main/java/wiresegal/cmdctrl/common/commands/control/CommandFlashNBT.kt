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
        if (args.size < 2)
            throw CTRLUsageException(getCommandUsage(sender))

        val math = CommandMath.evaluate(args.slice(1 until args.size).toTypedArray())

        val match = ScoreExpander.NON_POSITION.matchEntire(args[0]) ?: throw CTRLUsageException(getCommandUsage(sender))
        val selector = match.groupValues[1]
        val key = match.groupValues[2]
        val entity = getEntity(server, sender, selector)
        if (entity is EntityPlayer)
            throw CommandException("commands.entitydata.noPlayers", entity.getDisplayName())

        val orig = entityToNBT(entity)
        val toNBT = orig.copy()

        if (key.startsWith("nbt.") || key.startsWith("nbtliteral.")) {
            val k: String
            if (key.startsWith("nbt.")) k = key.removePrefix("nbt.")
            else k = key.removePrefix("nbtliteral.")
            if (toNBT.setObject(k, NBTTagDouble(math.toDouble())) && toNBT != orig) {
                entity.readFromNBT(toNBT)
                notifyCommandListener(sender, this, "commands.entitydata.success", toNBT)
            } else
                throw CommandException("commands.entitydata.failed", toNBT)
        } else {
            val scoreboard = server.worldServerForDimension(0).scoreboard
            val scoreobjective = scoreboard.getObjective(key)
            if (scoreobjective != null) {
                val name = if (entity is EntityPlayerMP) entity.getName() else entity.cachedUniqueIdString
                val score = math.toInt()
                scoreboard.getOrCreateScore(name, scoreobjective).scorePoints = score
                notifyCommandListener(sender, this, "commands.scoreboard.players.set.success", scoreobjective.name, score, name)

            } else throw CommandException("commands.scoreboard.objectiveNotFound", key)
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "flashnbt"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.flashnbt.usage")
}
