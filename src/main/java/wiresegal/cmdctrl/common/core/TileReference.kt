package wiresegal.cmdctrl.common.core

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * @author WireSegal
 * Created at 5:36 PM on 12/3/16.
 */
data class TileReference(val id: String, val pos: BlockPos) {
    constructor(id: String, pos: Long) : this(id, BlockPos.fromLong(pos))

    operator fun get(world: World): TileEntity? {
        val te = world.getTileEntity(pos) ?: return null
        val nbt = te.writeToNBT(NBTTagCompound())
        if (nbt.getString("id") == id) return te
        return null
    }
}
