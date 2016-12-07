@file:Suppress("DEPRECATION")

package wiresegal.cmdctrl.common.man

import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.translation.I18n
import java.util.*

/**
 * @author WireSegal
 * Created at 5:00 PM on 12/6/16.
 */
class ManEntry(sender: ICommandSender, command: ICommand?, subcommand: String?) {
    val keys: List<String>
    var hasSub = false
    var subcom = ""

    init {
        val translationKeys = mutableListOf<String>()

        if (command != null) {
            val nameBase = command.getCommandUsage(sender)
            val manBase = "$nameBase.man"
            val subStr = (subcommand ?: "").toLowerCase(Locale.ROOT)
            val sub = "$nameBase.sub.$subStr.man"

            if (subcommand != null && I18n.canTranslate(sub)) {
                translationKeys.add(sub)
                var i = 1
                while (I18n.canTranslate("$sub$i")) translationKeys.add("$sub${i++}")
                hasSub = true
                subcom = subcommand
            } else if (I18n.canTranslate(manBase)) {
                translationKeys.add(manBase)
                var i = 1
                while (I18n.canTranslate("$manBase$i")) translationKeys.add("$manBase${i++}")
            }
        }

        keys = translationKeys
    }

    val asTextComponents: List<ITextComponent>
        get() = keys.map { TextComponentString(" | ").appendSibling(TextComponentTranslation(it))}
}
