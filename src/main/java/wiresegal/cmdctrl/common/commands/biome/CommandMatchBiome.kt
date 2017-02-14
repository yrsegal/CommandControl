package wiresegal.cmdctrl.common.commands.biome

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import wiresegal.cmdctrl.common.core.notifyCTRLListener

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
object CommandMatchBiome : CommandBase() {

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size > 4) {
            val senderPos = sender.position
            val x1 = parseDouble(senderPos.x.toDouble(), args[0], -3000000, 3000000, false)
            val z1 = parseDouble(senderPos.z.toDouble(), args[1], -3000000, 3000000, false)

            val x2 = parseDouble(senderPos.x.toDouble(), args[2], -3000000, 3000000, false)
            val z2 = parseDouble(senderPos.z.toDouble(), args[3], -3000000, 3000000, false)
            val pos1 = BlockPos(x1, 0.0, z1)
            val pos2 = BlockPos(x2, 0.0, z2)

            val target = CommandSetBiome.parseBiome(args[4])

            val id = Biome.getIdForBiome(target).toByte()
            val name = Biome.REGISTRY.getNameForObject(target)

            val world = sender.entityWorld

            if (world.isBlockLoaded(pos1) && world.isBlockLoaded(pos2)) {
                val matches = BlockPos.getAllInBoxMutable(pos1, pos2).count { world.getBiome(it) == target }
                if (matches > 0) {
                    notifyCTRLListener(sender, this, "commandcontrol.testforbiomes.output", matches, x1.toInt(), z1.toInt(), x2.toInt(), z2.toInt(), id, name)
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, matches)
                } else
                    throw CTRLException("commandcontrol.testforbiomes.output", matches, x1.toInt(), z1.toInt(), x2.toInt(), z2.toInt(), id, name)
            } else
                throw CTRLException("commandcontrol.testforbiomes.range", x1.toInt(), z1.toInt(), x2.toInt(), z2.toInt())
        } else
            throw CTRLUsageException(getCommandUsage(sender))
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1 -> getTabCompletionCoordinate(args, 0, pos)
            2 -> getTabCompletionCoordinate(args, -1, pos)
            3 -> getTabCompletionCoordinate(args, 2, pos)
            4 -> getTabCompletionCoordinate(args, 1, pos)
            5 -> getListOfStringsMatchingLastWord(args, CommandSetBiome.biomes)
            else -> emptyList()
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "testforbiomes"
    override fun getCommandUsage(sender: ICommandSender?) = CommandControl.translate("commandcontrol.testforbiomes.usage")
}
