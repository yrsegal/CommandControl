package wiresegal.cmdctrl.common.core

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.EntityNotFoundException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTPrimitive
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagString
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
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
object ScoreExpander : CommandBase() {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private const val SELECTOR = "(?:@[praet](?:\\[(?:[\\w.=,!-:]*)\\])?)"
    private const val NAME = "\\w+"
    private const val POSITION_PATTERN = "\\[\\d+\\.\\d+(?:\\.\\d+)?\\]"
    private const val TOKENIZER_PATTERN = "<($SELECTOR|$NAME|$POSITION_PATTERN)\\.([^>]+)>"
    private const val COMPRESSOR_PATTERN = "<<((?:$SELECTOR|$NAME|$POSITION_PATTERN)\\.(?:[^>]+))>>"
    private const val DECOMPRESSOR_PATTERN = "  ((?:$SELECTOR|$NAME|$POSITION_PATTERN)\\.(?:[^ >]+))  "

    val POSITION = POSITION_PATTERN.toRegex()
    val TOKENIZER = TOKENIZER_PATTERN.toRegex()
    val COMPRESSOR = COMPRESSOR_PATTERN.toRegex()
    val DECOMPRESSOR = DECOMPRESSOR_PATTERN.toRegex()

    @SubscribeEvent
    fun interceptCommand(e: CommandEvent) {
        e.parameters = e.parameters.map {
            DECOMPRESSOR.replace(TOKENIZER.replace(COMPRESSOR.replace(it) {
                "  " + it.groupValues[1] + "  "
            }) {
                val selector = it.groupValues[1]
                val key = it.groupValues[2]

                val server = FMLCommonHandler.instance().minecraftServerInstance

                try {
                    if (selector == "world") {
                        (ControlSaveData[e.sender.entityWorld].worldData[key] ?: 0).toString()
                    } else if (selector == "global") {
                        (ControlSaveData.globalWorldData.globalData[key] ?: 0).toString()
                    } else if (POSITION.matches(selector)) {
                        val numbers = selector.removePrefix("[").removeSuffix("]").split(".")
                        val data = ControlSaveData[e.sender.entityWorld]
                        if (numbers.size == 2) {
                            val pos = Slice(numbers[0].toInt(), numbers[1].toInt())
                            (data.sliceData[pos][key] ?: 0).toString()
                        } else {
                            val pos = BlockPos(numbers[0].toInt(), numbers[1].toInt(), numbers[2].toInt())
                            (data.posData[pos][key] ?: 0).toString()
                        }
                    } else if (TileSelector.isTileSelector(selector)) {
                        val tile = TileSelector.matchOne(server, e.sender, selector)
                        if (tile != null) {
                            if (key.startsWith("nbt.") || key.startsWith("nbtliteral.")) {
                                val k: String
                                val flag: Boolean
                                if (key.startsWith("nbt.")) {
                                    k = key.removePrefix("nbt.")
                                    flag = true
                                } else {
                                    k = key.removePrefix("nbtliteral.")
                                    flag = false
                                }
                                val tag = tile.writeToNBT(NBTTagCompound()).getObject(k) ?: throw CTRLException("commandcontrol.probenbt.notag", k)
                                if (flag) {
                                    if (tag is NBTPrimitive) {
                                        if (tag.int.toDouble() == tag.double) tag.long.toString() else tag.double.toString()
                                    } else if (tag is NBTTagString) tag.string else tag.toString()
                                } else tag.toString()
                            } else (ControlSaveData[tile.world].tileData[tile][key] ?: 0).toString()
                        } else throw EntityNotFoundException("commandcontrol.expander.notile")
                    } else {
                        val entity = getEntity(server, e.sender, selector)
                        if (key.startsWith("nbt.") || key.startsWith("nbtliteral.")) {
                            val k: String
                            val flag: Boolean
                            if (key.startsWith("nbt.")) {
                                k = key.removePrefix("nbt.")
                                flag = true
                            } else {
                                k = key.removePrefix("nbtliteral.")
                                flag = false
                            }
                            val tag = entityToNBT(entity).getObject(k) ?: throw CTRLException("commandcontrol.probenbt.notag", k)
                            if (flag) {
                                if (tag is NBTPrimitive) {
                                    if (tag.int.toDouble() == tag.double) tag.long.toString() else tag.double.toString()
                                } else if (tag is NBTTagString) tag.string else tag.toString()
                            } else tag.toString()
                        } else {
                            val scoreboard = server.worldServerForDimension(0).scoreboard
                            val scoreobjective = scoreboard.getObjective(key)
                            if (scoreobjective != null) {
                                val name = if (entity is EntityPlayerMP) entity.getName() else entity.cachedUniqueIdString
                                (scoreboard.getOrCreateScore(name, scoreobjective).scorePoints).toString()
                            } else throw CommandException("commands.scoreboard.objectiveNotFound", key)
                        }
                    }
                } catch (ex: CommandException) {
                    e.sender.addChatMessage(TextComponentTranslation(ex.message, *ex.errorObjects).setStyle(Style().setColor(TextFormatting.RED)))
                    e.isCanceled = true
                    it.value
                }
            }) {
                "<" + it.groupValues[1] + ">"
            }
        }.toTypedArray()
    }


    // CommandBase is only extended to allow access to entityToNBT, these are safe to leave throwing errors

    override fun execute(server: MinecraftServer?, sender: ICommandSender?, args: Array<out String>?)
            = throw UnsupportedOperationException()

    override fun getCommandName()
            = throw UnsupportedOperationException()

    override fun getCommandUsage(sender: ICommandSender?)
            = throw UnsupportedOperationException()
}
