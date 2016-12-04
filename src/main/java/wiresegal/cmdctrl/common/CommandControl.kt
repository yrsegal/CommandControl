package wiresegal.cmdctrl.common

import com.teamwizardry.librarianlib.common.network.PacketHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.relauncher.Side
import wiresegal.cmdctrl.common.commands.biome.CommandFillBiome
import wiresegal.cmdctrl.common.commands.biome.CommandGetBiome
import wiresegal.cmdctrl.common.commands.biome.CommandMatchBiome
import wiresegal.cmdctrl.common.commands.biome.CommandSetBiome
import wiresegal.cmdctrl.common.commands.data.CommandData
import wiresegal.cmdctrl.common.commands.data.CommandDataExecute
import wiresegal.cmdctrl.common.config.ConfigLoader
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.network.PacketBiomeUpdate

/**
 * @author WireSegal
 * Created at 4:17 PM on 12/3/16.
 */
@Mod(modid = "commandcontrol", name = "Command Control", version = "1.0", dependencies = "required-after:librarianlib")
class CommandControl {
    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        ConfigLoader.load(e)
        ControlSaveData
        PacketHandler.register(PacketBiomeUpdate::class.java, Side.CLIENT)
    }

    @Mod.EventHandler
    fun serverStarting(e: FMLServerStartingEvent) {
        // Biome Control
        e.registerServerCommand(CommandSetBiome)
        e.registerServerCommand(CommandFillBiome)
        e.registerServerCommand(CommandGetBiome)
        e.registerServerCommand(CommandMatchBiome)

        // Logic
        e.registerServerCommand(CommandData)
        e.registerServerCommand(CommandDataExecute)
    }
}
