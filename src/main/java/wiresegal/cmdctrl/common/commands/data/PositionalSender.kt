package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.ICommandSender
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.event.ClickEvent

/**
 * @author WireSegal
 * Created at 11:19 PM on 12/3/16.
 */
class TileSender(val parent: ICommandSender, val tile: TileEntity) : ICommandSender by parent {
    override fun getPosition() = tile.pos
    override fun getPositionVector() = Vec3d(tile.pos)

    override fun getName(): String {
        val display = tile.displayName
        return display?.formattedText ?: TileSelector.classToNameMap[tile.javaClass] ?: "INVALID TILE"
    }

    override fun getDisplayName(): ITextComponent {
        val comp = TextComponentString(name)
        comp.style.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tile.pos.run { "/tp $x $y $z" })
        comp.style.insertion = this.name
        return comp
    }
}
