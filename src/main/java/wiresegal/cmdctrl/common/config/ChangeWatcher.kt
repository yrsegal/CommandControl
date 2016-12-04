package wiresegal.cmdctrl.common.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class ChangeWatcher(val watch: List<String>, val commands: List<CommandModule>) {
    companion object {
        @JvmStatic
        fun fromObject(obj: JsonObject): ChangeWatcher {
            if (!obj.has("watch") || !obj.has("execute"))
                throw IllegalArgumentException("Illegal command argument: $obj")

            val watch = mutableListOf<String>()
            val toWatch = obj.get("watch")
            if (toWatch.isJsonPrimitive) watch.add(toWatch.asString)
            else if (toWatch.isJsonArray) toWatch.asJsonArray.mapTo(watch) { it.asString }
            else throw IllegalArgumentException("Illegal command argument: $obj")

            val commands = CommandModule.fromPossibleArray(obj.get("execute"))

            return ChangeWatcher(watch, commands)
        }

        @JvmStatic
        fun fromPossibleArray(el: JsonElement): List<ChangeWatcher> {
            val ret = mutableListOf<ChangeWatcher>()
            if (!el.isJsonArray && !el.isJsonObject)
                throw IllegalArgumentException("Illegal command argument: $el")
            if (el.isJsonObject)
                ret.add(fromObject(el.asJsonObject))
            else
                el.asJsonArray.mapTo(ret) {
                    if (it.isJsonObject)
                        fromObject(it.asJsonObject)
                    else
                        throw IllegalArgumentException("Illegal command argument: $el")
                }

            return ret
        }
    }
}
