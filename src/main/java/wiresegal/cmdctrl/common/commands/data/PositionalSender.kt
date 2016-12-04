package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.ICommandSender
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.event.ClickEvent

/**
 * @author WireSegal
 * Created at 11:19 PM on 12/3/16.
 */
class PositionalSender(val parent: ICommandSender, val pos: BlockPos) : ICommandSender by parent {
    override fun getPosition() = pos
    override fun getPositionVector() = Vec3d(pos)

    override fun getName() = pos.run { "$x, $y, $z" }

    override fun getDisplayName(): ITextComponent {
        val comp = TextComponentString(name)
        comp.style.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, pos.run { "/tp $x $y $z" })
        comp.style.insertion = this.name
        return comp
    }
}
