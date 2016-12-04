package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.ScoreMap
import wiresegal.cmdctrl.common.core.Slice

/**
 * @author WireSegal
 * Created at 7:36 PM on 12/3/16.
 */
object CommandDataExecute : CommandBase() {

    val positionals = arrayOf("slice", "pos", "tile")

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args.size < 3)
            throw WrongUsageException(getCommandUsage(sender))
        val scope = args[0]
        val rules = args[1]
        val command = buildString(args, 2)
        if (scope !in positionals)
            throw WrongUsageException(getCommandUsage(sender))

        val manager = server.getCommandManager()

        var toThrow: CommandException? = null
        if (scope == "slice") {
            val slices = ControlSaveData[sender.entityWorld].sliceData
            val pred = createSlicePredicate(sender, rules, slices)
            for ((key, data) in slices) if (data.isNotEmpty() && pred(key)) {
                try {
                    val invocations = manager.executeCommand(PositionalSender(sender, key), command)

                    if (invocations < 1) toThrow = CommandException("commands.execute.allInvocationsFailed", command)
                } catch (var24: Throwable) {
                    toThrow = CommandException("commands.execute.failed", command, key.toString())
                }
            }
            if (toThrow != null) throw toThrow
        } else if (scope == "pos") {
            val poses = ControlSaveData[sender.entityWorld].posData
            val pred = createPosPredicate(sender, rules, poses)
            for ((key, data) in poses) if (data.isNotEmpty() && pred(key)) {
                try {
                    val invocations = manager.executeCommand(PositionalSender(sender, key), command)

                    if (invocations < 1) toThrow = CommandException("commands.execute.allInvocationsFailed", command)
                } catch (var24: Throwable) {
                    toThrow = CommandException("commands.execute.failed", command, key.run { "$x, $y, $z"} )
                }
            }
        } else if (scope == "tile") {
            val tiles = TileSelector.matchTiles(server, sender, rules)
            for (tile in tiles) {
                try {
                    val invocations = manager.executeCommand(TileSender(sender, tile), command)

                    if (invocations < 1) toThrow = CommandException("commands.execute.allInvocationsFailed", command)
                } catch (var24: Throwable) {
                    toThrow = CommandException("commands.execute.failed", command, tile.pos.run { "$x, $y, $z"} )
                }
            }
        }
        if (toThrow != null) throw toThrow
    }

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
     * score_name - maximum score of key "name" for the pos
     * score_name_min - minimum score of key "name" for the pos
     */

    val RULE_FORMAT = "^(?:\\[?([\\w.=,!-]*)\\]?)?$".toRegex()
    val SINGLE_RULE = "\\G(\\w+)=([-!]?[\\w.-]*)(?:$|,)".toRegex()

    fun createSlicePredicate(sender: ICommandSender, rules: String, dataStorage: ScoreMap<Slice>): (Slice) -> Boolean {
        if (!RULE_FORMAT.matches(rules)) throw CommandException("commandcontrol.dataexecute.invalidrule")
        val predicates = mutableListOf<(Slice) -> Boolean>()
        val matches = SINGLE_RULE.findAll(rules.removePrefix("[").removeSuffix("]"))
        val map = mapOf(*matches.map { it.groupValues[1] to it.groupValues[2] }.toList().toTypedArray())
        predicates.addAll(getScorePredicates(dataStorage, map))
        predicates.addAll(getPosPredicates("slice", sender, map))
        val c = if (map.containsKey("c")) parseInt(map["c"]) else null

        if (c == 0)
            return { false }
        else if (c == null)
            return { predicates.fold(false) { prev, pred -> prev || pred(it) } }
        else if (c < 0) return {
            var i = 0
            predicates.foldRight(false) { pred, prev -> c < i-- && prev || pred(it)  }
        }
        else return {
            var i = 0
            predicates.fold(false) { prev, pred -> c > i++ && prev || pred(it)  }
        }
    }

    fun createPosPredicate(sender: ICommandSender, rules: String, dataStorage: ScoreMap<BlockPos>): (BlockPos) -> Boolean {
        if (!RULE_FORMAT.matches(rules)) throw CommandException("commandcontrol.dataexecute.invalidrule")
        val predicates = mutableListOf<(BlockPos) -> Boolean>()
        val matches = SINGLE_RULE.findAll(rules.removePrefix("[").removeSuffix("]"))
        val map = mapOf(*matches.map { it.groupValues[1] to it.groupValues[2] }.toList().toTypedArray())
        predicates.addAll(getScorePredicates(dataStorage, map))
        predicates.addAll(getPosPredicates("pos", sender, map))
        val c = if (map.containsKey("c")) parseInt(map["c"]) else null

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

    private fun <T> getScorePredicates(dataStorage: ScoreMap<T>, params: Map<String, String>): List<(T) -> Boolean> {
        val ret = mutableListOf<(T) -> Boolean>()
        for ((key, value) in params) if (key.startsWith("score_")) {
            val number = parseInt(value)
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

    private fun <T : BlockPos> getPosPredicates(scope: String, sender: ICommandSender, params: Map<String, String>): List<(T) -> Boolean> {
        val ret = mutableListOf<(T) -> Boolean>()
        val xOverride = if (params.containsKey("x")) parseDouble(params["x"]) else null
        val yOverride = if (params.containsKey("y")) parseDouble(params["y"]) else null
        val zOverride = if (params.containsKey("z")) parseDouble(params["z"]) else null

        val xShift = if (params.containsKey("xs")) parseDouble(params["xs"]) else 0.0
        val yShift = if (params.containsKey("ys")) parseDouble(params["ys"]) else 0.0
        val zShift = if (params.containsKey("zs")) parseDouble(params["zs"]) else 0.0

        val minR = if (params.containsKey("rm")) parseDouble(params["rm"]) else 0.0
        val maxR = if (params.containsKey("r")) parseDouble(params["r"]) else 30000000.0

        val x = (xOverride ?: sender.positionVector.xCoord) + xShift
        val y = (yOverride ?: sender.positionVector.yCoord) + yShift
        val z = (zOverride ?: sender.positionVector.zCoord) + zShift

        val pos = Vec3d(x, y, z)

        val dx = if (params.containsKey("dx")) parseDouble(params["dx"]) else null
        val dy = if (params.containsKey("dy")) parseDouble(params["dy"]) else null
        val dz = if (params.containsKey("dz")) parseDouble(params["dz"]) else null

        if (dx != null) ret.add { it.x < x + dx || it.x > x - dx }
        if (dz != null) ret.add { it.z < z + dz || it.z > z - dz }

        if (scope == "slice") {
            val any = { it: BlockPos ->
                val dSq = pos.squareDistanceTo(it.x.toDouble(), pos.yCoord, it.z.toDouble())
                dSq >= minR * minR && dSq <= maxR * maxR
            }
            ret.add(any)
        } else {
            ret.add {
                val dSq = pos.squareDistanceTo(Vec3d(it))
                dSq >= minR * minR && dSq <= maxR * maxR
            }
            if (dy != null) ret.add { it.y < y + dy || it.y > y - dy }
        }

        return ret
    }


    override fun getTabCompletionOptions(server: MinecraftServer?, sender: ICommandSender?, args: Array<out String>, pos: BlockPos?): List<String> {
        if (args.size == 1)
            return getListOfStringsMatchingLastWord(args, *positionals)
        return emptyList()
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "dataexecute"
    override fun getCommandAliases() = mutableListOf("executedata")
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.dataexecute.usage"
}
