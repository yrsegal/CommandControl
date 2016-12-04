package wiresegal.cmdctrl.common.core

import java.util.*

/**
 * @author WireSegal
 * Created at 4:28 PM on 12/3/16.
 *
 * A Type alias for pretty usage.
 */
class ScoreStorage : HashMap<String, Int>()

/**
 * Another type alias, for easy getting.
 */
interface IScoreMap<T> : MutableMap<T, ScoreStorage>

class ScoreMap<T> : HashMap<T, ScoreStorage>(), IScoreMap<T> {
    operator override fun get(key: T): ScoreStorage {
        var gotten = super.get(key)

        if (gotten == null) {
            gotten = ScoreStorage()
            put(key, gotten)
        }

        return gotten
    }
}

class WeakScoreMap<T> : WeakHashMap<T, ScoreStorage>(), IScoreMap<T> {
    operator override fun get(key: T): ScoreStorage {
        var gotten = super.get(key)

        if (gotten == null) {
            gotten = ScoreStorage()
            put(key, gotten)
        }

        return gotten
    }
}
