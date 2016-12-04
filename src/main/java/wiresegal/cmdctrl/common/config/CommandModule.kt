package wiresegal.cmdctrl.common.config

import com.google.gson.JsonElement
import net.minecraft.command.CommandResultStats
import net.minecraft.server.MinecraftServer

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class CommandModule(val command: String, val stats: Map<CommandResultStats.Type, IStatsAction>, val conditionals: List<CommandModule>) {
    constructor(command: String) : this(command, mapOf(), listOf())

    fun run(server: MinecraftServer, debug: Boolean = false) {
        val sender = ModuleSender(this, server, debug)
        val result = server.commandManager.executeCommand(sender, command)
        if (result > 0) conditionals.forEach { it.run(server) }
    }

    companion object {
        @JvmStatic
        fun fromElement(el: JsonElement): CommandModule {
            if (el.isJsonPrimitive)
                return CommandModule(el.asString)
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
                if (obj.has("if")) conditionals.addAll(fromPossibleArray(obj.get("if")))

                return CommandModule(cmd, stats, conditionals)
            }

            throw IllegalArgumentException("Illegal command argument: $el")
        }

        @JvmStatic
        fun fromPossibleArray(el: JsonElement): List<CommandModule> {
            val ret = mutableListOf<CommandModule>()
            try {
                ret.add(fromElement(el))
            } catch (e: IllegalArgumentException) {
                if (!el.isJsonArray)
                    throw IllegalArgumentException("Illegal command argument: $el")
                el.asJsonArray.mapTo(ret) { fromElement(it) }
            }
            return ret
        }
    }
}
