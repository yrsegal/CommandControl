package wiresegal.cmdctrl.common.commands.biome

import com.teamwizardry.librarianlib.LibrarianLib
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import wiresegal.cmdctrl.common.core.notifyCTRLListener

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
object CommandGetBiome : CommandBase() {

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size > 1) {
            val senderPos = sender.position
            val x = parseDouble(senderPos.x.toDouble(), args[0], -3000000, 3000000, false)
            val z = parseDouble(senderPos.z.toDouble(), args[1], -3000000, 3000000, false)
            val pos = BlockPos(x, 0.0, z)


            val world = sender.entityWorld
            if (world.isBlockLoaded(pos)) {
                if (args.size > 2) {
                    val biome = CommandSetBiome.parseBiome(args[2])

                    val id = Biome.getIdForBiome(biome).toByte()
                    val name = Biome.REGISTRY.getNameForObject(biome)

                    val realBiome = world.getBiome(pos)

                    if (biome == realBiome) {
                        notifyCTRLListener(sender, this, "commandcontrol.testforbiome.match", x.toInt(), z.toInt(), id, name)
                        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1)
                    } else {
                        val realId = Biome.getIdForBiome(realBiome).toByte()
                        val realName = Biome.REGISTRY.getNameForObject(realBiome)
                        throw CTRLException("commandcontrol.testforbiome.nomatch", x.toInt(), z.toInt(), realId, realName, id, name)
                    }
                } else {
                    val biome = world.getBiome(pos)

                    val id = Biome.getIdForBiome(biome).toByte()
                    val name = Biome.REGISTRY.getNameForObject(biome)

                    notifyCTRLListener(sender, this, "commandcontrol.testforbiome.success", x.toInt(), z.toInt(), id, name)
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1)
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, Biome.getIdForBiome(biome))
                }
            } else
                throw CTRLException("commandcontrol.testforbiome.range", x.toInt(), z.toInt())
        } else
            throw CTRLUsageException(getCommandUsage(sender))
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1 -> getTabCompletionCoordinate(args, 0, pos)
            2 -> getTabCompletionCoordinate(args, -1, pos)
            3 -> getListOfStringsMatchingLastWord(args, CommandSetBiome.biomes)
            else -> emptyList()
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "testforbiome"
    override fun getCommandUsage(sender: ICommandSender?) = LibrarianLib.PROXY.translate("commandcontrol.testforbiome.usage")
}
