package wiresegal.cmdctrl.common.core

import com.teamwizardry.librarianlib.common.util.NBTTypes
import com.teamwizardry.librarianlib.common.util.builders.nbt
import com.teamwizardry.librarianlib.common.util.indices
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldSavedData
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import wiresegal.cmdctrl.common.commands.data.TileSelector

/**
 * @author WireSegal
 * Created at 4:24 PM on 12/3/16.
 */
class ControlSaveData(name: String = key) : WorldSavedData(name) {

    private var world: World? = null

    companion object {
        const val id = "commandcontrol"

        const val key = "$id:savedata"
        const val globalKey = "$id:global"
        const val worldKey = "$id:world"
        const val sliceKey = "$id:slice"
        const val posKey = "$id:pos"
        const val tileKey = "$id:tile"

        operator fun get(world: World): ControlSaveData {
            var data = world.loadItemData(ControlSaveData::class.java, key) as? ControlSaveData
            if (data == null) {
                data = ControlSaveData()
                world.setItemData(key, data)
            }
            data.world = world
            return data
        }

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

        if (tag.hasKey(globalKey, NBTTypes.LIST)) {
            val list = tag.getTagList(globalKey, NBTTypes.COMPOUND)
            list.indices
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { globalData.put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(worldKey, NBTTypes.LIST)) {
            val list = tag.getTagList(worldKey, NBTTypes.COMPOUND)
            list.indices
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { globalData.put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(sliceKey, NBTTypes.LIST)) {
            val list = tag.getTagList(sliceKey, NBTTypes.COMPOUND)
            list.indices
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { sliceData[Slice(it.getLong("pos"))].put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(posKey, NBTTypes.LIST)) {
            val list = tag.getTagList(posKey, NBTTypes.COMPOUND)
            list.indices
                    .map { list[it] }
                    .filterIsInstance<NBTTagCompound>()
                    .forEach { posData[BlockPos.fromLong(it.getLong("pos"))].put(it.getString("key"), it.getInteger("value")) }
        }

        if (tag.hasKey(tileKey, NBTTypes.LIST)) {
            val list = tag.getTagList(tileKey, NBTTypes.COMPOUND)
            list.indices
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
            tag.setTag(globalKey, l)
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
            tag.setTag(worldKey, l)
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
            tag.setTag(sliceKey, l)
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
            tag.setTag(posKey, l)
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
            tag.setTag(tileKey, l)
        }

        return tag
    }

}
