@file:Suppress("DEPRECATION")

package wiresegal.cmdctrl.common

import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import wiresegal.cmdctrl.common.commands.biome.CommandFillBiome
import wiresegal.cmdctrl.common.commands.biome.CommandGetBiome
import wiresegal.cmdctrl.common.commands.biome.CommandMatchBiome
import wiresegal.cmdctrl.common.commands.biome.CommandSetBiome
import wiresegal.cmdctrl.common.commands.control.CommandFlashNBT
import wiresegal.cmdctrl.common.commands.control.CommandMath
import wiresegal.cmdctrl.common.commands.control.CommandProbeNBT
import wiresegal.cmdctrl.common.commands.data.CommandData
import wiresegal.cmdctrl.common.commands.data.CommandDataExecute
import wiresegal.cmdctrl.common.commands.misc.CommandDimension
import wiresegal.cmdctrl.common.commands.misc.CommandMan
import wiresegal.cmdctrl.common.commands.misc.CommandMotion
import wiresegal.cmdctrl.common.commands.misc.CommandReloadScripts
import wiresegal.cmdctrl.common.config.ConfigLoader
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.ExtraPlayerDataStore
import wiresegal.cmdctrl.common.core.GameruleDisableExplosions
import wiresegal.cmdctrl.common.core.ScoreExpander

/**
 * @author WireSegal
 * Created at 4:17 PM on 12/3/16.
 */
@Mod(modid = "commandcontrol", name = "Command Control", version = "1.1")
class CommandControl {
    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        ConfigLoader.load(e)
        ControlSaveData
        ScoreExpander
        ExtraPlayerDataStore
        if (useGamerules)
            GameruleDisableExplosions

        val config = Configuration(e.suggestedConfigurationFile)
        config.load()
        useCommands = config["general", "useCommands", true, "Whether to use the custom commands. (For building commands clientside for realms deployment.)"].boolean
        useGamerules = config["general", "useGamerules", true, "Whether to use the custom gamerules. (For building commands clientside for realms deployment.)"].boolean
        config.save()

    }

    @NetworkCheckHandler
    fun checkVersions(presentMods: Map<String, String>, side: Side) = true

    @Mod.EventHandler
    fun serverStarting(e: FMLServerStartingEvent) {
        if (useCommands) {
            // Biome Control
            e.registerServerCommand(CommandSetBiome)
            e.registerServerCommand(CommandFillBiome)
            e.registerServerCommand(CommandGetBiome)
            e.registerServerCommand(CommandMatchBiome)

            // Logic
            e.registerServerCommand(CommandData)
            e.registerServerCommand(CommandDataExecute)

            // Control
            e.registerServerCommand(CommandProbeNBT)
            e.registerServerCommand(CommandMath)
            e.registerServerCommand(CommandFlashNBT)

            // Misc
            e.registerServerCommand(CommandDimension)
            e.registerServerCommand(CommandReloadScripts)
            e.registerServerCommand(CommandMan)
            e.registerServerCommand(CommandMotion)
        }

        server = e.server
    }

    private lateinit var server: MinecraftServer

    @Mod.EventHandler
    fun serverStarted(e: FMLServerStartedEvent) {
        ConfigLoader.loadScripts(server)
    }

    companion object {
        fun translate(key: String, vararg args: Any?): String = I18n.translateToLocalFormatted(key, args)
        fun canTranslate(key: String): Boolean = I18n.canTranslate(key)

        var useCommands = true
            private set

        var useGamerules = true
            private set

        val LOGGER: Logger = LogManager.getLogger("commandcontrol")
    }
}
