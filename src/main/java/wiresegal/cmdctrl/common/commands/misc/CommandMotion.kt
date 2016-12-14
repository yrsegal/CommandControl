package wiresegal.cmdctrl.common.commands.misc

import com.teamwizardry.librarianlib.common.network.PacketHandler
import net.minecraft.command.*
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import wiresegal.cmdctrl.common.network.PacketMotionUpdate

/**
 * @author WireSegal
 * Created at 7:43 PM on 12/4/16.
 */
object CommandMotion : CommandBase() {

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 3) throw WrongUsageException(getCommandUsage(sender))

        val entity = if (args.size > 3) getEntity(server, sender, args[3]) else getCommandSenderAsPlayer(sender)

        val x = parseExpandedRelative(entity.motionX, args[0])
        val y = parseExpandedRelative(entity.motionY, args[1])
        val z = parseExpandedRelative(entity.motionZ, args[2])

        entity.motionX = x
        entity.motionY = y
        entity.motionZ = z

        if (entity is EntityPlayerMP)
            PacketHandler.NETWORK.sendTo(PacketMotionUpdate(entity.motionX, entity.motionY, entity.motionZ), entity)

        notifyCommandListener(sender, this, "commandcontrol.motion.success", entity.name, entity.motionX, entity.motionY, entity.motionZ)
    }

    fun parseExpandedRelative(current: Double, token: String): Double {
        val flag = token.startsWith("~")
        if (flag) {
            if (token.length == 1) return current
            else return when (token[1]) {
                '*' -> {
                    val double = parseDouble(token.substring(2))
                    current * double
                }
                '/' -> {
                    val double = parseDouble(token.substring(2))
                    current / double
                }
                '-' -> {
                    val double = parseDouble(token.substring(2))
                    current - double
                }
                '+' -> {
                    val double = parseDouble(token.substring(2))
                    current + double
                }
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> {
                    val double = parseDouble(token.substring(1))
                    current + double
                }
                else -> throw NumberInvalidException("commands.generic.num.invalid", token)
            }
        }
        return parseDouble(token)
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "motion"
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.motion.usage"
}
