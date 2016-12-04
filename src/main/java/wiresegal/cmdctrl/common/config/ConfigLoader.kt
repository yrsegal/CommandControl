package wiresegal.cmdctrl.common.config

import com.google.gson.JsonParser
import com.teamwizardry.librarianlib.LibrarianLog
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import java.io.File

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/4/16.
 */
object ConfigLoader {
    private var module = ScriptModule(true, listOf(), listOf(), listOf())

    fun load(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)

        val configDir = File(e.modConfigurationDirectory, "commandscripts")
        if (configDir.isDirectory) {
            val files = configDir.list { file, s ->
                file.isFile && s.endsWith(".json")
            }
            files.forEach { file ->
                val qualified = File(configDir, file)
                try {
                    module += ScriptModule.fromObject(JsonParser().parse(qualified.reader()).asJsonObject)
                } catch (e: Exception) { LibrarianLog.error("Failed to parse script $file") }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldLoad(e: WorldEvent.Load) {
        module.onLoad.forEach { it.run(FMLCommonHandler.instance().minecraftServerInstance) }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(e: TickEvent.WorldTickEvent) {
        if (e.phase == TickEvent.Phase.END && e.side == Side.SERVER)
            module.onTick.forEach { it.run(FMLCommonHandler.instance().minecraftServerInstance) }
    }
}
