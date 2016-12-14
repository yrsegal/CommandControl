package wiresegal.cmdctrl.common.config

import com.google.gson.JsonElement
import com.teamwizardry.librarianlib.common.util.MethodHandleHelper
import com.teamwizardry.librarianlib.common.util.MutableStaticFieldDelegate
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandResultStats
import net.minecraft.command.ICommandListener
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import wiresegal.cmdctrl.common.core.BlankCommandListener

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class CommandModule(val command: String, val stats: Map<CommandResultStats.Type, IStatsAction>, val conditionals: List<CommandModule>, val invConditionals: List<CommandModule>, val debug: Boolean = false) {
    constructor(command: String, debug: Boolean = false) : this(command, mapOf(), listOf(), listOf(), debug)

    fun run(server: MinecraftServer, pos: BlockPos? = null, world: World? = null) {
        val sender = ModuleSender(this, world, server, debug, pos)
        val prevCommandListener = commandListener
        if (!debug) commandListener = BlankCommandListener
        val result = server.commandManager.executeCommand(sender, command)
        if (result > 0) conditionals.forEach { it.run(server, pos, world) }
        else invConditionals.forEach { it.run(server, pos, world) }
        if (!debug) commandListener = prevCommandListener
    }

    fun run(server: MinecraftServer, world: World? = null) = run(server, null, world)

    companion object {
        var commandListener: ICommandListener by MutableStaticFieldDelegate(
                MethodHandleHelper.wrapperForStaticGetter(CommandBase::class.java, "a", "field_71533_a", "commandListener"),
                MethodHandleHelper.wrapperForStaticSetter(CommandBase::class.java, "a", "field_71533_a", "commandListener"))

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

                val invConditionals = mutableListOf<CommandModule>()
                if (obj.has("else")) invConditionals.addAll(fromPossibleArray(obj.get("else"), debug))

                return CommandModule(cmd, stats, conditionals, invConditionals, debug)
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
