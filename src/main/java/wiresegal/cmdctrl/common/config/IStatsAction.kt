package wiresegal.cmdctrl.common.config

import com.google.gson.JsonObject
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandSender
import net.minecraft.nbt.NBTTagCompound
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.shade.nbt

/**
 * @author WireSegal
 * Created at 5:17 PM on 12/4/16.
 */
interface IStatsAction {
    fun set(type: CommandResultStats.Type, value: Int, sender: ICommandSender)

    companion object {
        fun fromObject(type: CommandResultStats.Type, obj: JsonObject): IStatsAction {
            if (obj.has("entity")) {
                val selector = obj.get("entity").asString
                val objective = obj.get("objective").asString
                return object : IStatsAction {
                    val stats = CommandResultStats()

                    init {
                        stats.readStatsFromNBT(nbt {
                            comp(
                                    "CommandStats" to comp(
                                            type.typeName + "Name" to selector,
                                            type.typeName + "Objective" to objective
                                    )
                            )
                        } as NBTTagCompound)
                    }

                    override fun set(type: CommandResultStats.Type, value: Int, sender: ICommandSender) {
                        stats.setCommandStatForSender(sender.server, sender, type, value)
                    }
                }
            } else if (obj.has("tile")) {
                val selector = obj.get("tile").asString
                val key = obj.get("key").asString
                return object : IStatsAction {
                    override fun set(type: CommandResultStats.Type, value: Int, sender: ICommandSender) {
                        val tiles = TileSelector.matchTiles(sender.server!!, sender, selector)
                        tiles.forEach {
                            val data = ControlSaveData[it.world]
                            data.tileData[it][key] = value
                        }
                    }
                }
            }

            throw IllegalArgumentException("Invalid command argument: $obj")
        }
    }
}
