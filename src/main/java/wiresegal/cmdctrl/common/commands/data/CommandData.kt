package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.*
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.ScoreStorage
import wiresegal.cmdctrl.common.core.Slice

/**
 * @author WireSegal
 * Created at 7:36 PM on 12/3/16.
 */
object CommandData : CommandBase() {

    val validScopes = arrayOf("global", "world", "slice", "pos")
    val validCommands = arrayOf("set", "add", "list", "remove", "test", "operation")
    val operations = arrayOf("+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><")
    val positionals = arrayOf("slice", "pos")
    val validPositionals = arrayOf(*validCommands, "execute")

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        when (args.size) {
            0 -> throw WrongUsageException(getCommandUsage(sender))
            1 -> throw WrongUsageException(getCommandUsage(sender) + if (args[0] in validScopes) ".${args[0]}" else "")
            else -> when (args[0]) {
                "global" -> runGlobal(server, sender, args.drop(1).toTypedArray())
                "world" -> runWorld(server, sender, args.drop(1).toTypedArray())
                "slice" -> runSlice(server, sender, args.drop(1).toTypedArray())
                "pos" -> runPos(server, sender, args.drop(1).toTypedArray())
            }
        }
    }

    fun getData(server: MinecraftServer, sender: ICommandSender, scope: String, key: String, otherArgs: Array<String>): Int {
        val globalData = ControlSaveData[server.worldServerForDimension(0)]
        val data = ControlSaveData[sender.entityWorld]

        var ret: Int? = null
        when (scope) {
            "global" -> ret = globalData.globalData[key] ?: 0
            "world" -> ret = data.worldData[key] ?: 0
            "slice" -> {
                if (otherArgs.size < 2)
                    throw CommandException("commandcontrol.storedata.autoget.slice")
                val pos = sender.position
                val x = parseDouble(pos.x.toDouble(), otherArgs[0], -3000000, 3000000, false)
                val z = parseDouble(pos.z.toDouble(), otherArgs[1], -3000000, 3000000, false)
                val slice = Slice(x, z)
                ret = data.sliceData[slice][key] ?: 0
            }
            "pos" -> {
                if (otherArgs.size < 3)
                    throw CommandException("commandcontrol.storedata.autoget.pos")
                val senderPos = sender.position
                val x = parseDouble(senderPos.x.toDouble(), otherArgs[0], -3000000, 3000000, false)
                val y = parseDouble(senderPos.x.toDouble(), otherArgs[1], -3000000, 3000000, false)
                val z = parseDouble(senderPos.z.toDouble(), otherArgs[2], -3000000, 3000000, false)
                val pos = BlockPos(x, y, z)
                ret = data.posData[pos][key] ?: 0
            }
        }
        if (ret == null) throw CommandException("commandcontrol.storedata.autoget.scope.invalid")

        return ret
    }

    fun setData(server: MinecraftServer, sender: ICommandSender, scope: String, key: String, otherArgs: Array<String>, setTo: Int) {
        val globalData = ControlSaveData[server.worldServerForDimension(0)]
        val data = ControlSaveData[sender.entityWorld]

        when (scope) {
            "global" -> globalData.globalData[key] = setTo
            "world" -> data.worldData[key] = setTo
            "slice" -> {
                if (otherArgs.size < 2)
                    throw CommandException("commandcontrol.storedata.autoget.slice")
                val pos = sender.position
                val x = parseDouble(pos.x.toDouble(), otherArgs[0], -3000000, 3000000, false)
                val z = parseDouble(pos.z.toDouble(), otherArgs[1], -3000000, 3000000, false)
                val slice = Slice(x, z)
                data.sliceData[slice][key] = setTo
            }
            "pos" -> {
                if (otherArgs.size < 3)
                    throw CommandException("commandcontrol.storedata.autoget.pos")
                val senderPos = sender.position
                val x = parseDouble(senderPos.x.toDouble(), otherArgs[0], -3000000, 3000000, false)
                val y = parseDouble(senderPos.x.toDouble(), otherArgs[1], -3000000, 3000000, false)
                val z = parseDouble(senderPos.z.toDouble(), otherArgs[2], -3000000, 3000000, false)
                val pos = BlockPos(x, y, z)
                data.posData[pos][key] = setTo
            }
            else -> throw CommandException("commandcontrol.storedata.autoget.scope.invalid")
        }
    }

    fun runGlobal(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args[0] !in validCommands)
            throw WrongUsageException("${getCommandUsage(sender)}.global")

        val globalData = ControlSaveData[server.worldServerForDimension(0)]
        runCommands("global", server, BlockPos.ORIGIN, sender, args, globalData.globalData)
        globalData.markDirty()
    }

    fun runWorld(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
        if (args[0] !in validCommands)
            throw WrongUsageException("${getCommandUsage(sender)}.world")

        val data = ControlSaveData[sender.entityWorld]
        runCommands("world", server, BlockPos.ORIGIN, sender, args, data.worldData)
        data.markDirty()
    }

    fun runSlice(server: MinecraftServer, sender: ICommandSender, originalArgs: Array<out String>) {
        if (originalArgs.size < 3 || originalArgs[2] !in validPositionals)
            throw WrongUsageException("${getCommandUsage(sender)}.slice")

        val pos = sender.position
        val x = parseDouble(pos.x.toDouble(), originalArgs[0], -3000000, 3000000, false)
        val z = parseDouble(pos.z.toDouble(), originalArgs[1], -3000000, 3000000, false)
        val slice = Slice(x, z)

        val data = ControlSaveData[sender.entityWorld]

        val args = originalArgs.drop(2).toTypedArray()
        runCommands("slice", server, pos, sender, args, data.sliceData[slice])
        data.markDirty()
    }

    fun runPos(server: MinecraftServer, sender: ICommandSender, originalArgs: Array<out String>) {
        if (originalArgs.size < 4 || originalArgs[3] !in validPositionals)
            throw WrongUsageException("${getCommandUsage(sender)}.pos")

        val senderPos = sender.position
        val x = parseDouble(senderPos.x.toDouble(), originalArgs[0], -3000000, 3000000, false)
        val y = parseDouble(senderPos.y.toDouble(), originalArgs[1], -3000000, 3000000, false)
        val z = parseDouble(senderPos.z.toDouble(), originalArgs[2], -3000000, 3000000, false)
        val pos = BlockPos(x, y, z)

        val args = originalArgs.drop(3).toTypedArray()

        val data = ControlSaveData[sender.entityWorld]
        runCommands("pos", server, pos, sender, args, data.posData[pos])
        data.markDirty()
    }

    fun runCommands(scope: String, server: MinecraftServer, blockPos: BlockPos, sender: ICommandSender, originalArgs: Array<out String>, scoreStorage: ScoreStorage) {
        val command = originalArgs[0]
        val args = originalArgs.drop(1)
        when (command) {
            "set" -> {
                if (args.size < 2)
                    throw WrongUsageException("${getCommandUsage(sender)}.$scope.set")
                val setTo = parseInt(args[1])
                scoreStorage[args[0]]
                notifyCommandListener(sender, this, "commandcontrol.storedata.set", args[0], setTo)
            }
            "add" -> {
                if (args.size < 2)
                    throw WrongUsageException("${getCommandUsage(sender)}.$scope.add")
                val setTo = parseInt(args[1]) + (scoreStorage[args[0]] ?: 0)
                scoreStorage[args[0]] = setTo
                notifyCommandListener(sender, this, "commandcontrol.storedata.set", args[0], setTo)
            }
            "remove" -> {
                if (args.isEmpty())
                    throw WrongUsageException("${getCommandUsage(sender)}.$scope.remove")
                scoreStorage.remove(args[0])
                notifyCommandListener(sender, this, "commandcontrol.storedata.removed", args[0])
            }
            "list" -> {
                notifyCommandListener(sender, this, "commandcontrol.storedata.list.$scope", blockPos.x, blockPos.y, blockPos.z, sender.entityWorld.provider.dimension, sender.entityWorld.provider.dimensionType.getName())
                for ((key, value) in scoreStorage)
                    notifyCommandListener(sender, this, "commandcontrol.storedata.list.entry", key, value)
            }
            "test" -> {
                if (args.isEmpty())
                    throw WrongUsageException(getCommandUsage(sender) + ".$scope.test")
                val value = scoreStorage[args[0]]
                if (value != null) {
                    if (args.size > 1 && value < parseInt(args[0]))
                        throw CommandException("commandcontrol.storedata.toosmall")
                    else if (args.size > 2 && value > parseInt(args[1]))
                        throw CommandException("commandcontrol.storedata.toolarge")
                } else throw CommandException("commandcontrol.storedata.notfound")
                notifyCommandListener(sender, this, "commandcontrol.storedata.exists", args[0])
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, value)
            }
            "operation" -> {
                if (args.size < 4)
                    throw WrongUsageException("${getCommandUsage(sender)}.global.operation")
                val key = args[0]
                val value = scoreStorage[key] ?: 0
                fun setValue(value: Int) { scoreStorage[key] = value }
                val operation = args[1]
                val otherScope = args[2]
                val otherKey = args[3]
                val otherArgs = args.drop(4).toTypedArray()
                val other = getData(server, sender, otherScope, otherKey, otherArgs)
                when (operation) {
                    "+=" -> setValue(value + other)
                    "-=" -> setValue(value - other)
                    "*=" -> setValue(value * other)
                    "/=" -> setValue(value / other)
                    "%=" -> setValue(value % other)
                    "=" -> setValue(other)
                    "<" -> setValue(Math.min(value, other))
                    ">" -> setValue(Math.max(value, other))
                    "><" -> {
                        setValue(other)
                        setData(server, sender, otherScope, otherKey, otherArgs, value)
                    }
                    else ->
                        throw CommandException("commands.scoreboard.players.operation.invalidOperation", operation)
                }
            }
            "execute" -> {
                if (scope !in positionals)
                    throw CommandException("commandcontrol.storedata.executenotallowed")
                //todo execute
            }
        }
    }

    override fun getTabCompletionOptions(server: MinecraftServer, sender: ICommandSender, args: Array<out String>, pos: BlockPos?): List<String> {
        if (args.size == 1) return getListOfStringsMatchingLastWord(args, *validScopes)
        if (args[0] in validScopes) {
            if (args[0] !in positionals) {
                if (args.size == 2) return getListOfStringsMatchingLastWord(args, *validCommands)
                else if (args[1] == "operation") {
                    if (args.size == 4) return getListOfStringsMatchingLastWord(args, *operations)
                    if (args.size == 5) return getListOfStringsMatchingLastWord(args, *validScopes)
                    if (args.size >= 6) {
                        if (args[5] == "slice") {
                            if (args.size == 6)
                                return getTabCompletionCoordinate(args, 5, pos)
                            else if (args.size == 7)
                                return getTabCompletionCoordinate(args, 4, pos)
                        } else if (args[6] == "pos")
                            return getTabCompletionCoordinate(args, 5, pos)
                    }
                }
            } else {
                if (args[0] == "slice") {
                    if (args.size == 2)
                        return getTabCompletionCoordinate(args, 1, pos)
                    else if (args.size == 3)
                        return getTabCompletionCoordinate(args, 0, pos)
                    else if (args.size == 4)
                        return getListOfStringsMatchingLastWord(args, *validCommands)
                    else if (args[3] == "operation") {
                        if (args.size == 6) return getListOfStringsMatchingLastWord(args, *operations)
                        if (args.size == 7) return getListOfStringsMatchingLastWord(args, *validScopes)
                        if (args.size >= 8) {
                            if (args[7] == "slice") {
                                if (args.size == 8)
                                    return getTabCompletionCoordinate(args, 7, pos)
                                else if (args.size == 9)
                                    return getTabCompletionCoordinate(args, 6, pos)
                            } else if (args[8] == "pos")
                                return getTabCompletionCoordinate(args, 7, pos)
                        }
                    }
                } else {
                    if (args.size in 2..4)
                        return getTabCompletionCoordinate(args, 1, pos)
                    else if (args.size == 5)
                        return getListOfStringsMatchingLastWord(args, *validCommands)
                    else if (args[4] == "operation") {
                        if (args.size == 7) return getListOfStringsMatchingLastWord(args, *operations)
                        if (args.size == 8) return getListOfStringsMatchingLastWord(args, *validScopes)
                        if (args.size >= 9) {
                            if (args[8] == "slice") {
                                if (args.size == 9)
                                    return getTabCompletionCoordinate(args, 8, pos)
                                else if (args.size == 10)
                                    return getTabCompletionCoordinate(args, 7, pos)
                            } else if (args[9] == "pos")
                                return getTabCompletionCoordinate(args, 8, pos)
                        }
                    }
                }
            }
        }

        return emptyList()
    }

    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "storedata"
    override fun getCommandAliases() = listOf("savedata")
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.storedata.usage"
}
