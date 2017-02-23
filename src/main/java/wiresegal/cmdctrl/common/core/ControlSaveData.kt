package wiresegal.cmdctrl.common.core

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.Constants
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import wiresegal.cmdctrl.common.commands.data.TileSelector
import wiresegal.cmdctrl.common.core.shade.nbt

/**
 * @author WireSegal
 * Created at 4:24 PM on 12/3/16.
 */
class ControlSaveData(name: String = KEY) : WorldSavedData(name) {

    private var world: World? = null

    companion object {
        const val ID = "commandcontrol"

        const val KEY = "$ID:savedata"
        const val GLOBAL_KEY = "$ID:global"
        const val WORLD_KEY = "$ID:world"
        const val SLICE_KEY = "$ID:slice"
        const val POS_KEY = "$ID:pos"
        const val TILE_KEY = "$ID:tile"

        operator fun get(world: World): ControlSaveData {
            var data = world.loadItemData(ControlSaveData::class.java, KEY) as? ControlSaveData
            if (data == null) {
                data = ControlSaveData()
                world.setItemData(KEY, data)
            }
            data.world = world
            return data
        }

        val globalWorldData: ControlSaveData
            get() = this[FMLCommonHandler.instance().minecraftServerInstance.worldServerForDimension(0)]

        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onWorldLoad(e: WorldEvent.Load) {
            get(e.world)
        }

    }

    // Only check global data from dimension 0
    val globalData = ScoreStorage()

    val worldData = ScoreStorage()
    val sliceData = ScoreMap<Slice>()
    val posData = ScoreMap<BlockPos>()
    val tileData = WeakScoreMap<TileEntity>()

    override fun readFromNBT(tag: NBTTagCompound) {
        globalData.clear()
        worldData.clear()
        sliceData.clear()
        posData.clear()
        tileData.clear()

        if (tag.hasKey(GLOBAL_KEY, Constants.NBT.TAG_LIST)) {
            val list = tag.getTagList(GLOBAL_KEY, Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount())
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { globalData.put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(WORLD_KEY, Constants.NBT.TAG_LIST)) {
            val list = tag.getTagList(WORLD_KEY, Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount())
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { globalData.put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(SLICE_KEY, Constants.NBT.TAG_LIST)) {
            val list = tag.getTagList(SLICE_KEY, Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount())
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { sliceData[Slice(it.getLong("pos"))].put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(POS_KEY, Constants.NBT.TAG_LIST)) {
            val list = tag.getTagList(POS_KEY, Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount())
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { posData[BlockPos.fromLong(it.getLong("pos"))].put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(TILE_KEY, Constants.NBT.TAG_LIST)) {
            val list = tag.getTagList(TILE_KEY, Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount())
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach {
                        world?.let { w ->
                            val id = it.getString("id")
                            val pos = BlockPos.fromLong(it.getLong("pos"))
                            val te = w.getTileEntity(pos)
                            if (te != null && TileSelector.classToNameMap[te.javaClass] == id)
                                tileData[te].put(it.getString("key"), it.getInteger("value"))
                        }
                    }
        }
    }

    override fun writeToNBT(tag: NBTTagCompound): NBTTagCompound {
        if (globalData.isNotEmpty()) {
            val l = NBTTagList()
            globalData.forEach { s, i ->
                l.appendTag(nbt {
                    comp(
                            "key" to s,
                            "value" to i
                    )
                })
            }
            tag.setTag(GLOBAL_KEY, l)
        }

        if (worldData.isNotEmpty()) {
            val l = NBTTagList()
            worldData.forEach { s, i ->
                l.appendTag(nbt {
                    comp(
                            "key" to s,
                            "value" to i
                    )
                })
            }
            tag.setTag(WORLD_KEY, l)
        }

        if (sliceData.isNotEmpty()) {
            val l = NBTTagList()
            sliceData.forEach { slice, storage ->
                storage.forEach { s, i ->
                    l.appendTag(nbt {
                        comp(
                                "pos" to slice.toLong(),
                                "key" to s,
                                "value" to i
                        )
                    })
                }
            }
            tag.setTag(SLICE_KEY, l)
        }

        if (posData.isNotEmpty()) {
            val l = NBTTagList()
            posData.forEach { pos, storage ->
                storage.forEach { s, i ->
                    l.appendTag(nbt {
                        comp(
                                "pos" to pos.toLong(),
                                "key" to s,
                                "value" to i
                        )
                    })
                }
            }
            tag.setTag(POS_KEY, l)
        }

        if (tileData.isNotEmpty()) {
            val l = NBTTagList()
            tileData.forEach { tile, storage ->
                storage.forEach { s, i ->
                    l.appendTag(nbt {
                        comp(
                                "id" to TileSelector.classToNameMap[tile.javaClass],
                                "pos" to tile.pos.toLong(),
                                "key" to s,
                                "value" to i
                        )
                    })
                }
            }
            tag.setTag(TILE_KEY, l)
        }

        return tag
    }

}
