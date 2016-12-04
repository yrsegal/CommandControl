package wiresegal.cmdctrl.common.config

import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString

/**
 * @author WireSegal
 * Created at 5:10 PM on 12/4/16.
 */
class ModuleSender(val module: CommandModule, private val server: MinecraftServer) : ICommandSender {
    override fun sendCommandFeedback() = server.worldServers[0].gameRules.getBoolean("sendCommandFeedback")
    override fun getName() = "Scripting"
    override fun getDisplayName() = TextComponentString(name)
    override fun canCommandSenderUseCommand(permLevel: Int, commandName: String?) = true
    override fun getPosition() = BlockPos.ORIGIN
    override fun getEntityWorld() = server.worldServers[0]
    override fun setCommandStat(type: CommandResultStats.Type, amount: Int) = module.stats[type]?.set(type, amount, this) ?: Unit
    override fun getPositionVector() = Vec3d.ZERO
    override fun getCommandSenderEntity() = null
    override fun getServer() = server

    override fun addChatMessage(component: ITextComponent) {
        val prefix = TextComponentString("[")
        server.logInfo(prefix.appendSibling(displayName).appendText(": ").appendSibling(component).appendText("]").unformattedText)
    }
}
