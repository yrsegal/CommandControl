package wiresegal.cmdctrl.common.config

import com.google.gson.JsonParser
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import wiresegal.cmdctrl.common.CommandControl
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.getObject
import wiresegal.cmdctrl.common.core.shade.json
import wiresegal.cmdctrl.common.core.shade.serialize
import java.io.File
import java.util.*

/**
 * @author WireSegal
 * Created at 5:43 PM on 12/4/16.
 */
object ConfigLoader {
    private var module = ScriptModule(listOf(), listOf(), listOf())

    private lateinit var configDir: File

    fun load(e: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)

        configDir = File(e.modConfigurationDirectory, "commandscripts")
        if (!configDir.exists()) {
            configDir.mkdir()
            val example = File(configDir, "example.json")
            example.writeText(json {
                obj(
                        "init" to array("# commands run on world start go here"),
                        "tick" to array("# commands run every tick go here", obj(
                                "execute" to "# command to execute with other arguments",
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
                                        "# conditional commands go here"
                                ),
                                "else" to array(
                                        "# commands to be run if the command fails go here"
                                )
                        )),
                        "tilechanges" to array(obj(
                                "id" to "tile id to watch",
                                "watch" to array("tile fields to watch"),
                                "execute" to array("# commands to run")
                        ))
                )
            }.serialize())
        }
    }

    private val tileMap = WeakHashMap<TileEntity, MutableMap<String, Int>>()

    private val toWatch = hashMapOf<String, MutableList<String>>()

    fun loadScripts(server: MinecraftServer) {
        module = ScriptModule(listOf(), listOf(), listOf())

        val files = configDir.list { dir, s ->
            File(dir, s).isFile && s.endsWith(".json")
        }
        files.forEach { file ->
            val qualified = File(configDir, file)
            try {
                module += ScriptModule.fromObject(JsonParser().parse(qualified.reader()).asJsonObject)
            } catch (e: Exception) {
                CommandControl.LOGGER.error("Failed to parse script $file")
            }
        }

        tileMap.clear()
        toWatch.clear()
        module.tileChanges.forEach { toWatch.getOrPut(it.id) { mutableListOf() }.addAll(it.watch) }

        module.onLoad.forEach { it.run(FMLCommonHandler.instance().minecraftServerInstance, server.worldServerForDimension(0)) }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onWorldTick(e: TickEvent.WorldTickEvent) {
        if (e.phase == TickEvent.Phase.END && e.side == Side.SERVER) {
            val server = FMLCommonHandler.instance().minecraftServerInstance

            for (tile in e.world.loadedTileEntityList) {
                val dump = tile.writeToNBT(NBTTagCompound())
                val id = TileSelector.classToNameMap[tile.javaClass]

                val keys = toWatch[id]?.mapNotNull {
                    val obj = dump.getObject(it)
                    if (obj == null) null else it to obj
                }

                if (tile in tileMap) {
                    val ranModules = mutableSetOf<ChangeWatcher>()
                    keys?.forEach {
                        val (key, tag) = it
                        val prev = tileMap[tile]!!.getOrElse(key) { null } ?: return@forEach
                        val newHash = tag.hashCode()
                        if (prev != newHash)
                            module.tileChanges
                                    .filter { it.id == id && key in it.watch && it !in ranModules }
                                    .forEach {
                                        ranModules.add(it)
                                        it.commands.forEach { it.run(server, tile.pos, e.world) }
                                    }
                    }
                }

                keys?.forEach { tileMap.getOrPut(tile) { hashMapOf() }.put(it.first, it.second.hashCode()) }

            }
            module.onTick.forEach { it.run(server, e.world) }
        }
    }
}
