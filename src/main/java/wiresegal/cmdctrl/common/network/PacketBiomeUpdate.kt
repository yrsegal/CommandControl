package wiresegal.cmdctrl.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import wiresegal.cmdctrl.common.core.Slice

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/3/16.
 */
class PacketBiomeUpdate(var slice: Slice? = null, var id: Byte = 0) : PacketBase() {
    private fun Chunk.setBiome(pos: BlockPos, biome: Biome) {
        val i = pos.x and 15
        val j = pos.z and 15

        val id = Biome.REGISTRY.getIDForObject(biome)

        this.biomeArray[j shl 4 or i] = (id and 255).toByte()
    }

    override fun handle(ctx: MessageContext) {
        slice?.run {
            val world = Minecraft.getMinecraft().theWorld
            val pos = BlockPos(x, 0, z)
            val biome = Biome.getBiome(id.toInt()) ?: return

            world.getChunkFromBlockCoords(pos).setBiome(pos, biome)
            world.markBlockRangeForRenderUpdate(x - 1, 0, z - 1, x + 1, 255, z + 1)
        }
    }

    override fun writeCustomBytes(buf: ByteBuf) {
        slice?.run {
            buf.writeInt(x)
            buf.writeInt(z)
        }
        buf.writeByte(id.toInt())
    }

    override fun readCustomBytes(buf: ByteBuf) {
        val x = buf.readInt()
        val z = buf.readInt()
        slice = Slice(x, z)
        id = buf.readByte()
    }
}
