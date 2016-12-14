package wiresegal.cmdctrl.common.core

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.EntityNotFoundException
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import wiresegal.cmdctrl.common.commands.data.TileSelector

/**
 * @author WireSegal
 * Created at 9:56 AM on 12/14/16.
 */
object ScoreExpander {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private val TOKENIZER = "<((?:@[praet](?:\\[?(?:[\\w.=,!-:]*)\\]?))|\\w+)\\.([^>]+)>".toRegex()

    @SubscribeEvent
    fun interceptCommand(e: CommandEvent) {
        e.parameters = e.parameters.map {
            TOKENIZER.replace(it) {
                val selector = it.groupValues[1]
                val key = it.groupValues[2]

                val server = FMLCommonHandler.instance().minecraftServerInstance

                try {
                    if (TileSelector.isTileSelector(selector)) {
                        val tile = TileSelector.matchOne(server, e.sender, selector)
                        if (tile != null) {
                            (ControlSaveData[tile.world].tileData[tile][key] ?: 0).toString()
                        } else throw EntityNotFoundException("commandcontrol.expander.notile")
                    } else {
                        val entity = CommandBase.getEntity(server, e.sender, selector)
                        val scoreboard = server.worldServerForDimension(0).scoreboard
                        val scoreobjective = scoreboard.getObjective(key)
                        if (scoreobjective != null) {
                            val name = if (entity is EntityPlayerMP) entity.getName() else entity.cachedUniqueIdString
                            (scoreboard.getOrCreateScore(name, scoreobjective).scorePoints).toString()
                        } else throw CommandException("commands.scoreboard.objectiveNotFound")
                    }
                } catch (ex: CommandException) {
                    e.sender.addChatMessage(TextComponentTranslation(ex.message, *ex.errorObjects).setStyle(Style().setColor(TextFormatting.RED)))
                    e.isCanceled = true
                    it.value
                }
            }
        }.toTypedArray()
    }
}
