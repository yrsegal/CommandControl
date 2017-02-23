package wiresegal.cmdctrl.common.core

import java.util.*

/**
 * @author WireSegal
 * Created at 4:28 PM on 12/3/16.
 *
 * A Type alias for pretty usage.
 */
class ScoreStorage : MutableMap<String, Int> by HashMap() {
    fun forEach(call: (String, Int) -> Unit) = forEach { call(it.key, it.value) }
}

/**
 * Another type alias, for easy getting.
 */
interface IScoreMap<T : Any> : MutableMap<T, ScoreStorage> {
    fun forEach(call: (T, ScoreStorage) -> Unit) = forEach { call(it.key, it.value) }
}

class ScoreMap<T : Any>(val map: MutableMap<T, ScoreStorage> = HashMap()) : MutableMap<T, ScoreStorage> by map, IScoreMap<T> {
    operator override fun get(key: T): ScoreStorage {
        var gotten = map[key]

        if (gotten == null) {
            gotten = ScoreStorage()
            put(key, gotten)
        }

        return gotten
    }
}

class WeakScoreMap<T : Any>(val map: MutableMap<T, ScoreStorage> = WeakHashMap()) : MutableMap<T, ScoreStorage> by map, IScoreMap<T> {
    operator override fun get(key: T): ScoreStorage {
        var gotten = map[key]

        if (gotten == null) {
            gotten = ScoreStorage()
            put(key, gotten)
        }

        return gotten
    }
}
