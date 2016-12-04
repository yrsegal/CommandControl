package wiresegal.cmdctrl.common.commands.data

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import wiresegal.cmdctrl.common.core.ControlSaveData
import wiresegal.cmdctrl.common.core.ScoreMap
import wiresegal.cmdctrl.common.core.Slice

/**
 * @author WireSegal
 * Created at 7:36 PM on 12/3/16.
 */
object CommandDataExecute : CommandBase() {

    val positionals = arrayOf("slice", "pos")

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
            val pred = createSlicePredicate(rules, slices)
            for ((key, data) in slices) if (data.isNotEmpty() && pred(key)) {
                try {
                    val invocations = manager.executeCommand(PositionalSender(sender, key.toPos()), command)

                    if (invocations < 1) toThrow = CommandException("commands.execute.allInvocationsFailed", command)
                } catch (var24: Throwable) {
                    toThrow = CommandException("commands.execute.failed", command, key.toString())
                }
            }
            if (toThrow != null) throw toThrow
        } else {
            val poses = ControlSaveData[sender.entityWorld].posData
            val pred = createPosPredicate(rules, poses)
            for ((key, data) in poses) if (data.isNotEmpty() && pred(key)) {
                try {
                    val invocations = manager.executeCommand(PositionalSender(sender, key), command)

                    if (invocations < 1) toThrow = CommandException("commands.execute.allInvocationsFailed", command)
                } catch (var24: Throwable) {
                    toThrow = CommandException("commands.execute.failed", command, key.run { "$x, $y, $z"} )
                }
            }
        }
        if (toThrow != null) throw toThrow
    }

    fun createSlicePredicate(rules: String, dataStorage: ScoreMap<Slice>): (Slice) -> Boolean {
        return {true} //todo implement rules
    }

    fun createPosPredicate(rules: String, dataStorage: ScoreMap<BlockPos>): (BlockPos) -> Boolean {
        return {true} //todo implement rules
    }



    override fun getRequiredPermissionLevel() = 2
    override fun getCommandName() = "dataexecute"
    override fun getCommandUsage(sender: ICommandSender?) = "commandcontrol.dataexecute.usage"
}
