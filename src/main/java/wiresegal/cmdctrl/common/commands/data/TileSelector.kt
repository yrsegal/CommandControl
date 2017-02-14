package wiresegal.cmdctrl.common.commands.data

import com.teamwizardry.librarianlib.common.util.ImmutableStaticFieldDelegate
import com.teamwizardry.librarianlib.common.util.MethodHandleHelper
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.Vec3d
import wiresegal.cmdctrl.common.core.CTRLException
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.WeakScoreMap

/**
 * @author WireSegal
 * Created at 1:48 PM on 12/4/16.
 */
object TileSelector {
    private val TOKEN_PATTERN = "^(?:@t)?(?:\\[?([\\w.=,!-:]*)\\]?)?$".toRegex()
    private val KEY_VALUE_LIST_PATTERN = "\\G(\\w+)=([-!]?[\\w.-:]*)(?:$|,)".toRegex()
    private val WORLD_BINDING_ARGS = setOf("x", "y", "z", "xs", "ys", "zs", "dx", "dy", "dz", "rm", "r")

    /*
     * Valid selector rules:
     * x - set the x value used for radial and volumetric calculations
     * y - set the y value used for radial and volumetric calculations
     * z - set the z value used for radial and volumetric calculations
     * xs - shift x value
     * ys - shift y value
     * zs - shift z value
     * r - maximum radius
     * rm - minimum radius
     * dx - x volume the target must be within
     * dy - y volume the target must be within
     * dz - z volume the target must be within
     *
     * type - the id of the tile
     *
     * has - Whether the data contains a score for this tile
     *
     * score_name - maximum score of key "name" for the tile
     * score_name_min - minimum score of key "name" for the tile
     */

    val nameToClassMap by ImmutableStaticFieldDelegate<TileSelector, Map<String, Class<out TileEntity>>>(
            MethodHandleHelper.wrapperForStaticGetter(TileEntity::class.java, "f", "field_145855_i", "nameToClassMap"))
    val classToNameMap by ImmutableStaticFieldDelegate<TileSelector, Map<Class<out TileEntity>, String>>(
            MethodHandleHelper.wrapperForStaticGetter(TileEntity::class.java, "g", "field_145853_j", "classToNameMap"))

    private fun hasArgument(params: Map<String, String>) = WORLD_BINDING_ARGS.any { params.containsKey(it) }

    private fun getArgumentMap(argumentString: String): Map<String, String> {
        val map = mutableMapOf<String, String>()

        val matcher = KEY_VALUE_LIST_PATTERN.findAll(argumentString.removePrefix("@t").removePrefix("[").removeSuffix("]"))
        matcher.forEach { map.put(it.groupValues[1], it.groupValues[2]) }

        return map
    }

    fun matchOne(server: MinecraftServer, sender: ICommandSender, token: String)
            = matchTiles(server, sender, token).singleOrNull()

    fun isTileSelector(token: String) =
        token.startsWith("@t") && TOKEN_PATTERN.matchEntire(token) != null

    fun matchTiles(server: MinecraftServer, sender: ICommandSender, token: String): List<TileEntity> {
        val matcher = TOKEN_PATTERN.matchEntire(token)

        if (matcher != null) {
            val map = getArgumentMap(matcher.groupValues[1])

            if (!isTileTypeValid(map["type"])) {
                return emptyList()
            } else {
                val c = if (map.containsKey("c")) CommandBase.parseInt(map["c"]) else 0
                if (hasArgument(map)) {
                    val world = sender.entityWorld
                    val data = ControlSaveData[world]

                    return world.loadedTileEntityList
                            .filter(createPredicate(sender, map, data.tileData))
                            .sortedBy { (if (c < 0) -1 else 1) * sender.positionVector.distanceTo(Vec3d(it.pos).addVector(0.5, 0.5, 0.5)) }
                } else {
                    val list = mutableListOf<TileEntity>()
                    for (world in server.worldServers) {
                        val data = ControlSaveData[world]

                        list.addAll(world.loadedTileEntityList
                                .filter(createPredicate(sender, map, data.tileData)))
                    }
                    return list
                            .sortedBy { (if (c < 0) -1 else 1) * sender.positionVector.distanceTo(Vec3d(it.pos).addVector(0.5, 0.5, 0.5)) }
                }
            }
        } else throw CTRLException("commandcontrol.tselector.invalidrule")
    }

    fun isTileTypeValid(type: String?) = type == null || nameToClassMap.containsKey(type)

