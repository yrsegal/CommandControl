package wiresegal.cmdctrl.common.core

import com.teamwizardry.librarianlib.common.util.get
import net.minecraft.nbt.*

/**
 * @author WireSegal
 * Created at 9:21 AM on 12/14/16.
 */

private val MATCHER = "(?:(?:(?:\\[\\d+\\])|(?:[^.\\[\\]]+))(\\.|$|(?=\\[)))+".toRegex()

private val TOKENIZER = "((?:\\[\\d+\\])|(?:[^.\\[\\]]+))(?=[.\\[]|$)".toRegex()

fun NBTTagCompound.getObject(key: String): NBTBase? {
    if (!MATCHER.matches(key)) return null

    var currentElement: NBTBase = this

    val matched = TOKENIZER.findAll(key)
    for (match in matched) {
        val m = match.groupValues[1]
        if (m.startsWith("[")) {
            val ind = m.removePrefix("[").removeSuffix("]").toInt()
            if (currentElement is NBTTagList) {
                if (currentElement.tagCount() < ind + 1) return null
                currentElement = currentElement[ind]
            } else if (currentElement is NBTTagByteArray) {
                if (currentElement.byteArray.size < ind + 1) return null
                currentElement = NBTTagByte(currentElement.byteArray[ind])
            } else if (currentElement is NBTTagIntArray) {
                if (currentElement.intArray.size < ind + 1) return null
                currentElement = NBTTagInt(currentElement.intArray[ind])
            } else return null
        } else if (currentElement is NBTTagCompound) {
            if (!currentElement.hasKey(m)) return null
            currentElement = currentElement[m]
        } else return null
    }
    return currentElement
}
