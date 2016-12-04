package wiresegal.cmdctrl.common.config

import com.google.gson.JsonObject

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class ScriptModule(val debug: Boolean, val onLoad: List<CommandModule>, val onTick: List<CommandModule>, val tileChanges: List<ChangeWatcher>) {
    companion object {
        @JvmStatic
        fun fromObject(obj: JsonObject): ScriptModule {
            val debug = obj.has("debug") && obj["debug"].isJsonPrimitive && obj["debug"].asBoolean

            val onLoad = mutableListOf<CommandModule>()
            if (obj.has("init")) onLoad.addAll(CommandModule.fromPossibleArray(obj.get("init")))

            val onTick = mutableListOf<CommandModule>()
            if (obj.has("tick")) onTick.addAll(CommandModule.fromPossibleArray(obj.get("tick")))

            val tileChanges = mutableListOf<ChangeWatcher>()
            if (obj.has("tilechanges")) tileChanges.addAll(ChangeWatcher.fromPossibleArray(obj.get("tilechanges")))

            return ScriptModule(debug, onLoad, onTick, tileChanges)
        }
    }
}
