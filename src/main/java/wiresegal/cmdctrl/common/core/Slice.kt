package wiresegal.cmdctrl.common.core

import net.minecraft.util.math.BlockPos

/**
 * @author WireSegal
 * Created at 4:28 PM on 12/3/16.
 */
class Slice(x: Number, z: Number) : BlockPos(x.toInt(), 0, z.toInt()) {

    constructor(pos: BlockPos) : this(pos.x, pos.z)
    constructor(pos: Long) : this(fromLong(pos))

    override fun toString() = "[$x, $z]"
}
