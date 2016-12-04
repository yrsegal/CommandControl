package wiresegal.cmdctrl.common.core

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

/**
 * @author WireSegal
 * Created at 4:28 PM on 12/3/16.
 */
data class Slice(val x: Int, val z: Int) {

    companion object {
        private val NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30000000)) // Bits for a single coord
        private val NUM_Z_BITS = 64 - NUM_X_BITS
        private val Z_SHIFT = 0
        private val X_SHIFT = Z_SHIFT + NUM_Z_BITS
        private val X_MASK = (1L shl NUM_X_BITS) - 1L
        private val Z_MASK = (1L shl NUM_Z_BITS) - 1L
    }

    constructor(x: Number, z: Number) : this(x.toInt(), z.toInt())

    constructor(pos: Long) : this((pos shl 64 - X_SHIFT - NUM_X_BITS shr 64 - NUM_X_BITS).toInt(),
                                  (pos shl 64 - Z_SHIFT - NUM_Z_BITS shr 64 - NUM_Z_BITS).toInt())

    constructor(pos: BlockPos): this(pos.x, pos.z)

    fun toLong(): Long {
        return this.x.toLong() and X_MASK shl X_SHIFT or (this.z.toLong() and Z_MASK shl Z_SHIFT)
    }
}
