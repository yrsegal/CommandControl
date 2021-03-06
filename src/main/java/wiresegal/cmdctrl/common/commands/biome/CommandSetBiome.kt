package wiresegal.cmdctrl.common.commands.biome

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.NumberInvalidException
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.CTRLUsageException
import wiresegal.cmdctrl.common.core.Slice
import wiresegal.cmdctrl.common.core.notifyCTRLListener

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
object CommandSetBiome : CommandBase() {

    val biomes by lazy { Biome.REGISTRY.map { Biome.REGISTRY.getNameForObject(it as Biome) } }

    fun parseBiome(string: String): Biome {
        var biome: Biome?
        try {
            val id = parseInt(string)
            biome = Biome.getBiome(id)
        } catch (e: NumberInvalidException) {
            val rl = ResourceLocation(string)
            biome = Biome.REGISTRY.getObject(rl)
        }

        if (biome == null) throw CTRLException("commandcontrol.setbiome.invalid", string)

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
                notifyCTRLListener(sender, this, "commandcontrol.setbiome.success", x, z, id, name)
                if (setBiome(world.getChunkFromBlockCoords(pos), pos, biome))
                    updateBiomes(world, x..x, z..z)
            } else
                throw CTRLException("commandcontrol.setbiome.range", x, z)
        } else
            throw CTRLUsageException(getCommandUsage(sender))
    }

    fun updateBiomes(world: World, slices: List<Slice>) {
        if (world !is WorldServer) return

        val validSlices = slices
                .flatMap {
                    Array(25) { i ->
                        val x = (i % 5) - 2 + it.x
                        val z = (i / 5) - 2 + it.z
                        Slice(x, z)
                    }.toList()
                }
                .map { (it.x shr 4) to (it.z shr 4) }
                .toSet()
        for ((chunkX, chunkZ) in validSlices) forceChunk(world, chunkX, chunkZ)
    }

    fun updateBiomes(world: World, xRange: IntRange, zRange: IntRange) {
        if (world !is WorldServer) return

        val x1 = (Math.min(xRange.first, xRange.last) - 2) shr 4
        val x2 = (Math.max(xRange.first, xRange.last) + 2) shr 4

        val z1 = (Math.min(zRange.first, zRange.last) - 2) shr 4
        val z2 = (Math.max(zRange.first, zRange.last) + 2) shr 4

        for (chunkX in x1..x2) for (chunkZ in z1..z2) forceChunk(world, chunkX, chunkZ)
    }

    fun forceChunk(world: WorldServer, chunkX: Int, chunkZ: Int) {
        val entry = world.playerChunkMap.getEntry(chunkX, chunkZ)
        val chunk = entry?.chunk
        if (chunk != null && entry != null)
            entry.sendPacket(SPacketChunkData(chunk, 65535))
    }

    fun setBiome(chunk: Chunk, pos: BlockPos, biome: Biome): Boolean {
        val i = pos.x and 15
        val j = pos.z and 15

        val id = Biome.REGISTRY.getIDForObject(biome)

        val prev = chunk.biomeArray[j shl 4 or i]
        if (prev.toInt() == id) return false
        chunk.biomeArray[j shl 4 or i] = (id and 255).toByte()
        return true
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
    override fun getCommandUsage(sender: ICommandSender?) = CommandControl.translate("commandcontrol.setbiome.usage")
}
