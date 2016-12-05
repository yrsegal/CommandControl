package wiresegal.cmdctrl.common.config

import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World

/**
 * @author WireSegal
 * Created at 5:10 PM on 12/4/16.
 */
class ModuleSender(private val module: CommandModule, private val world: World?, private val server: MinecraftServer, private val debug: Boolean = false, private val pos: BlockPos? = null) : ICommandSender {
    override fun sendCommandFeedback() = debug && server.worldServers[0].gameRules.getBoolean("sendCommandFeedback")
    override fun getName() = "Scripting"
    override fun getDisplayName() = TextComponentString(name)
    override fun canCommandSenderUseCommand(permLevel: Int, commandName: String?) = true
    override fun getPosition() = pos ?: BlockPos.ORIGIN
    override fun getEntityWorld() = world ?: server.worldServers[0]
    override fun setCommandStat(type: CommandResultStats.Type, amount: Int) = module.stats[type]?.set(type, amount, this) ?: Unit
    override fun getPositionVector() = if (pos == null) Vec3d.ZERO else Vec3d(pos)
    override fun getCommandSenderEntity() = null
    override fun getServer() = server

    override fun addChatMessage(component: ITextComponent) {
        val prefix = TextComponentString("[")
        server.addChatMessage(prefix.appendSibling(displayName).appendText(": ").appendSibling(component).appendText("]"))
    }
}
