package wiresegal.cmdctrl.common.config

import com.google.gson.JsonParser
import com.teamwizardry.librarianlib.LibrarianLog
import com.teamwizardry.librarianlib.common.util.builders.json
import com.teamwizardry.librarianlib.common.util.builders.serialize
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

    private lateinit var configDir: File

    fun load(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)

        configDir = File(e.modConfigurationDirectory, "commandscripts")
        if (!configDir.exists()) {
            configDir.mkdir()
            val example = File(configDir, "example.json")
            example.writeText(json {
                obj(
                        "init" to array("commands run on world start go here"),
                        "tick" to array("commands run every tick go here", obj(
                                "execute" to "command to execute with other arguments",
                                "stats" to obj(
                                        "SuccessCount" to obj(
                                                "entity" to "an entity selector like @p or @r",
                                                "objective" to "a scoreboard objective"
                                        ),
                                        "Any other stats tag" to obj(
                                                "tile" to "a tile selector (@t)",
                                                "key" to "a data key to store to"
                                        )
                                ),
                                "if" to array(
                                        "conditional commands go here"
                                )
                        )),
                        "tilechanges" to array(obj(
                                "id" to "tile id to watch",
                                "watch" to array("tile fields to watch"),
                                "execute" to array("commands to run")
                        ))
                )
            }.serialize())
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldLoad(e: WorldEvent.Load) {
        module = ScriptModule(true, listOf(), listOf(), listOf())

        val files = configDir.list { dir, s ->
            File(dir, s).isFile && s.endsWith(".json")
        }
        files.forEach { file ->
            val qualified = File(configDir, file)
            try {
                module += ScriptModule.fromObject(JsonParser().parse(qualified.reader()).asJsonObject)
            } catch (e: Exception) { LibrarianLog.error("Failed to parse script $file") }
        }

        module.onLoad.forEach { it.run(FMLCommonHandler.instance().minecraftServerInstance, module.debug) }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(e: TickEvent.WorldTickEvent) {
        if (e.phase == TickEvent.Phase.END && e.side == Side.SERVER)
            module.onTick.forEach { it.run(FMLCommonHandler.instance().minecraftServerInstance, module.debug) }
    }
}
