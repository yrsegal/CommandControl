package wiresegal.cmdctrl.common.config

import com.google.gson.JsonElement
import net.minecraft.command.CommandResultStats
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class CommandModule(val command: String, val stats: Map<CommandResultStats.Type, IStatsAction>, val conditionals: List<CommandModule>, val debug: Boolean = false) {
    constructor(command: String, debug: Boolean = false) : this(command, mapOf(), listOf(), debug)

    fun run(server: MinecraftServer, pos: BlockPos? = null, world: World? = null) {
        val sender = ModuleSender(this, world, server, debug, pos)
        val result = server.commandManager.executeCommand(sender, command)
        if (result > 0) conditionals.forEach { it.run(server, pos, world) }
    }

    fun run(server: MinecraftServer, world: World? = null) = run(server, null, world)

    companion object {
        @JvmStatic
        fun fromElement(el: JsonElement, debug: Boolean = false): CommandModule {
            if (el.isJsonPrimitive)
                return CommandModule(el.asString, debug)
            if (el.isJsonObject) {
                val obj = el.asJsonObject
                if (!obj.has("execute") || !obj["execute"].isJsonPrimitive)
                    throw IllegalArgumentException("Illegal command argument: $el")
                val cmd = obj["execute"].asString

                val stats = mutableMapOf<CommandResultStats.Type, IStatsAction>()
                if (obj.has("stats")) {
                    if (!obj["stats"].isJsonObject)
                        throw IllegalArgumentException("Illegal command argument: $el")
                    CommandResultStats.Type.values()
                            .filter { obj["stats"].asJsonObject.has(it.typeName) && obj["stats"].asJsonObject[it.typeName].isJsonPrimitive }
                            .forEach { stats.put(it, IStatsAction.fromObject(it, obj["stats"].asJsonObject.getAsJsonPrimitive(it.typeName).asJsonObject)) }
                }

                val conditionals = mutableListOf<CommandModule>()
                if (obj.has("if")) conditionals.addAll(fromPossibleArray(obj.get("if"), debug))

                return CommandModule(cmd, stats, conditionals, debug)
            }

            throw IllegalArgumentException("Illegal command argument: $el")
        }

        @JvmStatic
        fun fromPossibleArray(el: JsonElement, debug: Boolean = false): List<CommandModule> {
            val ret = mutableListOf<CommandModule>()
            try {
                ret.add(fromElement(el, debug))
            } catch (e: IllegalArgumentException) {
                if (!el.isJsonArray)
                    throw IllegalArgumentException("Illegal command argument: $el")
                el.asJsonArray.forEach {
                    val module = fromElement(it, debug)
                    if (!module.command.matches("^\\s*#".toRegex()))
                        ret.add(module)
                }
            }
            return ret
        }
    }
}