    fun createPredicate(sender: ICommandSender, map: Map<String, String>, dataStorage: WeakScoreMap<TileEntity>): (TileEntity) -> Boolean {
        val predicates = mutableListOf<(TileEntity) -> Boolean>()
        predicates.addAll(getScorePredicates(dataStorage, map))
        predicates.addAll(getPosPredicates(sender, map))
        predicates.addAll(getTypePredicates(map))
        predicates.addAll(getTagPredicates(dataStorage, map))
        val c = if (map.containsKey("c")) CommandBase.parseInt(map["c"]) else null

        if (c == null)
            return { !predicates.any { predicate -> !predicate(it) } }
        else if (c == 0)
            return { false }
        else if (c < 0) {
            var i = 0
            return { c < i-- && !predicates.any { predicate -> !predicate(it) } }
        } else {
            var i = 0
            return { c > i++ && !predicates.any { predicate -> !predicate(it) } }
        }
    }

    private fun getTypePredicates(params: Map<String, String>): List<(TileEntity) -> Boolean> {
        if (params.containsKey("type") && isTileTypeValid(params["type"]!!)) {
            val type = params["type"]
            return listOf({ it ->
                classToNameMap[it.javaClass] == type
            })
        }
        return emptyList()
    }

    private fun getScorePredicates(dataStorage: WeakScoreMap<TileEntity>, params: Map<String, String>): List<(TileEntity) -> Boolean> {
        val ret = mutableListOf<(TileEntity) -> Boolean>()
        for ((key, value) in params) if (key.startsWith("score_")) {
            val number = CommandBase.parseInt(value)
            if (key.endsWith("_min")) {
                val score = key.removePrefix("score_").removeSuffix("_min")
                ret.add {
                    val data = dataStorage[it][score]
                    data != null && data >= number
                }
            } else {
                val score = key.removePrefix("score_")
                ret.add {
                    val data = dataStorage[it][score]
                    data != null && data <= number
                }
            }
        }
        return ret
    }

    private fun getTagPredicates(dataStorage: WeakScoreMap<TileEntity>, params: Map<String, String>): List<(TileEntity) -> Boolean> {
        if (params.containsKey("has")) {
            val value = params["has"]
            return listOf({ it ->
                dataStorage[it].containsKey(value)
            })
        }

        return emptyList()
    }

    private fun getPosPredicates(sender: ICommandSender, params: Map<String, String>): List<(TileEntity) -> Boolean> {
        val ret = mutableListOf<(TileEntity) -> Boolean>()
        val xOverride = if (params.containsKey("x")) CommandBase.parseDouble(params["x"]) else null
        val yOverride = if (params.containsKey("y")) CommandBase.parseDouble(params["y"]) else null
        val zOverride = if (params.containsKey("z")) CommandBase.parseDouble(params["z"]) else null

        val xShift = if (params.containsKey("xs")) CommandBase.parseDouble(params["xs"]) else 0.0
        val yShift = if (params.containsKey("ys")) CommandBase.parseDouble(params["ys"]) else 0.0
        val zShift = if (params.containsKey("zs")) CommandBase.parseDouble(params["zs"]) else 0.0

        val minR = if (params.containsKey("rm")) CommandBase.parseDouble(params["rm"]) else 0.0
        val maxR = if (params.containsKey("r")) CommandBase.parseDouble(params["r"]) else 30000000.0

        val x = (xOverride ?: sender.positionVector.xCoord) + xShift
        val y = (yOverride ?: sender.positionVector.yCoord) + yShift
        val z = (zOverride ?: sender.positionVector.zCoord) + zShift

        val pos = Vec3d(x, y, z)

        val dx = if (params.containsKey("dx")) CommandBase.parseDouble(params["dx"]) else null
        val dy = if (params.containsKey("dy")) CommandBase.parseDouble(params["dy"]) else null
        val dz = if (params.containsKey("dz")) CommandBase.parseDouble(params["dz"]) else null

        if (dx != null) ret.add { it.pos.x < x + dx + 1 || it.pos.x > x - dx }
        if (dz != null) ret.add { it.pos.z < z + dz + 1 || it.pos.z > z - dz }
        if (dy != null) ret.add { it.pos.y < y + dy + 1 || it.pos.y > y - dy }

        ret.add {
            val dSq = pos.squareDistanceTo(Vec3d(it.pos))
            dSq >= minR * minR && dSq <= maxR * maxR
        }

        return ret
    }
}
