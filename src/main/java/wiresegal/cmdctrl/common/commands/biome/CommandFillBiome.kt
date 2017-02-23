package wiresegal.cmdctrl.common.commands.biome

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import wiresegal.cmdctrl.common.core.Slice
import wiresegal.cmdctrl.common.core.notifyCTRLListener

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
object CommandFillBiome : CommandBase() {

    val biomes: List<ResourceLocation?>
        get() = CommandSetBiome.biomes

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size > 4) {
            val senderPos = sender.position
            val x1 = parseDouble(senderPos.x.toDouble(), args[0], -3000000, 3000000, false).toInt()
            val z1 = parseDouble(senderPos.z.toDouble(), args[1], -3000000, 3000000, false).toInt()

            val x2 = parseDouble(senderPos.x.toDouble(), args[2], -3000000, 3000000, false).toInt()
            val z2 = parseDouble(senderPos.z.toDouble(), args[3], -3000000, 3000000, false).toInt()
            val pos1 = BlockPos(x1, 0, z1)
            val pos2 = BlockPos(x2, 0, z2)

            val biomeid = args[4]
            val biome = CommandSetBiome.parseBiome(biomeid)

            val world = sender.entityWorld

            val id = Biome.getIdForBiome(biome).toByte()
            val name = Biome.REGISTRY.getNameForObject(biome)

            if (world.isBlockLoaded(pos1) && world.isBlockLoaded(pos2)) {
                notifyCTRLListener(sender, this, "commandcontrol.fillbiomes.success", x1, z1, x2, z2, id, name)
                val slices = BlockPos.getAllInBoxMutable(pos1, pos2)
                        .filter { CommandSetBiome.setBiome(world.getChunkFromBlockCoords(it), it, biome) }
                        .map(::Slice)
                CommandSetBiome.updateBiomes(world, slices)
            } else
                throw CTRLException("commandcontrol.fillbiomes.range", x1, z1, x2, z2)
        } else
            throw CTRLUsageException(getCommandUsage(sender))
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1 -> getTabCompletionCoordinate(args, 0, pos)
            2 -> getTabCompletionCoordinate(args, -1, pos)
            3 -> getTabCompletionCoordinate(args, 2, pos)
            4 -> getTabCompletionCoordinate(args, 1, pos)
            5 -> getListOfStringsMatchingLastWord(args, biomes)
            else -> emptyList()
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "fillbiomes"
    override fun getCommandAliases() = mutableListOf("biomefill")
    override fun getCommandUsage(sender: ICommandSender?) = CommandControl.translate("commandcontrol.fillbiomes.usage")
}
