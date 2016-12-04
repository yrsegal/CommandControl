package wiresegal.cmdctrl.commoncoremod

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

/**
 * Created by Elad on 12/4/2016.
 */
@IFMLLoadingPlugin.MCVersion("1.10.2") // MC version
@IFMLLoadingPlugin.TransformerExclusions("wiresegal.cmdctrl") // what package should NOT be visited by your transformer
class CommandControlLoadingPlugin : IFMLLoadingPlugin { // self explanatory
    override fun getASMTransformerClass(): Array<String> {
        return arrayOf("wiresegal.cmdctrl.commoncoremod.CommandControlClassTransformer") // this is the fully classified path to your IClassTransformer
    }

    override fun getModContainerClass(): String? { // you don't need this if you have a regular mod class
        return null
    }

    override fun getSetupClass(): String? { // you don't need this, don't know what it does
        return null
    }

    override fun injectData(map: Map<String, Any>) { // you don't need this, don't know what it does

    }

    override fun getAccessTransformerClass(): String? { // this is a ancient remain from the days you had to manually AT
        return null
    }
}
