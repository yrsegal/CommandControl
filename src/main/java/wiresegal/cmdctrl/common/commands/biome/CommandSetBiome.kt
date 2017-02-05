package wiresegal.cmdctrl.common.commands.biome

import net.minecraft.command.*
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
object CommandSetBiome : CommandBase() {

    val biomes by lazy { Biome.REGISTRY.map { Biome.REGISTRY.getNameForObject(it as Biome) } }

    fun setBiome(chunk: Chunk, pos: BlockPos, biome: Biome) {
        val i = pos.x and 15
        val j = pos.z and 15

        val id = Biome.REGISTRY.getIDForObject(biome)

        chunk.biomeArray[j shl 4 or i] = (id and 255).toByte()
    }

    fun parseBiome(string: String): Biome {
        var biome: Biome?
        try {
            val id = parseInt(string)
            biome = Biome.getBiome(id)
        } catch (e: NumberInvalidException) {
            val rl = ResourceLocation(string)
            biome = Biome.REGISTRY.getObject(rl)
        }

        if (biome == null) throw CommandException("commandcontrol.setbiome.invalid", string)

        return biome
    }

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size > 2) {
            val senderPos = sender.position
            val x = parseDouble(senderPos.x.toDouble(), args[0], -3000000, 3000000, false).toInt()
            val z = parseDouble(senderPos.z.toDouble(), args[1], -3000000, 3000000, false).toInt()
            val pos = BlockPos(x, 0, z)

            val biomeid = args[2]
            val biome = parseBiome(biomeid)

            val world = sender.entityWorld

            val id = Biome.getIdForBiome(biome).toByte()
            val name = Biome.REGISTRY.getNameForObject(biome)

            if (world.isBlockLoaded(pos)) {
                notifyCommandListener(sender, this, "commandcontrol.setbiome.success", x, z, id, name)
                setBiome(world.getChunkFromBlockCoords(pos), pos, biome)
                updateBiomes(world, x..x, z..z)
            } else
                throw CommandException("commandcontrol.setbiome.range", x, z)
        } else
            throw WrongUsageException(getCommandUsage(sender))
    }

    fun updateBiomes(world: World, xRange: IntRange, zRange: IntRange) {
        if (world !is WorldServer) return

        val x1 = Math.min(xRange.first, xRange.last) - 2
        val x2 = Math.max(xRange.first, xRange.last) + 2

        val z1 = Math.min(zRange.first, zRange.last) - 2
        val z2 = Math.max(zRange.first, zRange.last) + 2

        for (chunkX in x1..x2) for (chunkZ in z1..z2) {
            val entry = world.playerChunkMap.getEntry(chunkX, chunkZ)
            val chunk = entry?.chunk
            if (chunk != null && entry != null)
                entry.sendPacket(SPacketChunkData(chunk, 65535))
        }
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        return when (args.size) {
            1 -> getTabCompletionCoordinate(args, 0, pos)
            2 -> getTabCompletionCoordinate(args, -1, pos)
            3 -> getListOfStringsMatchingLastWord(args, biomes)
            else -> emptyList()
        }
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "setbiome"
    override fun getCommandAliases() = mutableListOf("biomeset")
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.setbiome.usage"
}
