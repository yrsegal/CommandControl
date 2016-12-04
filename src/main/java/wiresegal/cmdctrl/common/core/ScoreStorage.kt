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
class ScoreMap<T> : HashMap<T, ScoreStorage>() {
    override fun get(key: T): ScoreStorage {
        var gotten = super.get(key)

        if (gotten == null) {
            gotten = ScoreStorage()
            put(key, gotten)
        }

        return gotten
    }
}
