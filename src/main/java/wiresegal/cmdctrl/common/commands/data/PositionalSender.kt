package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.ICommandSender
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * @author WireSegal
 * Created at 11:19 PM on 12/3/16.
 */
class PositionalSender(val parent: ICommandSender, val pos: BlockPos) : ICommandSender by parent {
    override fun getPosition() = pos
    override fun getPositionVector() = Vec3d(pos)
}
