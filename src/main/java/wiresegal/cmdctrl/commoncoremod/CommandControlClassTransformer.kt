package wiresegal.cmdctrl.commoncoremod

import net.minecraft.launchwrapper.IClassTransformer

/**
 * Created by Elad on 12/4/2016.
 */
class CommandControlClassTransformer : IClassTransformer {
    // classes are arrays of bytes, right? so
    override fun transform(name: String?, obfuscatedName: String?, p2: ByteArray?): ByteArray? {
        if (name == "whatever.class.you.transform") { // if class is whatever you need to transform
            // to be continued
        }
        return p2 // this should always be your default; leave a class as it is
    }
}