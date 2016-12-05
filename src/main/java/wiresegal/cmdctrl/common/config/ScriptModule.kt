package wiresegal.cmdctrl.common.config

import com.google.gson.JsonObject

/**
 * @author WireSegal
 * Created at 4:19 PM on 12/4/16.
 */
data class ScriptModule(val onLoad: List<CommandModule>, val onTick: List<CommandModule>, val tileChanges: List<ChangeWatcher>) {
    operator fun plus(that: ScriptModule)
            = ScriptModule(this.onLoad + that.onLoad,
                           this.onTick + that.onTick,
                           this.tileChanges + that.tileChanges)

    companion object {
        @JvmStatic
        fun fromObject(obj: JsonObject): ScriptModule {
            val debug = obj.has("debug")

            val onLoad = mutableListOf<CommandModule>()
            if (obj.has("init")) onLoad.addAll(CommandModule.fromPossibleArray(obj.get("init"), debug))

            val onTick = mutableListOf<CommandModule>()
            if (obj.has("tick")) onTick.addAll(CommandModule.fromPossibleArray(obj.get("tick"), debug))

            val tileChanges = mutableListOf<ChangeWatcher>()
            if (obj.has("tilechanges")) tileChanges.addAll(ChangeWatcher.fromPossibleArray(obj.get("tilechanges"), debug))

            return ScriptModule(onLoad, onTick, tileChanges)
        }
    }
}
